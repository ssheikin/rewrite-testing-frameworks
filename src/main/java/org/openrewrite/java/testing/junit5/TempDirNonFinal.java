/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.testing.junit5;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Modifier.Type;

import java.time.Duration;
import java.util.stream.Collectors;

public class TempDirNonFinal extends Recipe {

    private static final AnnotationMatcher TEMPDIR_ANNOTATION_MATCHER = new AnnotationMatcher(
            "@org.junit.jupiter.api.io.TempDir");

    @Override
    public String getDisplayName() {
        return "Make `@TempDir` fields non final";
    }

    @Override
    public String getDescription() {
        return "Make JUnit 5's `org.junit.jupiter.api.io.TempDir` fields non final.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getSingleSourceApplicableTest() {
        return new UsesType<>("org.junit.jupiter.api.io.TempDir");
    }

    @Override
    protected TempDirVisitor getVisitor() {
        return new TempDirVisitor();
    }

    private static class TempDirVisitor extends JavaVisitor<ExecutionContext> {
        @Override
        public J visitVariableDeclarations(J.VariableDeclarations multiVariable, ExecutionContext executionContext) {
            J.VariableDeclarations mv = (J.VariableDeclarations) super.visitVariableDeclarations(multiVariable,
                    executionContext);
            if (mv.getLeadingAnnotations().stream().anyMatch(TEMPDIR_ANNOTATION_MATCHER::matches)
                    && mv.getModifiers().stream().anyMatch(mod -> mod.getType() == Type.Final)) {
                return mv.withModifiers(mv.getModifiers().stream()
                        .filter(mod -> mod.getType() != Type.Final)
                        .collect(Collectors.toList()));
            }
            return mv;
        }
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(1);
    }
}