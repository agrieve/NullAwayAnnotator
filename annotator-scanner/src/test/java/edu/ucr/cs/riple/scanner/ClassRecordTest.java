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
import edu.ucr.cs.riple.scanner.tools.ClassRecordDisplay;
import edu.ucr.cs.riple.scanner.tools.DisplayFactory;
import org.junit.Test;

public class ClassRecordTest extends AnnotatorScannerBaseTest<ClassRecordDisplay> {

  private static final DisplayFactory<ClassRecordDisplay> CLASS_DISPLAY_FACTORY =
      values -> {
        Preconditions.checkArgument(values.length == 2, "Expected to find 2 values on each line");
        // Outputs are written in Temp Directory and is not known at compile time, therefore,
        // relative paths are getting compared.
        ClassRecordDisplay display = new ClassRecordDisplay(values[0], values[1]);
        display.path = display.path.substring(display.path.indexOf("edu/ucr/"));
        return new ClassRecordDisplay(values[0], values[1].substring(1));
      };
  private static final String HEADER = "class\tpath";
  private static final String FILE_NAME = "class_records.tsv";

  public ClassRecordTest() {
    super(CLASS_DISPLAY_FACTORY, HEADER, FILE_NAME);
  }

  @Test
  public void basicTest() {
    tester
        .addSourceLines("edu/ucr/A.java", "package edu.ucr;", "public class A", "{", "}")
        .setExpectedOutputs(new ClassRecordDisplay("edu.ucr.A", "edu/ucr/A.java"))
        .doTest();
  }

  @Test
  public void checkClassesAreWrittenInFlatNameTest() {
    tester
        .addSourceFile("SampleClassForTest.java")
        .setExpectedOutputs(
            new ClassRecordDisplay(
                "edu.ucr.cs.riple.scanner.testdata.SampleClassForTest",
                "edu/ucr/cs/riple/scanner/SampleClassForTest.java"),
            new ClassRecordDisplay(
                "edu.ucr.cs.riple.scanner.testdata.SampleClassForTest$Inner",
                "edu/ucr/cs/riple/scanner/SampleClassForTest.java"),
            new ClassRecordDisplay(
                "edu.ucr.cs.riple.scanner.testdata.SampleClassForTest$1InnerMethod",
                "edu/ucr/cs/riple/scanner/SampleClassForTest.java"),
            new ClassRecordDisplay(
                "edu.ucr.cs.riple.scanner.testdata.SampleClassForTest$1InnerMethod$1",
                "edu/ucr/cs/riple/scanner/SampleClassForTest.java"),
            new ClassRecordDisplay(
                "edu.ucr.cs.riple.scanner.testdata.Run",
                "edu/ucr/cs/riple/scanner/SampleClassForTest.java"))
        .doTest();
  }
}
