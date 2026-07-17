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
package org.eulerframework.security.oauth2.client.authentication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.common.util.StringUtils;
import org.eulerframework.security.core.EulerUser;
import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.core.identity.UserIdentity;
import org.eulerframework.security.core.identity.UserIdentityService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.core.userdetails.RandomUsernameGenerator;
import org.eulerframework.security.core.userdetails.UserDetailsNotFoundException;
import org.eulerframework.security.util.UserDetailsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link AuthenticationSuccessHandler} that promotes the OIDC / OAuth2
 * user placed in the security context by {@code oauth2Login()} into a
 * local {@link EulerUserDetails} principal backed by the application's
 * {@link EulerUserService} and {@link UserIdentityService}.
 *
 * <p>The promotion is required so that downstream Authorization Server
 * flows (e.g. this application acting as an OP for its own clients)
 * see the local {@code userId} as the token subject rather than the
 * upstream IdP-issued {@code sub}.
 *
 * <h2>Flow</h2>
 * <ol>
 *   <li>Extract {@code registrationId} from the incoming
 *       {@link OAuth2AuthenticationToken} and look up its
 *       {@link PerRegistrationLoginPolicy}. Registrations without a
 *       policy fall back to the injected {@link #fallbackPolicy}
 *       (identity-type = registrationId, auto-create disabled).</li>
 *   <li>Look the local user up via
 *       {@link UserIdentityService#findUserIdentityByRawSubject(String, String)}
 *       using the policy's {@code identityType} and the OAuth2
 *       principal's {@link OAuth2User#getName() name} (equals the
 *       IdP-issued {@code sub} for standard providers).</li>
 *   <li>On miss and when the policy's
 *       {@link PerRegistrationLoginPolicy#isAutoCreateUser()} is
 *       {@code true}: mint an opaque local username via
 *       {@link RandomUsernameGenerator}, persist the user with the
 *       policy's {@code defaultAuthorities} and an unusable
 *       {@code {noop}} password, then persist the identity binding
 *       carrying the OIDC profile attributes as extension properties.</li>
 *   <li>Rewrite the {@link SecurityContext} with a new
 *       {@link UsernamePasswordAuthenticationToken} whose principal is
 *       the {@link EulerUserDetails} projection of the local user, so
 *       that subsequent authorization filters see the local identity.</li>
 *   <li>Delegate to
 *       {@link SavedRequestAwareAuthenticationSuccessHandler} for the
 *       actual redirect, honouring the configured
 *       {@code targetUrlParameter} (typically the shared
 *       {@code loginSuccessRedirectParameter}).</li>
 * </ol>
 *
 * <p>When the OAuth2 principal is not an {@link OidcUser} the handler
 * still promotes based on {@link OAuth2User#getAttributes()} but the
 * OIDC-specific extension keys ({@code email}, {@code name},
 * {@code picture}, {@code locale}) are best-effort.
 */
public class OAuth2LoginPrincipalPromotingSuccessHandler
        implements AuthenticationSuccessHandler {

    /** Standard OIDC / OAuth2 attribute keys copied into the identity prototype. */
    public static final String ATTR_SUB = "sub";
    public static final String ATTR_EMAIL = "email";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_PICTURE = "picture";
    public static final String ATTR_LOCALE = "locale";

    private static final int RANDOM_PASSWORD_LENGTH = 32;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final EulerUserService userService;
    private final UserIdentityService userIdentityService;
    private final SavedRequestAwareAuthenticationSuccessHandler delegate =
            new SavedRequestAwareAuthenticationSuccessHandler();

    private SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    private Map<String, PerRegistrationLoginPolicy> policiesByRegistrationId = Map.of();

    public OAuth2LoginPrincipalPromotingSuccessHandler(
            EulerUserService userService,
            UserIdentityService userIdentityService) {
        Assert.notNull(userService, "userService is required");
        Assert.notNull(userIdentityService, "userIdentityService is required");
        this.userService = userService;
        this.userIdentityService = userIdentityService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        Assert.isInstanceOf(OAuth2AuthenticationToken.class, authentication,
                () -> "Only OAuth2AuthenticationToken is supported, got "
                        + authentication.getClass().getName());
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String registrationId = token.getAuthorizedClientRegistrationId();
        PerRegistrationLoginPolicy policy = resolvePolicy(registrationId);
        OAuth2User principal = token.getPrincipal();
        String rawSubject = principal.getName();
        Assert.hasText(rawSubject, "OAuth2User#getName() must not be empty");

        EulerUser localUser = this.userIdentityService
                .findUserIdentityByRawSubject(policy.getIdentityType(), rawSubject)
                .map(UserIdentity::getUserId)
                .map(this.userService::loadUserById)
                .orElseGet(() -> autoProvisionOrThrow(policy, principal));

        EulerUserDetails userDetails = UserDetailsUtils.toEulerUserDetails(localUser);

        UsernamePasswordAuthenticationToken promoted = UsernamePasswordAuthenticationToken.authenticated(
                userDetails, token.getCredentials(), userDetails.getAuthorities());
        promoted.setDetails(token.getDetails());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(promoted);
        SecurityContextHolder.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);

        this.logger.debug("Promoted OAuth2 principal from '{}' (sub={}) to local userDetails (userId={})",
                registrationId, rawSubject, userDetails.getUserId());

        this.delegate.onAuthenticationSuccess(request, response, promoted);
    }

    /**
     * Resolve the policy applied to {@code registrationId}. When no
     * explicit entry is configured, synthesize a permissive fallback
     * with {@code autoCreateUser=false} and
     * {@code identityType=registrationId} so that missing configuration
     * degrades to "recognise known users, reject unknown ones" rather
     * than to a NullPointerException.
     */
    private PerRegistrationLoginPolicy resolvePolicy(String registrationId) {
        PerRegistrationLoginPolicy policy = this.policiesByRegistrationId.get(registrationId);
        if (policy != null) {
            return policy;
        }
        this.logger.debug("No PerRegistrationLoginPolicy configured for registrationId='{}'; "
                + "falling back to identityType={} with autoCreateUser=false.",
                registrationId, registrationId);
        return new PerRegistrationLoginPolicy(false, List.of(), registrationId);
    }

    private EulerUser autoProvisionOrThrow(PerRegistrationLoginPolicy policy, OAuth2User principal) {
        if (!policy.isAutoCreateUser()) {
            throw new UserDetailsNotFoundException(principal.getName());
        }
        Map<String, Object> attributes = principal.getAttributes();
        EulerUserDetails seed = EulerUserDetails.builder()
                .username(RandomUsernameGenerator.generate())
                .password("{noop}" + StringUtils.randomString(RANDOM_PASSWORD_LENGTH))
                .authorities(policy.getDefaultAuthorities().toArray(new String[0]))
                .build();
        EulerUser createdUser = this.userService.createUser(seed);
        String userId = createdUser.getUserId();
        Assert.hasText(userId, "userService.createUser must return a persisted user carrying a userId");

        logger.debug("{}'s all attributes: {}", userId, attributes);

        Map<String, Object> extensions = new LinkedHashMap<>();
        // Preserve the raw IdP-issued subject on the prototype so that
        // per-type backends whose subject derivation is non-trivial can
        // still recover it; for identity types whose subject is the
        // identity function (google / apple / wechat) the extension is
        // redundant but harmless.
        extensions.put(ATTR_SUB, principal.getName());
        copyStringAttribute(attributes, ATTR_EMAIL, extensions);
        copyStringAttribute(attributes, ATTR_NAME, extensions);
        copyStringAttribute(attributes, ATTR_PICTURE, extensions);
        copyStringAttribute(attributes, ATTR_LOCALE, extensions);

        UserIdentity prototype = UserIdentity.withExtensions(extensions)
                .identityType(policy.getIdentityType())
                .build();
        this.userIdentityService.createUserIdentity(userId, prototype);
        return this.userService.loadUserById(userId);
    }

    private static void copyStringAttribute(Map<String, Object> attributes,
                                            String key,
                                            Map<String, Object> target) {
        if (attributes == null) {
            return;
        }
        Object value = attributes.get(key);
        if (value instanceof String stringValue && !stringValue.isEmpty()) {
            target.put(key, stringValue);
        }
    }

    /** Configure the {@code targetUrlParameter} used to honour original {@code returnUrl} parameters. */
    public void setTargetUrlParameter(String targetUrlParameter) {
        this.delegate.setTargetUrlParameter(targetUrlParameter);
    }

    /** Configure the fallback URL when no saved request nor target parameter is present. */
    public void setDefaultTargetUrl(String defaultTargetUrl) {
        this.delegate.setDefaultTargetUrl(defaultTargetUrl);
    }

    /**
     * Configure the per-{@code registrationId} policy map. Typically
     * assembled from
     * {@code euler.security.web.login-methods.*} entries of {@code type=oauth2}
     * by the autoconfigure layer.
     */
    public void setPoliciesByRegistrationId(
            Map<String, PerRegistrationLoginPolicy> policiesByRegistrationId) {
        this.policiesByRegistrationId = policiesByRegistrationId == null
                ? Map.of() : Map.copyOf(policiesByRegistrationId);
    }

    /**
     * Override the {@link SecurityContextRepository} used to persist the
     * promoted authentication. Defaults to
     * {@link HttpSessionSecurityContextRepository}, which is what
     * Spring Security's own oauth2Login default success handler uses.
     */
    public void setSecurityContextRepository(SecurityContextRepository securityContextRepository) {
        Assert.notNull(securityContextRepository, "securityContextRepository is required");
        this.securityContextRepository = securityContextRepository;
    }
}
