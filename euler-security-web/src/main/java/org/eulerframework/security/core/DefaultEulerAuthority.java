package org.eulerframework.security.core;

public class DefaultEulerAuthority implements EulerAuthority {
    private final String authority;
    private final String name;
    private final String description;

    public DefaultEulerAuthority(String authority, String name, String description) {
        this.authority = authority;
        this.name = name;
        this.description = description;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
