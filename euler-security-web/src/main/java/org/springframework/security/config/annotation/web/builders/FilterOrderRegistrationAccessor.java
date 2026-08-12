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
package org.springframework.security.config.annotation.web.builders;

import jakarta.servlet.Filter;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * Registers a custom filter class in the {@link HttpSecurity} filter order
 * registry so that it can be added through
 * {@link HttpSecurity#addFilter(Filter)}, which rejects filter classes that
 * have no registered order.
 * <p>
 * The order is derived from a registered anchor filter plus an offset,
 * mirroring {@link HttpSecurity#addFilterBefore(Filter, Class)} and
 * {@link HttpSecurity#addFilterAfter(Filter, Class)}, which perform the same
 * registration as a side effect of adding a filter.
 */
public final class FilterOrderRegistrationAccessor {

    /** Name of the private registry field on {@link HttpSecurity}. */
    private static final String FILTER_ORDERS_FIELD = "filterOrders";

    private FilterOrderRegistrationAccessor() {
    }

    /**
     * Register {@code filter} in the order registry of {@code http} at the
     * order of {@code anchorFilter} plus {@code offset}.
     *
     * @param http         the builder whose registry is updated
     * @param filter       the filter class to register
     * @param anchorFilter a filter class that already has a registered order
     * @param offset       offset applied to the anchor's order, e.g.
     *                     {@code -1} or {@code 1}
     * @throws IllegalArgumentException when the anchor has no registered order
     */
    public static void register(HttpSecurityBuilder<?> http, Class<? extends Filter> filter,
                                Class<? extends Filter> anchorFilter, int offset) {
        Assert.notNull(http, "http must not be null");
        Assert.notNull(filter, "filter must not be null");
        Assert.notNull(anchorFilter, "anchorFilter must not be null");

        FilterOrderRegistration registry = getFilterOrders(http);
        Integer anchorOrder = registry.getOrder(anchorFilter);
        if (anchorOrder == null) {
            throw new IllegalArgumentException("The anchor filter class " + anchorFilter.getName()
                    + " does not have a registered order");
        }
        registry.put(filter, anchorOrder + offset);
    }

    private static FilterOrderRegistration getFilterOrders(HttpSecurityBuilder<?> http) {
        Field field = ReflectionUtils.findField(http.getClass(), FILTER_ORDERS_FIELD);
        if (field == null) {
            throw new IllegalStateException("No '" + FILTER_ORDERS_FIELD + "' field found on "
                    + http.getClass().getName());
        }
        ReflectionUtils.makeAccessible(field);
        return (FilterOrderRegistration) ReflectionUtils.getField(field, http);
    }
}
