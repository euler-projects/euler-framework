package org.eulerframework.resource;

/**
 * {@link ResourceScope} defines a set of well-known visibility scopes that can be associated with
 * a resource. Note that this enum does not enforce any isolation by itself — it is purely a
 * descriptive marker. The actual access control logic must be implemented by the consuming
 * application according to its own requirements.
 */
public enum ResourceScope {
    /**
     * Resources with a scope of {@code PUBLIC} are visible to everyone. Anonymous
     * (unauthenticated) access is permissible for this scope, but not required.
     */
    PUBLIC,
    /**
     * Resources with a scope of {@code TENANT} are visible to all authenticated users within the
     * same tenant. Anonymous access is generally not permitted for this scope.
     */
    TENANT,
    /**
     * Resources with a scope of {@code USER} are visible only to the user who created them.
     * Anonymous access is generally not permitted for this scope.
     */
    USER,
    /**
     * Resources with a scope of {@code PRIVATE} are visible only to their owner. This scope is
     * semantically very close to {@code USER} and can often be treated as equivalent; however, in
     * certain user hierarchy designs, the effective visibility of {@code PRIVATE} may be narrower
     * than that of {@code USER}. The exact semantics are left to the implementor's discretion, but
     * under no circumstances should a {@code PRIVATE} resource be accessible to a broader audience
     * than a {@code USER}-scoped resource.
     */
    PRIVATE
}
