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
package org.eulerframework.security.authentication.factor;

import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Composite {@link UserAuthenticationService} that fans out to a set of
 * factor-specific services registered as Spring beans.
 * <p>
 * Naming and design follow Spring's {@code DelegatingFilterProxy} /
 * {@code DelegatingPasswordEncoder} pattern: the framework operates against
 * a single {@code UserAuthenticationService} entry-point — typically wired
 * into the {@code /user/identities} endpoint filter — while business code
 * registers as many backing services as needed (one per factor type).
 * <p>
 * Routing rules:
 * <ul>
 *     <li>{@link #bind} looks at {@code params.getFirst("factor_type")} and
 *         dispatches to the matching service. Missing or unknown values
 *         raise {@link UnsupportedFactorTypeException}.</li>
 *     <li>{@link #findById} performs a <em>short-circuit fan-out</em>:
 *         services are queried in registration order and the first
 *         non-empty result wins. {@code Optional.empty()} is returned when
 *         every service comes back empty.</li>
 *     <li>{@link #findAllByUserId} concatenates the per-service results in
 *         registration order. Callers that need a deterministic ordering
 *         must sort the aggregated list themselves.</li>
 *     <li>{@link #deleteById} fan-outs the call to every service. Each
 *         service returns silently when it does not own the factor (per
 *         the {@link UserAuthenticationService#deleteById} contract); if
 *         the factor existed before the call, the owning service will have
 *         removed it. To distinguish "deleted" from "never existed" the
 *         delegating service performs an upfront {@link #findById} probe
 *         and raises {@link UserAuthenticationFactorNotFoundException} when
 *         no service can locate the factor.</li>
 * </ul>
 * <p>
 * The delegating service itself implements {@link UserAuthenticationService}
 * and reports a reserved {@link #DELEGATING_FACTOR_TYPE} from
 * {@link #factorType()}; it never participates in {@code bind} routing.
 */
public class DelegatingUserAuthenticationService implements UserAuthenticationService {

    /**
     * Reserved factor-type sentinel returned by {@link #factorType()}. It
     * does not match any client-supplied {@code factor_type} value and is
     * not eligible for {@code bind} routing.
     */
    public static final String DELEGATING_FACTOR_TYPE = "__delegating__";

    /**
     * Form parameter name carrying the target factor type during
     * {@code POST /user/identities}.
     */
    public static final String FACTOR_TYPE_PARAMETER = "factor_type";

    private final List<UserAuthenticationService> services;
    private final Map<String, UserAuthenticationService> routes;

    /**
     * Create a delegator that fans out to the given factor-specific
     * services. The collection is copied; ordering of the supplied
     * collection is preserved and used by the short-circuit fan-out and
     * list aggregation logic.
     *
     * @param services factor-specific services to delegate to, must not
     *                 contain {@code null}, must not contain
     *                 {@link DelegatingUserAuthenticationService} instances
     *                 (to avoid recursion) and must not contain two
     *                 services with the same {@link #factorType()}
     */
    public DelegatingUserAuthenticationService(Collection<? extends UserAuthenticationService> services) {
        Assert.notNull(services, "services must not be null");
        List<UserAuthenticationService> copy = new ArrayList<>(services.size());
        Map<String, UserAuthenticationService> routeMap = new LinkedHashMap<>(services.size());
        for (UserAuthenticationService service : services) {
            Assert.notNull(service, "services must not contain null");
            Assert.isTrue(!(service instanceof DelegatingUserAuthenticationService),
                    "DelegatingUserAuthenticationService must not delegate to another " +
                            "DelegatingUserAuthenticationService");
            String factorType = service.factorType();
            Assert.hasText(factorType, "factorType must not be empty for service " + service.getClass().getName());
            Assert.isTrue(!DELEGATING_FACTOR_TYPE.equals(factorType),
                    "factorType '" + DELEGATING_FACTOR_TYPE + "' is reserved");
            UserAuthenticationService previous = routeMap.putIfAbsent(factorType, service);
            Assert.isNull(previous, () -> "Duplicate UserAuthenticationService bean for factorType '"
                    + factorType + "': " + (previous != null ? previous.getClass().getName() : "")
                    + " and " + service.getClass().getName());
            copy.add(service);
        }
        this.services = Collections.unmodifiableList(copy);
        this.routes = Collections.unmodifiableMap(routeMap);
    }

    @Override
    public String factorType() {
        return DELEGATING_FACTOR_TYPE;
    }

    @Override
    public UserAuthenticationFactor bind(String userId, MultiValueMap<String, String> params) {
        Assert.notNull(params, "params must not be null");
        String factorType = params.getFirst(FACTOR_TYPE_PARAMETER);
        if (factorType == null || factorType.isBlank()) {
            throw new InvalidAuthenticationFactorRequestException(
                    "Form parameter '" + FACTOR_TYPE_PARAMETER + "' is required");
        }
        UserAuthenticationService target = this.routes.get(factorType);
        if (target == null) {
            throw new UnsupportedFactorTypeException(factorType);
        }
        return target.bind(userId, params);
    }

    @Override
    public Optional<UserAuthenticationFactor> findById(String userId, String id) {
        for (UserAuthenticationService service : this.services) {
            Optional<UserAuthenticationFactor> hit = service.findById(userId, id);
            if (hit.isPresent()) {
                return hit;
            }
        }
        return Optional.empty();
    }

    @Override
    public List<UserAuthenticationFactor> findAllByUserId(String userId) {
        List<UserAuthenticationFactor> aggregated = new ArrayList<>();
        for (UserAuthenticationService service : this.services) {
            List<UserAuthenticationFactor> partial = service.findAllByUserId(userId);
            if (partial != null && !partial.isEmpty()) {
                aggregated.addAll(partial);
            }
        }
        return aggregated;
    }

    @Override
    public void deleteById(String userId, String id) {
        // Probe first so that "factor does not exist" is observable from the
        // outside — every service deleteById is silent when it does not own
        // the factor.
        if (findById(userId, id).isEmpty()) {
            throw new UserAuthenticationFactorNotFoundException(id);
        }
        for (UserAuthenticationService service : this.services) {
            service.deleteById(userId, id);
        }
    }

    /**
     * Returns the set of factor types currently routed by this delegator,
     * in registration order. Useful for debugging and metrics.
     */
    public List<String> getRegisteredFactorTypes() {
        return List.copyOf(this.routes.keySet());
    }
}
