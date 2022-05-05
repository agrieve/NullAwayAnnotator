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

import edu.ucr.cs.riple.core.Annotator;
import edu.ucr.cs.riple.core.util.Utility;
import edu.ucr.cs.riple.injector.Fix;
import edu.ucr.cs.riple.injector.Injector;
import edu.ucr.cs.riple.injector.WorkListBuilder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {

  public static void main(String[] args) {
    if (args.length == 0) {
      throw new RuntimeException("command not specified");
    }
    String command = args[0];
    switch (command) {
      case "apply":
        apply(args);
        break;
      case "explore":
        explore(args);
        break;
      default:
        throw new RuntimeException("Unknown command: " + command);
    }
  }

  private static void explore(String[] args) {
    if (args.length != 10) {
      throw new RuntimeException(
          "Annotator: explore needs 9 arguments: 1. command to execute NullAway, "
              + "2. output directory, 3. Annotator Depth level, 4. Nullable Annotation, 5. style 6. optimization flag, 7. Bail Out, 8. Cache, 9. Chain But received: "
              + Arrays.toString(args));
    }
    Annotator annotator = new Annotator();
    Path nullawayConfigPath = Paths.get(args[1]);
    String runCommand = args[2];
    annotator.depth = Integer.parseInt(args[3]);
    annotator.nullableAnnot = args[4];
    annotator.lexicalPreservationEnabled = Boolean.parseBoolean(args[5]);
    annotator.start(
        runCommand,
        nullawayConfigPath,
        Boolean.parseBoolean(args[6]),
        Boolean.parseBoolean(args[7]),
        Boolean.parseBoolean(args[8]),
        Boolean.parseBoolean(args[9]));
  }

  private static void apply(String[] args) {
    if (args.length != 3) {
      throw new RuntimeException(
          "Annotator:apply needs two arguments: 1. path to the suggested fix file, 2. code style preservation flag");
    }
    boolean keepStyle = Boolean.parseBoolean(args[2]);
    Injector injector =
        Injector.builder().setMode(Injector.MODE.BATCH).keepStyle(keepStyle).build();
    List<Fix> fixes = Utility.readFixesJson(Paths.get(args[1]));
    Path logPath = Paths.get(args[1]).getParent().resolve("injector_log.tsv");
    injector.start(new WorkListBuilder(fixes).getWorkLists(), logPath);
  }
}
