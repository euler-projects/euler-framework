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

import jakarta.servlet.http.HttpServletRequest;

/**
 * SPI that projects a {@link RegisteredLoginMethod} into a publishable
 * {@link LoginMethod} and handles dispatch when the login-method
 * dispatch endpoint receives a POST selecting that method.
 *
 * <p>Implementations are registered as Spring beans, one per
 * {@code type} value (e.g. {@code password}, {@code oauth2},
 * {@code otp}, {@code passkey}); a generic dispatcher delegates each
 * registration to the implementation whose {@link #type()} matches.
 *
 * <p>This SPI constitutes a frontend-agnostic backend contract.
 * The bundled server-rendered login page is a reference consumer;
 * clients may ignore the resolved submission targets and route
 * independently.
 */
public interface LoginMethodHandler {

    /**
     * The stable identifier used as the {@code method-type} value in
     * YAML (e.g. {@code "password"}, {@code "oauth2"}), matched
     * case-sensitively.
     */
    String type();

    /**
     * Derives the default name for a registration that does not declare
     * {@code method-name}. Names identify a method towards clients (the
     * dispatch method parameter) and must be unique across all
     * registrations; duplicates are rejected at aggregation time.
     *
     * <p>The default derivation is the {@link #type()} value, which is
     * only unique while a type has a single registration.
     * Implementations supporting several registrations per type should
     * derive from their distinguishing semantics (e.g. the OAuth2
     * provider, the OTP channel). Returns {@code null} when nothing can
     * be derived; such registrations are skipped.
     *
     * @param method the registration to name
     * @return the derived name, or {@code null} if underivable
     */
    default String resolveName(RegisteredLoginMethod method) {
        return type();
    }

    /**
     * Projects the registration named {@code name} into a publishable
     * login method.
     *
     * <p>Returns {@code null} when the registration cannot currently be
     * served (e.g. it references an unknown OAuth2 client
     * registration); such entries are skipped and never offered to
     * clients.
     *
     * @param name   the effective method name (declared
     *               {@code method-name}, or the value of
     *               {@link #resolveName})
     * @param method the registration to project
     * @return the publishable login method, or {@code null} to skip
     */
    LoginMethod describe(String name, RegisteredLoginMethod method);

    /**
     * Determines the dispatch action when a POST arrives at the
     * login-method dispatch endpoint selecting this method.
     *
     * <p>The endpoint carries two intents that implementations are
     * expected to tell apart by inspecting what the request already
     * holds:
     * <ul>
     *   <li><b>the input this method needs is complete</b> &mdash; the
     *       caller is submitting. Forward it, typically by replaying the
     *       POST to the endpoint that consumes it
     *       ({@link LoginMethodDispatch#redirectPreservingMethod}, 307).
     *       A client that renders this method's form up front therefore
     *       completes in a single hop, rather than being bounced to a
     *       screen it has already shown.</li>
     *   <li><b>the input is incomplete</b> &mdash; the caller is only
     *       asking for this method (say an alternate-method button
     *       beside another method's form). Answer 302 with the entry
     *       point that collects the method's input
     *       ({@link LoginMethodDispatch#redirect}).</li>
     * </ul>
     * Methods needing no input at all, such as a federated redirect,
     * treat every selection as a submission.
     *
     * <p>The redirect target is that entry point plainly, carrying no
     * progress of its own: implementations must not diagnose which field
     * is missing, nor resume a partially filled flow from the step it
     * stopped at. Walking the user through the fields belongs to the
     * client; keeping it out here is what holds this SPI down to
     * "forward, or hand back the collection screen".
     *
     * <p>Implementations must not write to the response directly;
     * instead return a {@link LoginMethodDispatch} that the routing
     * filter will execute.
     *
     * @param name    the effective method name (from the method
     *                parameter)
     * @param method  the registration
     * @param request the current servlet request (carries form params)
     * @return the dispatch action
     */
    LoginMethodDispatch dispatch(String name, RegisteredLoginMethod method, HttpServletRequest request);
}
