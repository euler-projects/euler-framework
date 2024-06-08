package org.eulerframework.security.core;

import org.eulerframework.security.core.userdetails.EulerUserDetails;

public interface EulerUserService {

    void createUser(EulerUserDetails userDetails);

    void updateUser(EulerUserDetails userDetails);

    void deleteUser(String userId);

    EulerUserDetails updatePassword(String userId, String newPassword);
}
