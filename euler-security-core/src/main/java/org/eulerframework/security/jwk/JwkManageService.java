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
package org.eulerframework.security.jwk;

import java.util.List;

public interface JwkManageService {

    ManagedJwk createJwk(JwkEntry entry);

    ManagedJwk createJwk(ManagedJwk managedJwk);

    ManagedJwk getJwk(String kid);

    List<ManagedJwk> listJwks();

    void updateJwk(JwkEntry entry);

    void updateJwk(ManagedJwk managedJwk);

    void patchJwk(ManagedJwk managedJwk);

    void deleteJwk(String kid);

    String getFingerprint();
}
