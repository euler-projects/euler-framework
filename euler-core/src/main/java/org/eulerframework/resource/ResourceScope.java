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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
     * The global registry of all interned {@code ResourceScope} instances, keyed by numeric visibility level.
     * Every instance obtained via {@link #resolve(int)} is stored here, ensuring that any two
     * calls with the same level value return the identical object reference.
     */
    private static final ConcurrentMap<Integer, ResourceScope> WELL_KNOWN = new ConcurrentHashMap<>();

    /**
     * Pre-initialized instance for {@link StandardResourceScope#PUBLIC}.
     */
    public static final ResourceScope PUBLIC = resolve(StandardResourceScope.PUBLIC);

    /**
     * Pre-initialized instance for {@link StandardResourceScope#TENANT}.
     */
    public static final ResourceScope TENANT = resolve(StandardResourceScope.TENANT);

    /**
     * Pre-initialized instance for {@link StandardResourceScope#USER}.
     */
    public static final ResourceScope USER = resolve(StandardResourceScope.USER);

    /**
     * Pre-initialized instance for {@link StandardResourceScope#PRIVATE}.
     */
    public static final ResourceScope PRIVATE = resolve(StandardResourceScope.PRIVATE);

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
     * Returns the singleton {@code ResourceScope} instance for the given numeric level.
     *
     * <p>Instances are interned globally: the first call for a given level allocates a new
     * {@code ResourceScope} and registers it in the global registry; subsequent calls with
     * the same level return the previously registered instance. This guarantee holds for
     * both the predefined standard scopes and any application-defined scopes.
     *
     * <p>This is the preferred method for reconstructing a scope from a stored value
     * (e.g., reading from a database or deserializing from JSON).
     *
     * @param level the numeric visibility level to resolve
     * @return the singleton instance for the given level
     */
    @JsonCreator
    @Nonnull
    public static ResourceScope resolve(int level) {
        return WELL_KNOWN.computeIfAbsent(level, ResourceScope::new);
    }

    /**
     * Returns the singleton {@code ResourceScope} instance corresponding to the given
     * {@link StandardResourceScope}.
     *
     * <p>This is a convenience overload of {@link #resolve(int)} and returns the same
     * instance as {@code resolve(standardResourceScope.intScope())}. It is not used as
     * the Jackson deserialization entry point; that role is fulfilled by {@link #resolve(int)}.
     *
     * @param standardResourceScope the standard scope to resolve
     * @return the singleton instance for the corresponding numeric level
     */
    @Nonnull
    public static ResourceScope resolve(@Nonnull StandardResourceScope standardResourceScope) {
        return resolve(standardResourceScope.intScope());
    }

    /**
     * Returns the numeric visibility level of this scope. A higher value indicates a broader visibility scope.
     *
     * @return the numeric visibility level
     */
    @JsonValue
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

    /**
     * Converts this {@link ResourceScope} to the nearest {@link StandardResourceScope},
     * where "nearest" is defined as the {@link StandardResourceScope} whose numeric
     * visibility level is the smallest value that is greater than or equal to the
     * visibility level of this {@link ResourceScope}. In other words, this method
     * performs a ceiling lookup over the ordered set of {@link StandardResourceScope}
     * values with respect to their numeric visibility levels.
     *
     * @throws IllegalArgumentException if the visibility level of this {@link ResourceScope}
     *                                  exceeds the maximum defined {@link StandardResourceScope}
     */
    public StandardResourceScope toStandardResourceScope() {
        for (StandardResourceScope standardResourceScope : StandardResourceScope.values()) {
            if (standardResourceScope.intScope() >= this.level) {
                return standardResourceScope;
            }
        }
        throw new IllegalArgumentException(
                "Visibility level " + this.level + " exceeds the maximum defined StandardResourceScope.");
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
