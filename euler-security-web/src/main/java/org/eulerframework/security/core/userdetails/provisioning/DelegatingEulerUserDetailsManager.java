package org.eulerframework.security.core.userdetails.provisioning;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;

public class DelegatingEulerUserDetailsManager extends AbstractEulerUserDetailsManager {
    /**
     * An ordered {@link EulerUserDetailsManager} map which key is each manager's support {@link UserDetails} type.
     * The map's order will affect
     * the loading order of method {@link DelegatingEulerUserDetailsManager#provideUserDetails(String)}
     * and the checking order of method {@link DelegatingEulerUserDetailsManager#userExists(String)}.
     */
    private final LinkedHashMap<Class<? extends UserDetails>, EulerUserDetailsManager> eulerUserDetailsManagers = new LinkedHashMap<>();

    public DelegatingEulerUserDetailsManager(LinkedHashMap<Class<? extends UserDetails>, EulerUserDetailsManager> eulerUserDetailsManagers) {
        Assert.notEmpty(eulerUserDetailsManagers, "eulerUserDetailsManagers must not be empty");
        this.eulerUserDetailsManagers.putAll(eulerUserDetailsManagers);
    }

    @Override
    public UserDetails provideUserDetails(String username) {
        UserDetails userDetails = null;
        for (EulerUserDetailsManager manager : eulerUserDetailsManagers.values()) {
            if ((userDetails = manager.provideUserDetails(username)) != null) {
                break;
            }
        }
        return userDetails;
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        return this.getEulerUserDetailsManager(user.getClass()).updatePassword(user, newPassword);
    }

    @Override
    public void createUser(UserDetails user) {
        Assert.isTrue(!this.userExists(user.getUsername()), "User already exists");
        this.getEulerUserDetailsManager(user.getClass()).createUser(user);
    }

    @Override
    public void updateUser(UserDetails user) {
        this.getEulerUserDetailsManager(user.getClass()).updateUser(user);
    }

    @Override
    public void deleteUser(String username) {
        UserDetails userDetails = this.provideUserDetails(username);
        if (userDetails != null) {
            this.getEulerUserDetailsManager(userDetails.getClass()).deleteUser(username);
        }
    }

    @Override
    public boolean userExists(String username) {
        for (EulerUserDetailsManager manager : eulerUserDetailsManagers.values()) {
            if (manager.userExists(username)) {
                return true;
            }
        }
        return false;
    }

    public void addEulerUserDetailsManager(Class<? extends UserDetails> type, EulerUserDetailsManager eulerUserDetailsManager) {
        Assert.notNull(type, "type must not be null");
        Assert.notNull(eulerUserDetailsManager, "eulerUserDetailsManager must not be null");
        Assert.isTrue(!this.eulerUserDetailsManagers.containsKey(type),
                () -> "EulerUserDetailsManager for user details type '" + type + "' already exists");

        this.eulerUserDetailsManagers.put(type, eulerUserDetailsManager);
    }

    private EulerUserDetailsManager getEulerUserDetailsManager(Class<? extends UserDetails> clazz) {
        EulerUserDetailsManager manager = eulerUserDetailsManagers.get(clazz);
        if (manager == null) {
            throw new IllegalArgumentException("EulerUserDetailsManager for user details type is '" + clazz + "' not found");
        }
        return manager;
    }
}
