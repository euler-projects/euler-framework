package org.eulerframework.resource;

/**
 * {@link ResourceScope} defines a set of well-known visibility scopes that can be associated with
 * a resource. Note that this class does not enforce any isolation by itself — it is purely a
 * descriptive marker. The actual access control logic must be implemented by the consuming
 * application according to its own requirements.
 *
 * <p>Each instance carries a {@link #visibilityLevel} value that numerically represents its
 * visibility breadth: a higher value means a wider (more permissive) scope. The built-in constants
 * are assigned non-consecutive values so that application-defined scopes can be interleaved by
 * constructing new instances with intermediate levels, without modifying this class.
 *
 * <p><b>Built-in visibility levels:</b>
 * <pre>
 *   PRIVATE  = 100
 *   USER     = 400
 *   TENANT   = 700
 *   PUBLIC   = 1000
 * </pre>
 *
 * <p>This class implements {@link Comparable} based on {@link #visibilityLevel}, so
 * {@code PUBLIC.compareTo(PRIVATE)} returns a positive integer and instances can be used directly
 * in sorted collections or streams without a custom comparator.
 *
 * <p>Two {@code ResourceScope} instances are considered {@link #equals equal} if and only if they
 * have the same {@link #visibilityLevel}.
 *
 * <p>Use {@link #resolve(int)} to obtain a {@code ResourceScope} from a raw visibility level. This
 * method returns the singleton constant for any built-in level, or a new instance for
 * application-defined levels. The standard scopes and their canonical visibility levels are
 * enumerated in {@link StandardResourceScope}.
 */
public class ResourceScope implements Comparable<ResourceScope> {

    /**
     * Resources with a scope of {@code PUBLIC} are visible to everyone. Anonymous
     * (unauthenticated) access is permissible for this scope, but not required.
     *
     * <p>Visibility level: {@code 1000}.
     */
    public static final ResourceScope PUBLIC = new ResourceScope(StandardResourceScope.PUBLIC.getVisibilityLevel());

    /**
     * Resources with a scope of {@code TENANT} are visible to all authenticated users within the
     * same tenant. Anonymous access is generally not permitted for this scope.
     *
     * <p>Visibility level: {@code 700}.
     */
    public static final ResourceScope TENANT = new ResourceScope(StandardResourceScope.TENANT.getVisibilityLevel());

    /**
     * Resources with a scope of {@code USER} are visible only to the user who created them.
     * Anonymous access is generally not permitted for this scope.
     *
     * <p>Visibility level: {@code 400}.
     */
    public static final ResourceScope USER = new ResourceScope(StandardResourceScope.USER.getVisibilityLevel());

    /**
     * Resources with a scope of {@code PRIVATE} are visible only to their owner. This scope is
     * semantically very close to {@code USER} and can often be treated as equivalent; however, in
     * certain user hierarchy designs, the effective visibility of {@code PRIVATE} may be narrower
     * than that of {@code USER}. The exact semantics are left to the implementor's discretion, but
     * under no circumstances should a {@code PRIVATE} resource be accessible to a broader audience
     * than a {@code USER}-scoped resource.
     *
     * <p>Visibility level: {@code 100}.
     */
    public static final ResourceScope PRIVATE = new ResourceScope(StandardResourceScope.PRIVATE.getVisibilityLevel());

    /**
     * The set of built-in singleton constants, used by {@link #resolve(int)} to return the
     * canonical instance for any standard visibility level.
     *
     * <p>This array must be declared <em>after</em> all four constants above so that the static
     * initializer sees their fully constructed values.
     */
    private static final ResourceScope[] WELL_KNOWN = {PUBLIC, TENANT, USER, PRIVATE};

    /**
     * The numeric visibility level of this scope. A higher value indicates a wider (more
     * permissive) visibility range. Values are intentionally non-consecutive to allow
     * application-specific scopes to be inserted between the built-in ones.
     */
    private final int visibilityLevel;

    /**
     * Creates a new {@code ResourceScope} with the given visibility level.
     *
     * <p>This constructor is intentionally private. Instances are created exclusively by this
     * class's own static initializer (for the built-in constants) and by {@link #resolve(int)}
     * (for application-defined levels). Callers should always use {@link #resolve(int)} to obtain
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
