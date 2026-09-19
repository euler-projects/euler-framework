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

package org.eulerframework.security.web.authentication.appattest;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.common.util.jackson.JacksonUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A filter that exposes the App Attest provider configuration document, a discovery
 * document in the spirit of OpenID Provider Configuration ({@code /.well-known/openid-configuration})
 * but for the Apple App Attest registration surface.
 * <p>
 * The App Attest endpoints are not OAuth endpoints, so they are deliberately published in their own
 * {@code .well-known} document rather than added to the OAuth Authorization Server metadata. The
 * document is anonymous and served on {@code GET}. Like Spring Authorization Server's OIDC provider
 * configuration endpoint, it sets no {@code Cache-Control} header, so the discovery document is
 * cacheable.
 * <p>
 * Response format:
 * <pre>
 * HTTP/1.1 200 OK
 * Content-Type: application/json
 *
 * {"challenge_endpoint": "https://as.example.com/app_attest/challenge",
 *  "registration_endpoint": "https://as.example.com/app_attest/register"}
 * </pre>
 * The endpoint URLs are absolute, derived from the request. A
 * {@link #setProviderConfigurationCustomizer(Consumer) customizer} may add or override claims.
 *
 * @see org.eulerframework.security.config.annotation.web.configurers.appattest.AppAttestProviderConfigurationEndpointConfigurer
 */
public class AppAttestProviderConfigurationEndpointFilter extends OncePerRequestFilter {

    /**
     * The well-known endpoint {@code URI} for the App Attest provider configuration document. Fixed,
     * as befits a {@code .well-known} URI; it is intentionally not configurable.
     */
    public static final String DEFAULT_PROVIDER_CONFIGURATION_ENDPOINT_URI = "/.well-known/app-attest-configuration";

    private final RequestMatcher requestMatcher;
    private final String challengeEndpointUri;
    private final String registrationEndpointUri;

    private Consumer<Map<String, Object>> providerConfigurationCustomizer = (claims) -> {
    };

    /**
     * @param challengeEndpointUri    the {@code URI} of the App Attest challenge endpoint to advertise
     * @param registrationEndpointUri the {@code URI} of the App Attest registration endpoint to advertise
     */
    public AppAttestProviderConfigurationEndpointFilter(String challengeEndpointUri,
                                                        String registrationEndpointUri) {
        Assert.hasText(challengeEndpointUri, "challengeEndpointUri must not be empty");
        Assert.hasText(registrationEndpointUri, "registrationEndpointUri must not be empty");
        this.requestMatcher = PathPatternRequestMatcher.pathPattern(
                HttpMethod.GET, DEFAULT_PROVIDER_CONFIGURATION_ENDPOINT_URI);
        this.challengeEndpointUri = challengeEndpointUri;
        this.registrationEndpointUri = registrationEndpointUri;
    }

    /**
     * Returns the {@link RequestMatcher} for this endpoint.
     *
     * @return the request matcher for the provider configuration endpoint
     */
    public RequestMatcher getRequestMatcher() {
        return this.requestMatcher;
    }

    /**
     * Sets the {@code Consumer} providing access to the claims of the provider configuration
     * document, allowing additional claims to be added or the advertised endpoints to be overridden.
     *
     * @param providerConfigurationCustomizer the claims customizer
     */
    public void setProviderConfigurationCustomizer(Consumer<Map<String, Object>> providerConfigurationCustomizer) {
        Assert.notNull(providerConfigurationCustomizer, "providerConfigurationCustomizer must not be null");
        this.providerConfigurationCustomizer = providerConfigurationCustomizer;
    }

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain) throws ServletException, IOException {
        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String baseUrl = baseUrl(request);

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("challenge_endpoint", baseUrl + this.challengeEndpointUri);
        claims.put("registration_endpoint", baseUrl + this.registrationEndpointUri);
        this.providerConfigurationCustomizer.accept(claims);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // No Cache-Control header, matching Spring Authorization Server's OIDC provider
        // configuration endpoint: a discovery document is meant to be cacheable.

        response.getWriter().write(JacksonUtils.writeValueAsString(claims));
    }

    /**
     * Resolve the absolute base URL (scheme, host, port and context path) of the request, so the
     * advertised endpoint {@code URI}s &mdash; which are relative to the context path &mdash; can be
     * published as absolute URLs.
     */
    private static String baseUrl(HttpServletRequest request) {
        return UriComponentsBuilder.fromUriString(request.getRequestURL().toString())
                .replacePath(request.getContextPath())
                .build()
                .toUriString();
    }
}
