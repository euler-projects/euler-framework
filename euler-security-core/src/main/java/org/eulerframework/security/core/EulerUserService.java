/*
 * Copyright 2013-2024 the original author or authors.
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
package org.eulerframework.security.core;

import org.eulerframework.security.core.userdetails.EulerUserDetails;

import java.util.List;

public interface EulerUserService {
    EulerUser createUser(EulerUserDetails userDetails);

    EulerUser createUser(EulerUser eulerUser);

    EulerUser loadUserById(String userId);

    EulerUser loadUserByUsername(String username);

    List<EulerUser> listUsers(int offset, int limit);

    void updateUser(EulerUser eulerUser);

    void updatePassword(String userId, String newPassword);

    void disableUser(String userId);

    void deleteUser(String userId);
}
