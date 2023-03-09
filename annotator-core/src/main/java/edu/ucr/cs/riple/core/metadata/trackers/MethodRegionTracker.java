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

package edu.ucr.cs.riple.core.metadata.trackers;

import com.google.common.collect.ImmutableSet;
import edu.ucr.cs.riple.core.Config;
import edu.ucr.cs.riple.core.ModuleInfo;
import edu.ucr.cs.riple.core.metadata.MetaData;
import edu.ucr.cs.riple.core.metadata.method.MethodDeclarationTree;
import edu.ucr.cs.riple.core.metadata.method.MethodNode;
import edu.ucr.cs.riple.injector.location.Location;
import edu.ucr.cs.riple.injector.location.OnMethod;
import edu.ucr.cs.riple.scanner.Serializer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Tracker for Methods. */
public class MethodRegionTracker extends MetaData<TrackerNode> implements RegionTracker {

  /**
   * {@link MethodDeclarationTree} instance, used to retrieve regions that will be affected due to
   * inheritance violations.
   */
  private final MethodDeclarationTree methodDeclarationTree;

  public MethodRegionTracker(
      Config config, ModuleInfo info, MethodDeclarationTree methodDeclarationTree) {
    super(config, info.dir.resolve(Serializer.METHOD_IMPACTED_REGION_FILE_NAME));
    this.methodDeclarationTree = methodDeclarationTree;
  }

  public MethodRegionTracker(
      Config config,
      ImmutableSet<ModuleInfo> modules,
      MethodDeclarationTree methodDeclarationTree) {
    super(
        config,
        modules.stream()
            .map(info -> info.dir.resolve(Serializer.METHOD_IMPACTED_REGION_FILE_NAME))
            .collect(ImmutableSet.toImmutableSet()));
    this.methodDeclarationTree = methodDeclarationTree;
  }

  @Override
  protected TrackerNode addNodeByLine(String[] values) {
    return config.getAdapter().deserializeTrackerNode(values);
  }

  @Override
  public Optional<Set<Region>> getRegions(Location location) {
    if (!location.isOnMethod()) {
      return Optional.empty();
    }
    OnMethod onMethod = location.toMethod();
    // Add callers of method.
    Set<Region> regions = getCallersOfMethod(onMethod.clazz, onMethod.method);
    // Add method itself.
    regions.add(new Region(onMethod.clazz, onMethod.method));
    // Add immediate super method.
    MethodNode parent =
        methodDeclarationTree.getClosestSuperMethod(onMethod.method, onMethod.clazz);
    if (parent != null && parent.isNonTop()) {
      regions.add(new Region(parent.location.clazz, parent.location.method));
    }
    return Optional.of(regions);
  }

  /**
   * Returns set of regions where the target method is called.
   *
   * @param clazz Fully qualified name of the class of the target method.
   * @param method Method signature.
   * @return Set of regions where target method is called.
   */
  public Set<Region> getCallersOfMethod(String clazz, String method) {
    return findNodesWithHashHint(
            candidate ->
                candidate.calleeClass.equals(clazz) && candidate.calleeMember.equals(method),
            TrackerNode.hash(clazz))
        .map(node -> node.region)
        .collect(Collectors.toSet());
  }
}