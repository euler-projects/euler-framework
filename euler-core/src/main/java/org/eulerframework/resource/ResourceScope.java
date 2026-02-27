/*
 * Copyright 2013-2026 the original author or authors.
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

package org.eulerframework.resource;

import javax.annotation.Nonnull;

/**
 * {@link ResourceScope} represents the visibility scope of a resource. Each scope is associated
 * with a numeric level, accessible via {@link #getLevel()}, where a higher value indicates a
 * broader visibility scope.
 *
 * <p>Several standard scopes are predefined as static constants on this class, each corresponding
 * to one of the named entries in the {@link StandardResourceScope} enumeration.
 *
 * <p>Application-defined scopes may be created via {@link #resolve(int)}, but callers must
 * ensure that the chosen level value does not conflict with any value already reserved by
 * {@link StandardResourceScope}. Furthermore, the relative ordering of level values must remain
 * semantically coherent with respect to the existing standard scopes — for example, a
 * {@code USER_GROUP} scope, which is conceptually broader than an individual user but narrower
 * than a tenant, must be assigned a level that falls strictly between the levels of
 * {@link StandardResourceScope#USER} and {@link StandardResourceScope#TENANT}:
 * <pre>{@code
 * public static final ResourceScope USER_GROUP = ResourceScope.resolve(300);
 * }</pre>
 *
 * <p>Two {@link ResourceScope} instances are considered equal if and only if their level values
 * are identical; {@link #equals(Object)} and {@link #compareTo(ResourceScope)} are both defined
 * in terms of this numeric comparison.
 *
 * @see StandardResourceScope
 */
public class ResourceScope implements Comparable<ResourceScope> {

    /**
     * Pre-initialized singleton for {@link StandardResourceScope#PUBLIC}.
     */
    public static final ResourceScope PUBLIC = new ResourceScope(StandardResourceScope.PUBLIC.intScope());

    /**
     * Pre-initialized singleton for {@link StandardResourceScope#TENANT}.
     */
    public static final ResourceScope TENANT = new ResourceScope(StandardResourceScope.TENANT.intScope());

    /**
     * Pre-initialized singleton for {@link StandardResourceScope#USER}.
     */
    public static final ResourceScope USER = new ResourceScope(StandardResourceScope.USER.intScope());

    /**
     * Pre-initialized singleton for {@link StandardResourceScope#PRIVATE}.
     */
    public static final ResourceScope PRIVATE = new ResourceScope(StandardResourceScope.PRIVATE.intScope());

    /**
     * The set of built-in singleton constants, used by {@link #resolve(int)} to return the
     * canonical instance for any standard numeric visibility level.
     */
    private static final ResourceScope[] WELL_KNOWN = {PUBLIC, TENANT, USER, PRIVATE};

    /**
     * The numeric visibility level of this scope. A higher value indicates a broader visibility scope.
     */
    private final int level;

    /**
     * Creates a new {@code ResourceScope} with the given numeric visibility level.
     *
     * <p>This constructor is intentionally private. Callers should always use {@link #resolve(int)}
     * to obtain a {@code ResourceScope} from a numeric value.
     *
     * @param level the numeric visibility level
     */
    private ResourceScope(int level) {
        this.level = level;
    }

    /**
     * Returns the canonical {@code ResourceScope} instance for the given numeric level.
     *
     * <p>If the value matches one of the predefined constants, the corresponding singleton is returned.
     * For any other value, a new {@code ResourceScope} instance is created and returned.
     *
     * <p>This is the only public way to obtain a {@code ResourceScope} instance and is the
     * preferred method for reconstructing a scope from a stored value (e.g., reading from a
     * database or deserializing from JSON).
     *
     * @param level the numeric visibility level to resolve
     * @return the matching singleton constant, or a new instance for application-defined values
     */
    public static ResourceScope resolve(int level) {
        for (ResourceScope scope : WELL_KNOWN) {
            if (scope.level == level) {
                return scope;
            }
        }
        return new ResourceScope(level);
    }

    /**
     * Returns the numeric visibility level of this scope. A higher value indicates a broader visibility scope.
     *
     * @return the numeric visibility level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns {@code true} if this scope's visibility is strictly broader than that of {@code other}.
     *
     * @param other the scope to compare to
     * @return {@code true} if this scope is broader than {@code other}
     */
    public boolean isBroaderThan(@Nonnull ResourceScope other) {
        return this.level > other.level;
    }

    /**
     * Returns {@code true} if this scope's visibility is strictly narrower than that of {@code other}.
     *
     * @param other the scope to compare to
     * @return {@code true} if this scope is narrower than {@code other}
     */
    public boolean isNarrowerThan(@Nonnull ResourceScope other) {
        return this.level < other.level;
    }

    /**
     * Returns {@code true} if this scope's visibility is the same as or broader than that of
     * {@code other}.
     *
     * @param other the scope to compare to
     * @return {@code true} if this scope is at least as broad as {@code other}
     */
    public boolean isSameOrBroaderThan(@Nonnull ResourceScope other) {
        return this.level >= other.level;
    }

    /**
     * Returns {@code true} if this scope's visibility is the same as or narrower than that of
     * {@code other}.
     *
     * @param other the scope to compare to
     * @return {@code true} if this scope is at most as broad as {@code other}
     */
    public boolean isSameOrNarrowerThan(@Nonnull ResourceScope other) {
        return this.level <= other.level;
    }

    @Override
    public int compareTo(@Nonnull ResourceScope other) {
        if (this == other) {
            return 0;
        }

        return Integer.compare(this.level, other.level);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ResourceScope other)) {
            return false;
        }
        return this.level == other.level;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(level);
    }

    @Override
    public String toString() {
        return "ResourceScope(" + level + ")";
    }
}
