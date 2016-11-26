package net.eulerframework.web.module.authentication.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import net.eulerframework.web.core.base.entity.UUIDEntity;

@SuppressWarnings("serial")
@Entity
@Table(name="SYS_GROUP")
public class Group extends UUIDEntity<Group> {
    
    public static final String SYSTEM_USERS_CROUP_NAME = "Users";

    @Column(name="NAME", nullable = false, unique = true)
    private String name;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "SYS_GROUP_AUTHORITY", joinColumns = { @JoinColumn(name = "GROUP_ID") }, inverseJoinColumns = { @JoinColumn(name = "AUTHORITY_ID") })
    private Set<Authority> authorities;
    @Column(name="DESCRIPTION")
    private String description;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Set<Authority> getAuthorities() {
        return authorities;
    }
    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    
}
