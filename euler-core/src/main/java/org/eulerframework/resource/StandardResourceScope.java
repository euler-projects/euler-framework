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
 * {@link StandardResourceScope} defines a set of well-known visibility scopes that can be associated with
 * a resource. Note that this enum does not enforce any isolation by itself — it is purely a
 * descriptive marker. The actual access control logic must be implemented by the consuming
 * application according to its own requirements.
 */
public enum StandardResourceScope {
    /**
     * Resources with a scope of {@code PUBLIC} are visible to everyone. Anonymous
     * (unauthenticated) access is permissible for this scope, but not required.
     *
     * <p>Visibility level: {@code 400}.
     */
    PUBLIC(400),
    /**
     * Resources with a scope of {@code TENANT} are visible to all authenticated users within the
     * same tenant. Anonymous access is generally not permitted for this scope.
     *
     * <p>Visibility level: {@code 300}.
     */
    TENANT(300),
    /**
     * Resources with a scope of {@code USER} are visible only to the user who created them.
     * Anonymous access is generally not permitted for this scope.
     *
     * <p>Visibility level: {@code 200}.
     */
    USER(200),
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
    PRIVATE(100);

    private final int visibilityLevel;

    StandardResourceScope(int visibilityLevel) {
        this.visibilityLevel = visibilityLevel;
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
}
