/*
 * MIT License
 *
 * Copyright (c) 2020 Nima Karimipour
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package edu.ucr.cs.riple.core;

import com.google.common.base.Preconditions;
import com.uber.nullaway.fixserialization.FixSerializationConfig;
import edu.ucr.cs.css.Serializer;
import edu.ucr.cs.css.XMLUtil;
import edu.ucr.cs.riple.core.explorers.BasicExplorer;
import edu.ucr.cs.riple.core.explorers.DeepExplorer;
import edu.ucr.cs.riple.core.explorers.DummyExplorer;
import edu.ucr.cs.riple.core.explorers.Explorer;
import edu.ucr.cs.riple.core.explorers.FieldExplorer;
import edu.ucr.cs.riple.core.explorers.MethodExplorer;
import edu.ucr.cs.riple.core.explorers.ParameterExplorer;
import edu.ucr.cs.riple.core.metadata.index.Bank;
import edu.ucr.cs.riple.core.metadata.index.Error;
import edu.ucr.cs.riple.core.metadata.index.FixEntity;
import edu.ucr.cs.riple.core.metadata.method.MethodInheritanceTree;
import edu.ucr.cs.riple.core.metadata.trackers.FieldRegionTracker;
import edu.ucr.cs.riple.core.metadata.trackers.MethodRegionTracker;
import edu.ucr.cs.riple.core.util.Utility;
import edu.ucr.cs.riple.injector.Fix;
import edu.ucr.cs.riple.injector.Injector;
import edu.ucr.cs.riple.injector.WorkListBuilder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Annotator {

  public Path fixPath;
  public Path errorPath;
  public Path dir;
  public Path nullAwayConfigPath;

  public String nullableAnnot;
  public int depth;
  public boolean lexicalPreservationEnabled;

  private String buildCommand;
  private Injector injector;
  private List<Report> finishedReports;
  private List<Explorer> explorers;
  private DeepExplorer deepExplorer;

  public MethodRegionTracker methodRegionTracker;
  public FieldRegionTracker fieldRegionTracker;
  public MethodInheritanceTree methodInheritanceTree;

  public static final Log log = new Log();

  public static class Log {
    public int total;
    int requested;
    long time;
    long deep;
    long buildTime = 0;

    @Override
    public String toString() {
      return "total="
          + total
          + ", requested="
          + requested
          + ", time="
          + time
          + ", deep="
          + deep
          + ", buildTime="
          + buildTime;
    }
  }

  private List<Fix> init(String buildCommand, boolean useCache, boolean optimized) {
    System.out.println("Making the first build.");
    this.buildCommand = buildCommand;
    this.finishedReports = new ArrayList<>();
    FixSerializationConfig.Builder builder =
        new FixSerializationConfig.Builder()
            .setSuggest(true, true)
            .setAnnotations(nullableAnnot, "UNKNOWN")
            .setOutputDirectory(this.dir.toString());
    buildProject(builder, false);
    List<Fix> allFixes = Utility.readAllFixes(fixPath);
    if (useCache) {
      Utility.removeCachedFixes(allFixes, dir);
    }
    allFixes = Collections.unmodifiableList(allFixes);
    log.total = allFixes.size();
    this.injector =
        Injector.builder()
            .setMode(Injector.MODE.BATCH)
            .keepStyle(lexicalPreservationEnabled)
            .build();
    this.methodInheritanceTree =
        new MethodInheritanceTree(dir.resolve(Serializer.METHOD_INFO_NAME));
    this.methodRegionTracker = new MethodRegionTracker(dir.resolve(Serializer.CALL_GRAPH_NAME));
    this.fieldRegionTracker = new FieldRegionTracker(dir.resolve(Serializer.FIELD_GRAPH_NAME));
    Bank<Error> errorBank = new Bank<>(errorPath, Error::new);
    Bank<FixEntity> fixBank = new Bank<>(fixPath, FixEntity::new);
    this.explorers = new ArrayList<>();
    if (depth < 0) {
      this.explorers.add(new DummyExplorer(this, null, null));
    }
    if (optimized && depth > -1) {
      System.out.println("Initializing Explorers.");
      this.explorers.add(new ParameterExplorer(this, allFixes, errorBank, fixBank));
      this.explorers.add(new FieldExplorer(this, allFixes, errorBank, fixBank));
      this.explorers.add(new MethodExplorer(this, allFixes, errorBank, fixBank));
    }
    this.explorers.add(new BasicExplorer(this, errorBank, fixBank));
    this.deepExplorer = new DeepExplorer(this, errorBank, fixBank);
    return allFixes;
  }

  public void start(
      String buildCommand,
      Path nullawayConfigPath,
      boolean useCache,
      boolean optimized,
      boolean bailout,
      boolean chain) {
    log.time = System.currentTimeMillis();
    System.out.println("Annotator Started.");
    this.nullAwayConfigPath = nullawayConfigPath;
    this.dir =
        Paths.get(
            XMLUtil.getValueFromTag(nullawayConfigPath, "/serialization/path", String.class)
                .orElse("/tmp/NullAwayFix"));
    this.fixPath = this.dir.resolve("fixes.tsv");
    this.errorPath = this.dir.resolve("errors.tsv");
    List<Fix> fixes = init(buildCommand, useCache, optimized);
    fixes.forEach(
        fix -> {
          if (finishedReports
              .stream()
              .noneMatch(diagnoseReport -> diagnoseReport.fix.equals(fix))) {
            analyze(fix);
          }
        });
    log.deep = System.currentTimeMillis();
    if (depth > -1) {
      this.deepExplorer.start(bailout, optimized, finishedReports, log);
    }
    log.deep = System.currentTimeMillis() - log.deep;
    log.time = System.currentTimeMillis() - log.time;
    Utility.writeReports(dir, finishedReports, chain);
    Utility.writeLog(this);
  }

  public void remove(List<Fix> fixes) {
    if (fixes == null || fixes.size() == 0) {
      return;
    }
    List<Fix> toRemove =
        fixes
            .stream()
            .map(
                fix ->
                    new Fix(
                        fix.annotation,
                        fix.method,
                        fix.variable,
                        fix.location,
                        fix.className,
                        fix.uri,
                        "false"))
            .collect(Collectors.toList());
    apply(toRemove);
  }

  public void apply(List<Fix> fixes) {
    if (fixes == null || fixes.size() == 0) {
      return;
    }
    injector.start(
        new WorkListBuilder(fixes).getWorkLists(), false, dir.resolve("injector_log.json"));
  }

  private void analyze(Fix fix) {
    Report report = null;
    boolean appliedFix = false;
    for (Explorer explorer : explorers) {
      if (explorer.isApplicable(fix)) {
        if (explorer.requiresInjection(fix)) {
          apply(Collections.singletonList(fix));
          appliedFix = true;
        }
        report = explorer.effect(fix);
        break;
      }
    }
    if (appliedFix) {
      remove(Collections.singletonList(fix));
    }
    Preconditions.checkNotNull(report);
    finishedReports.add(report);
  }

  public void buildProject(FixSerializationConfig.Builder writer, boolean count) {
    if (count) {
      log.requested++;
    }
    writer.writeAsXML(nullAwayConfigPath.toString());
    try {
      long start = System.currentTimeMillis();
      Utility.executeCommand(buildCommand);
      log.buildTime += System.currentTimeMillis() - start;
    } catch (Exception e) {
      throw new RuntimeException("Could not run command: " + buildCommand);
    }
  }

  public void buildProject(FixSerializationConfig.Builder writer) {
    buildProject(writer, true);
  }
}
