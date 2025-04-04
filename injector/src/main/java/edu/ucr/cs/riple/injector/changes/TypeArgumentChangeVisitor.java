/*
 * Copyright (c) 2023 University of California, Riverside.
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

package edu.ucr.cs.riple.injector.changes;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.WildcardType;
import com.github.javaparser.ast.visitor.GenericVisitorWithDefaults;
import com.google.common.collect.ImmutableList;
import edu.ucr.cs.riple.injector.modifications.Modification;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Set;

/**
 * A visitor that computes the modifications needed to change the type arguments of a node. This
 * visitor supports the following types:
 *
 * <ul>
 *   <li>{@link ClassOrInterfaceType}
 *   <li>{@link ArrayType}
 * </ul>
 *
 * If other types are visited by this visitor, no changes will be applied. If support for further
 * types are desired, their corresponding visit methods should be overridden.
 */
public class TypeArgumentChangeVisitor
    extends GenericVisitorWithDefaults<Set<Modification>, TypeUseAnnotationChange> {

  private final Deque<Integer> index;
  private final AnnotationExpr annotationExpr;

  public TypeArgumentChangeVisitor(ImmutableList<Integer> index, AnnotationExpr annotationExpr) {
    this.index = new ArrayDeque<>(index);
    this.annotationExpr = annotationExpr;
  }

  /**
   * Applies the change on the given type.
   *
   * @param type the type to apply the change on.
   * @param change the change to apply.
   * @return the set of modifications to apply on the type.
   */
  private Set<Modification> applyOnType(Type type, TypeUseAnnotationChange change) {
    Modification onType = change.computeTextModificationOnType(type, annotationExpr);
    if (onType != null) {
      return Set.of(onType);
    } else {
      // Unable to apply the change on the type.
      return Collections.emptySet();
    }
  }

  /**
   * Checks if the index reached to base.
   *
   * @return true if the index is at base, false otherwise.
   */
  private boolean isAtBase() {
    return index.size() == 1 && index.getFirst() == 0;
  }

  @Override
  public Set<Modification> visit(ClassOrInterfaceType type, TypeUseAnnotationChange change) {
    if (isAtBase()) {
      return applyOnType(type, change);
    }
    if (type.getTypeArguments().isEmpty() || this.index.isEmpty()) {
      return Collections.emptySet();
    }
    int index = this.index.pollFirst() - 1;
    if (type.getTypeArguments().get().size() <= index) {
      return Collections.emptySet();
    }
    return type.getTypeArguments().get().get(index).accept(this, change);
  }

  @Override
  public Set<Modification> visit(ArrayType type, TypeUseAnnotationChange change) {
    if (isAtBase()) {
      return applyOnType(type, change);
    }
    if (!index.isEmpty() && index.pollFirst() == 1) {
      // current index is 1, we need to visit the component type.
      return type.getComponentType().accept(this, change);
    }
    return Collections.emptySet();
  }

  @Override
  public Set<Modification> visit(WildcardType type, TypeUseAnnotationChange change) {
    return applyOnType(type, change);
  }

  /**
   * This method is called when the visitor visits a primitive type. This is necessary because the
   * component type of array may be a primitive type.
   *
   * @param type the primitive type
   * @param change the change to apply
   * @return the set of modifications to apply on the type.
   */
  @Override
  public Set<Modification> visit(PrimitiveType type, TypeUseAnnotationChange change) {
    if (isAtBase()) {
      return applyOnType(type, change);
    }
    return Collections.emptySet();
  }

  /** This will be called by every node visit method that is not overridden. */
  @Override
  public Set<Modification> defaultAction(Node n, TypeUseAnnotationChange arg) {
    // For now, we do not intend to annotate any other type.
    return Collections.emptySet();
  }
}
