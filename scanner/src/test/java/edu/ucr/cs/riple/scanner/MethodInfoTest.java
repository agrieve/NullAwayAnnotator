/*
 * MIT License
 *
 * Copyright (c) 2022 Nima Karimipour
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

package edu.ucr.cs.riple.scanner;

import com.google.common.base.Preconditions;
import edu.ucr.cs.riple.scanner.tools.DisplayFactory;
import edu.ucr.cs.riple.scanner.tools.MethodInfoDisplay;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MethodInfoTest extends ScannerBaseTest<MethodInfoDisplay> {

  private static final DisplayFactory<MethodInfoDisplay> METHOD_DISPLAY_FACTORY =
      values -> {
        Preconditions.checkArgument(values.length == 9, "Expected to find 9 values on each line");
        // Outputs are written in Temp Directory and is not known at compile time, therefore,
        // relative paths are getting compared.
        MethodInfoDisplay display =
            new MethodInfoDisplay(
                values[0], values[1], values[2], values[3], values[4], values[5], values[6],
                values[7], values[8]);
        display.path = display.path.substring(display.path.indexOf("edu/ucr/"));
        return display;
      };
  private static final String HEADER =
      "id"
          + "\t"
          + "class"
          + "\t"
          + "method"
          + "\t"
          + "parent"
          + "\t"
          + "size"
          + "\t"
          + "flags"
          + "\t"
          + "nullable"
          + "\t"
          + "parameters"
          + "\t"
          + "uri";
  private static final String FILE_NAME = "method_info.tsv";

  public MethodInfoTest() {
    super(METHOD_DISPLAY_FACTORY, HEADER, FILE_NAME);
  }

  @Test
  public void BasicTest() {
    tester
        .addSourceLines(
            "edu/ucr/A.java",
            "package edu.ucr;",
            "public class A {",
            "   public Object returnNonNull(){",
            "      return new Object();",
            "   }",
            "}")
        .setExpectedOutputs(
            new MethodInfoDisplay(
                "1",
                "edu.ucr.A",
                "returnNonNull()",
                "0",
                "0",
                "[]",
                "false",
                "[]",
                "edu/ucr/A.java"))
        .doTest();
  }
}