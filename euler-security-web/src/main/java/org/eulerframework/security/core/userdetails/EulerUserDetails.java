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
package org.eulerframework.security.core.userdetails;

import org.eulerframework.security.util.UserDetailsUtils;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;

public final class EulerUserDetails implements UserDetails, CredentialsContainer {
    private String userId;

    private String password;

    private final String username;

    private final Set<GrantedAuthority> authorities;

    private final boolean accountNonExpired;

    private final boolean accountNonLocked;

    private final boolean credentialsNonExpired;

    private final boolean enabled;

    /**
     * Calls the more complex constructor with all boolean arguments set to {@code true}.
     */
    public EulerUserDetails(String userId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this(userId, username, password, true, true, true, true, authorities);
    }

    /**
     * Construct the <code>User</code> with the details required by
     * {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}.
     *
     * @param username              the username presented to the
     *                              <code>DaoAuthenticationProvider</code>
     * @param password              the password that should be presented to the
     *                              <code>DaoAuthenticationProvider</code>
     * @param enabled               set to <code>true</code> if the user is enabled
     * @param accountNonExpired     set to <code>true</code> if the account has not expired
     * @param credentialsNonExpired set to <code>true</code> if the credentials have not
     *                              expired
     * @param accountNonLocked      set to <code>true</code> if the account is not locked
     * @param authorities           the authorities that should be granted to the caller if they
     *                              presented the correct username and password and the user is enabled. Not null.
     * @throws IllegalArgumentException if a <code>null</code> value was passed either as
     *                                  a parameter or as an element in the <code>GrantedAuthority</code> collection
     */
    public EulerUserDetails(String userId, String username, String password, boolean enabled, boolean accountNonExpired,
                            boolean credentialsNonExpired, boolean accountNonLocked,
                            Collection<? extends GrantedAuthority> authorities) {
        Assert.isTrue(username != null && !username.isEmpty() && password != null,
                "Cannot pass null or empty values to constructor");

        this.userId = userId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.authorities = Collections.unmodifiableSet(UserDetailsUtils.sortAuthorities(authorities));
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Set<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    /**
     * Returns {@code true} if the supplied object is a {@code User} instance with the
     * same {@code username} value.
     * <p>
     * In other words, the objects are equal if they have the same username, representing
     * the same principal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EulerUserDetails user) {
            return this.username.equals(user.getUsername());
        }
        return false;
    }

    /**
     * Returns the hashcode of the {@code username}.
     */
    @Override
    public int hashCode() {
        return this.username.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + " [" +
                "UserId=" + this.userId + ", " +
                "Username=" + this.username + ", " +
                "Password=[PROTECTED], " +
                "Enabled=" + this.enabled + ", " +
                "AccountNonExpired=" + this.accountNonExpired + ", " +
                "CredentialsNonExpired=" + this.credentialsNonExpired + ", " +
                "AccountNonLocked=" + this.accountNonLocked + ", " +
                "Granted Authorities=" + this.authorities + "]";
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    /**
     * Builds the user to be added. At minimum the username, password, and authorities
     * should provided. The remaining attributes have reasonable defaults.
     */
    public static final class UserBuilder {
        private String userId;

        private String username;

        private String password;

        private List<GrantedAuthority> authorities = new ArrayList<>();

        private boolean accountExpired;

        private boolean accountLocked;

        private boolean credentialsExpired;

        private boolean disabled;

        private Function<String, String> passwordEncoder = (password) -> password;

        /**
         * Creates a new instance
         */
        private UserBuilder() {
        }

        /**
         * Populates the userId. This attribute is required.
         *
         * @param userId the userId. Cannot be null.
         * @return the {@link UserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public UserBuilder userId(String userId) {
            Assert.notNull(userId, "userId cannot be null");
            this.userId = userId;
            return this;
        }

        /**
         * Populates the username. This attribute is required.
         *
         * @param username the username. Cannot be null.
         * @return the {@link UserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public UserBuilder username(String username) {
            Assert.notNull(username, "username cannot be null");
            this.username = username;
            return this;
        }

        /**
         * Populates the password. This attribute is required.
         *
         * @param password the password. Cannot be null.
         * @return the {@link UserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public UserBuilder password(String password) {
            Assert.notNull(password, "password cannot be null");
            this.password = password;
            return this;
        }

        /**
         * Encodes the current password (if non-null) and any future passwords supplied to
         * {@link #password(String)}.
         *
         * @param encoder the encoder to use
         * @return the {@link UserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public UserBuilder passwordEncoder(Function<String, String> encoder) {
            Assert.notNull(encoder, "encoder cannot be null");
            this.passwordEncoder = encoder;
            return this;
        }

        /**
         * Populates the roles. This method is a shortcut for calling
         * {@link #authorities(String...)}, but automatically prefixes each entry with
         * "ROLE_". This means the following:
         *
         * <code>
         * builder.roles("USER","ADMIN");
         * </code>
         * <p>
         * is equivalent to
         *
         * <code>
         * builder.authorities("ROLE_USER","ROLE_ADMIN");
         * </code>
         *
         * <p>
         * This attribute is required, but can also be populated with
         * {@link #authorities(String...)}.
         * </p>
         *
         * @param roles the roles for this user (i.e. USER, ADMIN, etc). Cannot be null,
         *              contain null values or start with "ROLE_"
         * @return the {@link UserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public UserBuilder roles(String... roles) {
            List<GrantedAuthority> authorities = new ArrayList<>(roles.length);
            for (String role : roles) {
                Assert.isTrue(!role.startsWith("ROLE_"),
                        () -> role + " cannot start with ROLE_ (it is automatically added)");
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
            return authorities(authorities);
        }

        /**
         * Populates the authorities. This attribute is required.
         *
         * @param authorities the authorities for this user. Cannot be null, or contain
         *                    null values
         * @return the {@link UserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         * @see #roles(String...)
         */
        public UserBuilder authorities(GrantedAuthority... authorities) {
            Assert.notNull(authorities, "authorities cannot be null");
            return authorities(Arrays.asList(authorities));
        }

        /**
         * Populates the authorities. This attribute is required.
         *
         * @param authorities the authorities for this user. Cannot be null, or contain
         *                    null values
         * @return the {@link UserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         * @see #roles(String...)
         */
        public UserBuilder authorities(Collection<? extends GrantedAuthority> authorities) {
            Assert.notNull(authorities, "authorities cannot be null");
            this.authorities = new ArrayList<>(authorities);
            return this;
        }

        /**
         * Populates the authorities. This attribute is required.
         *
         * @param authorities the authorities for this user (i.e. ROLE_USER, ROLE_ADMIN,
         *                    etc). Cannot be null, or contain null values
         * @return the {@link UserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         * @see #roles(String...)
         */
        public UserBuilder authorities(String... authorities) {
            Assert.notNull(authorities, "authorities cannot be null");
            return authorities(AuthorityUtils.createAuthorityList(authorities));
        }

        /**
         * Defines if the account is expired or not. Default is false.
         *
         * @param accountExpired true if the account is expired, false otherwise
         * @return the {@link UserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public UserBuilder accountExpired(boolean accountExpired) {
            this.accountExpired = accountExpired;
            return this;
        }

        /**
         * Defines if the account is locked or not. Default is false.
         *
         * @param accountLocked true if the account is locked, false otherwise
         * @return the {@link UserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public UserBuilder accountLocked(boolean accountLocked) {
            this.accountLocked = accountLocked;
            return this;
        }

        /**
         * Defines if the credentials are expired or not. Default is false.
         *
         * @param credentialsExpired true if the credentials are expired, false otherwise
         * @return the {@link UserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public UserBuilder credentialsExpired(boolean credentialsExpired) {
            this.credentialsExpired = credentialsExpired;
            return this;
        }

        /**
         * Defines if the account is disabled or not. Default is false.
         *
         * @param disabled true if the account is disabled, false otherwise
         * @return the {@link UserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public UserBuilder disabled(boolean disabled) {
            this.disabled = disabled;
            return this;
        }

        public EulerUserDetails build() {
            String encodedPassword = this.passwordEncoder.apply(this.password);
            return new EulerUserDetails(this.userId, this.username, encodedPassword, !this.disabled, !this.accountExpired,
                    !this.credentialsExpired, !this.accountLocked, this.authorities);
        }

    }
}
