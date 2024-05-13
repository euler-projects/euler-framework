package org.eulerframework.security.core.userdetails;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UserDetails;

public interface EulerUserDetails extends UserDetails, CredentialsContainer {
    /**
     * Returns the unique user id of the user
     */
    String getUserId();

    /**
     * Returns the email of the user.
     */
    String getEmail();

    /**
     * Returns the phone number of the user
     */
    String getPhone();
}
