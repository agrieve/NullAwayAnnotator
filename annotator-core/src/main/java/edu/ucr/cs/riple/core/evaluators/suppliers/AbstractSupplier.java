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

package edu.ucr.cs.riple.core.evaluators.suppliers;

import com.google.common.collect.ImmutableSet;
import edu.ucr.cs.riple.core.Config;
import edu.ucr.cs.riple.core.ModuleInfo;
import edu.ucr.cs.riple.core.injectors.AnnotationInjector;
import edu.ucr.cs.riple.core.metadata.field.FieldRegistry;
import edu.ucr.cs.riple.core.metadata.index.Error;
import edu.ucr.cs.riple.core.metadata.index.ErrorStore;
import edu.ucr.cs.riple.core.metadata.method.MethodRegistry;

/** Base class for all instances of {@link Supplier}. */
public abstract class AbstractSupplier implements Supplier {

  /** Error Store instance. */
  protected final ErrorStore errorStore;
  /** Injector instance. */
  protected final AnnotationInjector injector;
  /** Method registry instance. */
  protected final MethodRegistry methodRegistry;
  /** Depth of analysis. */
  protected final int depth;
  /** Field declaration analysis to detect fixes on inline multiple field declaration statements. */
  protected final FieldRegistry fieldRegistry;
  /** Annotator config. */
  protected final Config config;

  public AbstractSupplier(
      ImmutableSet<ModuleInfo> modules, Config config, MethodRegistry registry) {
    this.config = config;
    this.fieldRegistry = new FieldRegistry(config, modules);
    this.methodRegistry = registry;
    this.errorStore = initializeErrorStore(modules);
    this.injector = initializeInjector();
    this.depth = initializeDepth();
  }

  /**
   * Initializer for injector.
   *
   * @return {@link AnnotationInjector} instance.
   */
  protected abstract AnnotationInjector initializeInjector();

  /**
   * Initializer for depth.
   *
   * @return depth of analysis.
   */
  protected abstract int initializeDepth();

  /**
   * Initializer for error store.
   *
   * @param modules Set of modules involved in the analysis.
   * @return {@link ErrorStore} of {@link Error} instances.
   */
  protected ErrorStore initializeErrorStore(ImmutableSet<ModuleInfo> modules) {
    return new ErrorStore(
        modules.stream()
            .map(info -> info.dir.resolve("errors.tsv"))
            .collect(ImmutableSet.toImmutableSet()),
        Error.factory(config, fieldRegistry));
  }

  @Override
  public ErrorStore getErrorStore() {
    return errorStore;
  }

  @Override
  public AnnotationInjector getInjector() {
    return injector;
  }

  @Override
  public MethodRegistry getMethodRegistry() {
    return methodRegistry;
  }

  @Override
  public int depth() {
    return depth;
  }

  @Override
  public Config getConfig() {
    return config;
  }
}
