/*
 * Copyright 2013-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.security.web.authentication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the {@code Accept}-header negotiation that decides whether an
 * authentication submission is answered with a JSON envelope or a redirect.
 * The rule must never misread a browser form post as JSON, otherwise the
 * server-rendered login page would silently lose its redirect.
 */
class SecurityJsonResponsesTests {

    @Test
    void traditionalBrowserFormPostIsNotJson() {
        // The Accept header a browser sends for an HTML form POST / navigation.
        // Its trailing */* must not be mistaken for a JSON preference.
        assertFalse(SecurityJsonResponses.prefersJson(
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"));
    }

    @Test
    void explicitJsonIsJson() {
        assertTrue(SecurityJsonResponses.prefersJson("application/json"));
    }

    @Test
    void jsonListedAmongOthersIsJson() {
        assertTrue(SecurityJsonResponses.prefersJson("application/json, text/plain, */*"));
    }

    @Test
    void jsonWithQualityFactorIsJson() {
        assertTrue(SecurityJsonResponses.prefersJson("application/json;q=0.9"));
    }

    @Test
    void bareWildcardIsNotJson() {
        // fetch / XMLHttpRequest default to */* unless the client opts in,
        // so a bare wildcard must stay on the redirect path.
        assertFalse(SecurityJsonResponses.prefersJson("*/*"));
    }

    @Test
    void htmlOnlyIsNotJson() {
        assertFalse(SecurityJsonResponses.prefersJson("text/html"));
    }

    @Test
    void missingOrBlankIsNotJson() {
        assertAll(
                () -> assertFalse(SecurityJsonResponses.prefersJson((String) null)),
                () -> assertFalse(SecurityJsonResponses.prefersJson("")),
                () -> assertFalse(SecurityJsonResponses.prefersJson("   ")));
    }

    @Test
    void malformedHeaderFallsBackToRedirect() {
        // A value with no '/' is not a valid media type; negotiation must
        // degrade to the redirect path rather than propagate the parse error.
        assertFalse(SecurityJsonResponses.prefersJson("garbage"));
    }
}
