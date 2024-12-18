/*
 * MIT License
 *
 * Copyright (c) 2023 Nima Karimipour
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

package edu.ucr.cs.riple.core.cache;

import com.google.common.collect.ImmutableSet;
import edu.ucr.cs.riple.core.registries.index.Error;
import edu.ucr.cs.riple.core.registries.index.Fix;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Base class for impact caches.
 *
 * @param <T> type of impacts saved in this model.
 * @param <S> type of the map used to store impacts.
 */
public abstract class BaseCache<T extends Impact, S extends Map<Fix, T>> implements ImpactCache<T> {

  /** Container holding cache entries. */
  protected final S store;

  public BaseCache(S store) {
    this.store = store;
  }

  @Override
  public boolean isUnknown(Fix fix) {
    return !this.store.containsKey(fix);
  }

  @Nullable
  @Override
  public T fetchImpact(Fix fix) {
    return store.get(fix);
  }

  @Override
  public ImmutableSet<Error> getTriggeredErrorsForCollection(Collection<Fix> fixes) {
    return fixes.stream()
        .map(store::get)
        .filter(Objects::nonNull)
        .flatMap(impact -> impact.triggeredErrors.stream())
        // filter errors that will be resolved with the existing collection of fixes.
        .filter(error -> !error.isResolvableWith(fixes))
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public ImmutableSet<Fix> getTriggeredFixesFromDownstreamForCollection(Collection<Fix> fixTree) {
    return fixTree.stream()
        .map(store::get)
        .filter(Objects::nonNull)
        .flatMap(impact -> impact.getTriggeredFixesFromDownstreamErrors().stream())
        // filter fixes that are already inside tree.
        .filter(fix -> !fixTree.contains(fix))
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public ImmutableSet<Error> getTriggeredErrors(Fix fix) {
    T impact = fetchImpact(fix);
    return impact == null ? ImmutableSet.of() : impact.getTriggeredErrors();
  }

  @Override
  public void updateImpactsAfterInjection(Collection<Fix> fixes) {
    this.store.values().forEach(impact -> impact.updateStatusAfterInjection(fixes));
  }

  @Override
  public int size() {
    return this.store.size();
  }
}
