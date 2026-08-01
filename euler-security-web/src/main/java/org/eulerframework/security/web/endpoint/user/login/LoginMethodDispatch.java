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
package org.eulerframework.security.web.endpoint.user.login;

import org.springframework.util.Assert;

/**
 * Immutable result of {@link LoginMethodHandler#dispatch} describing how
 * the routing filter should respond to a POST at the unified
 * login-method dispatch entry point.
 *
 * <p>Three outcomes:
 * <ul>
 *   <li>{@link #redirect(String)} — 302 Found (the entry point that
 *       collects this method's input)</li>
 *   <li>{@link #redirectPreservingMethod(String)} — 307 Temporary
 *       Redirect (preserves POST method and body, e.g. password form
 *       forwarded to the formLogin processing URL)</li>
 *   <li>{@link #notImplemented()} — 501 Not Implemented (placeholder
 *       for unfinished features)</li>
 * </ul>
 */
public final class LoginMethodDispatch {

    public enum Action {
        REDIRECT_302,
        REDIRECT_307,
        NOT_IMPLEMENTED
    }

    private final Action action;
    private final String location;

    private LoginMethodDispatch(Action action, String location) {
        this.action = action;
        this.location = location;
    }

    /**
     * 302 Found redirect. The client will issue a GET to
     * {@code location}.
     */
    public static LoginMethodDispatch redirect(String location) {
        Assert.hasText(location, "location must not be empty");
        return new LoginMethodDispatch(Action.REDIRECT_302, location);
    }

    /**
     * 307 Temporary Redirect preserving the original POST method and
     * request body. Used to hand off form credentials to a different
     * processing endpoint without re-submission.
     */
    public static LoginMethodDispatch redirectPreservingMethod(String location) {
        Assert.hasText(location, "location must not be empty");
        return new LoginMethodDispatch(Action.REDIRECT_307, location);
    }

    /**
     * 501 Not Implemented. Signals that the dispatch target is not yet
     * available (placeholder for future implementation).
     */
    public static LoginMethodDispatch notImplemented() {
        return new LoginMethodDispatch(Action.NOT_IMPLEMENTED, null);
    }

    public Action getAction() {
        return this.action;
    }

    public String getLocation() {
        return this.location;
    }
}
