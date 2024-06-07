package org.eulerframework.security.core.userdetails;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import java.util.List;

public class DelegatingEulerUserDetailsManager extends AbstractEulerUserDetailsManager {
    private List<AbstractEulerUserDetailsManager> eulerUserDetailsManagers;

    @Override
    public boolean support(UserDetails userDetails) {
        for (AbstractEulerUserDetailsManager manager : eulerUserDetailsManagers) {
            if (manager.support(userDetails)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        for (AbstractEulerUserDetailsManager manager : eulerUserDetailsManagers) {
            if (manager.support(user)) {
                return manager.updatePassword(user, newPassword);
            }
        }
        return null;
    }

    @Override
    public void createUser(UserDetails user) {
        Assert.isTrue(!this.userExists(user.getUsername()), "User already exists");
        for (AbstractEulerUserDetailsManager manager : eulerUserDetailsManagers) {
            if (manager.support(user)) {
                manager.createUser(user);
                return;
            }
        }
    }

    @Override
    public void updateUser(UserDetails user) {
        for (AbstractEulerUserDetailsManager manager : eulerUserDetailsManagers) {
            if (manager.support(user)) {
                manager.updateUser(user);
                return;
            }
        }
    }

    @Override
    public void deleteUser(String username) {
        for (AbstractEulerUserDetailsManager manager : eulerUserDetailsManagers) {
            manager.deleteUser(username); // Delete user from all UserDetailsManagers
        }
    }

    @Override
    public boolean userExists(String username) {
        for (AbstractEulerUserDetailsManager manager : eulerUserDetailsManagers) {
            if (manager.userExists(username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        for (AbstractEulerUserDetailsManager manager : eulerUserDetailsManagers) {
            if (manager.userExists(username)) {
                return manager.loadUserByUsername(username);
            }
        }
        throw new UsernameNotFoundException("User '" + username + "' not found");
    }

    public void setEulerUserDetailsManagers(List<AbstractEulerUserDetailsManager> eulerUserDetailsManagers) {
        this.eulerUserDetailsManagers = eulerUserDetailsManagers;
    }
}
