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
@file:Suppress("JUnitMalformedDeclaration")

package org.openrewrite.java.testing.junit5

import org.junit.jupiter.api.Test
import org.openrewrite.Issue
import org.openrewrite.java.Assertions.java
import org.openrewrite.java.JavaParser
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

class LifecycleNonPrivateTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(LifecycleNonPrivate())
        spec.parser(JavaParser.fromJavaVersion()
            .classpath("junit")
            .build())
    }
    
    @Test
    @Issue("https://github.com/openrewrite/rewrite-testing-frameworks/issues/241")
    fun beforeEachPrivate() = rewriteRun(
        java("""
            import org.junit.jupiter.api.BeforeEach;
            
            class A {
                @BeforeEach
                private void beforeEach() {
                }
                private void unaffected() {
                }
            }
        """,
        """
            import org.junit.jupiter.api.BeforeEach;
            
            class A {
                @BeforeEach
                void beforeEach() {
                }
                private void unaffected() {
                }
            }
        """)
    )

    @Test
    @Issue("https://github.com/openrewrite/rewrite-testing-frameworks/issues/241")
    fun afterAllPrivate() = rewriteRun(
        java("""
            import org.junit.jupiter.api.AfterAll;
            
            class A {
                @AfterAll
                private static void afterAll() {
                }
                private void unaffected() {
                }
            }
        """,
        """
            import org.junit.jupiter.api.AfterAll;
            
            class A {
                @AfterAll
                static void afterAll() {
                }
                private void unaffected() {
                }
            }
        """)
    )

    @Test
    @Issue("https://github.com/openrewrite/rewrite-testing-frameworks/issues/241")
    fun beforeEachAfterAllUnchanged() = rewriteRun(
        java("""
            import org.junit.jupiter.api.AfterAll;
            import org.junit.jupiter.api.BeforeEach;
            
            class A {
                @BeforeEach
                void beforeEach() {
                }
                @AfterAll
                static void afterAll() {
                }
                private void unaffected() {
                }
            }
        """)
    )
}

