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

/**
 * Represents the visibility scope of a resource and defines a total ordering over all scopes
 * based on visibility breadth. A higher {@link #visibilityLevel} indicates a wider (more
 * permissive) scope — {@code PUBLIC.compareTo(PRIVATE)} therefore yields a positive integer —
 * so instances can be placed directly into sorted collections, used as {@code TreeMap} keys, or
 * compared with the predicate methods {@link #isWiderThan}, {@link #isNarrowerThan}, and their
 * inclusive counterparts.
 *
 * <p>The four standard scopes — {@link #PUBLIC}, {@link #TENANT}, {@link #USER}, and
 * {@link #PRIVATE} — are provided as static constants whose visibility levels are defined by
 * {@link StandardResourceScope}. Application code that requires an intermediate scope may define
 * one via {@link #resolve(int)}, using any level value not already occupied by a standard
 * constant:
 * <pre>
 *   public static final ResourceScope GROUP = ResourceScope.resolve(250);
 * </pre>
 *
 * <p>Two instances are {@linkplain #equals equal} if and only if they share the same
 * {@link #visibilityLevel}. The four standard constants are singletons, so identity ({@code ==})
 * comparison is safe when dealing exclusively with standard scopes.
 *
 * <p>Note that {@code ResourceScope} does not enforce any access isolation by itself — it is a
 * descriptive marker only. All access control decisions based on scope values remain the
 * responsibility of the consuming application.
 */
public class ResourceScope implements Comparable<ResourceScope> {

    /**
     * Pre-initialized singleton for {@link StandardResourceScope#PUBLIC}.
     */
    public static final ResourceScope PUBLIC = new ResourceScope(StandardResourceScope.PUBLIC.getVisibilityLevel());

    /**
     * Pre-initialized singleton for {@link StandardResourceScope#TENANT}.
     */
    public static final ResourceScope TENANT = new ResourceScope(StandardResourceScope.TENANT.getVisibilityLevel());

    /**
     * Pre-initialized singleton for {@link StandardResourceScope#USER}.
     */
    public static final ResourceScope USER = new ResourceScope(StandardResourceScope.USER.getVisibilityLevel());

    /**
     * Pre-initialized singleton for {@link StandardResourceScope#PRIVATE}.
     */
    public static final ResourceScope PRIVATE = new ResourceScope(StandardResourceScope.PRIVATE.getVisibilityLevel());

    /**
     * The set of built-in singleton constants, used by {@link #resolve(int)} to return the
     * canonical instance for any standard visibility level.
     */
    private static final ResourceScope[] WELL_KNOWN = {PUBLIC, TENANT, USER, PRIVATE};

    /**
     * The numeric visibility level of this scope. A higher value indicates a wider (more
     * permissive) visibility range.
     */
    private final int visibilityLevel;

    /**
     * Creates a new {@code ResourceScope} with the given visibility level.
     *
     * <p>This constructor is intentionally private. Callers should always use {@link #resolve(int)} to obtain
     * a {@code ResourceScope} from a numeric level.
     *
     * @param visibilityLevel the numeric visibility level
     */
    private ResourceScope(int visibilityLevel) {
        this.visibilityLevel = visibilityLevel;
    }

    /**
     * Returns the canonical {@code ResourceScope} instance for the given visibility level.
     *
     * <p>If the level matches one of the built-in constants ({@link #PUBLIC}, {@link #TENANT},
     * {@link #USER}, {@link #PRIVATE}), the corresponding singleton is returned. For any other
     * level a new {@code ResourceScope} instance is created and returned.
     *
     * <p>This is the only public way to obtain a {@code ResourceScope} instance and is the
     * preferred method for reconstructing a scope from a stored value (e.g. reading from a
     * database or deserializing from JSON). Application-defined scopes should be declared as
     * static constants resolved through this method:
     * <pre>
     *   public static final ResourceScope GROUP = ResourceScope.resolve(550);
     * </pre>
     *
     * @param visibilityLevel the numeric visibility level to resolve
     * @return the matching singleton constant, or a new instance for application-defined levels
     */
    public static ResourceScope resolve(int visibilityLevel) {
        for (ResourceScope scope : WELL_KNOWN) {
            if (scope.visibilityLevel == visibilityLevel) {
                return scope;
            }
        }
        return new ResourceScope(visibilityLevel);
    }

    /**
     * Returns the numeric visibility level of this scope. A higher value means a wider (more
     * permissive) visibility range.
     *
     * @return the visibility level
     */
    public int getVisibilityLevel() {
        return visibilityLevel;
    }

    /**
     * Compares this scope to {@code other} by {@link #visibilityLevel}.
     *
     * <p>Returns a negative integer if this scope is narrower than {@code other}, zero if they
     * have the same visibility level, or a positive integer if this scope is wider.
     *
     * @param other the scope to compare to; must not be {@code null}
     * @return a negative integer, zero, or a positive integer as this scope's visibility level is
     *         less than, equal to, or greater than {@code other}'s visibility level
     */
    @Override
    public int compareTo(ResourceScope other) {
        return Integer.compare(this.visibilityLevel, other.visibilityLevel);
    }

    /**
     * Returns {@code true} if this scope's visibility is strictly wider (more permissive) than
     * that of {@code other}.
     *
     * @param other the scope to compare to; must not be {@code null}
     * @return {@code true} if this scope is wider than {@code other}
     */
    public boolean isWiderThan(ResourceScope other) {
        return this.visibilityLevel > other.visibilityLevel;
    }

    /**
     * Returns {@code true} if this scope's visibility is strictly narrower (more restrictive) than
     * that of {@code other}.
     *
     * @param other the scope to compare to; must not be {@code null}
     * @return {@code true} if this scope is narrower than {@code other}
     */
    public boolean isNarrowerThan(ResourceScope other) {
        return this.visibilityLevel < other.visibilityLevel;
    }

    /**
     * Returns {@code true} if this scope's visibility is the same as or wider than that of
     * {@code other}.
     *
     * @param other the scope to compare to; must not be {@code null}
     * @return {@code true} if this scope is at least as wide as {@code other}
     */
    public boolean isSameOrWiderThan(ResourceScope other) {
        return this.visibilityLevel >= other.visibilityLevel;
    }

    /**
     * Returns {@code true} if this scope's visibility is the same as or narrower than that of
     * {@code other}.
     *
     * @param other the scope to compare to; must not be {@code null}
     * @return {@code true} if this scope is at most as wide as {@code other}
     */
    public boolean isSameOrNarrowerThan(ResourceScope other) {
        return this.visibilityLevel <= other.visibilityLevel;
    }

    /**
     * Returns {@code true} if {@code obj} is a {@code ResourceScope} with the same
     * {@link #visibilityLevel} as this instance.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ResourceScope)) {
            return false;
        }
        return this.visibilityLevel == ((ResourceScope) obj).visibilityLevel;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(visibilityLevel);
    }

    /**
     * Returns a string representation of this scope in the form {@code ResourceScope(visibilityLevel)}.
     */
    @Override
    public String toString() {
        return "ResourceScope(" + visibilityLevel + ")";
    }
}
