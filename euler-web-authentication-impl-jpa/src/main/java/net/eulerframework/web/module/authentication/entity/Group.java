package net.eulerframework.web.module.authentication.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import net.eulerframework.web.core.base.entity.UUIDEntity;

@Entity
@Table(name="SYS_GROUP")
public class Group extends UUIDEntity<Group> {
    
    public static final String SYSTEM_USERS_CROUP_NAME = "Users";

    @Column(name="CODE", nullable = false, unique = true)
    private String code;
    @Column(name="NAME", nullable = false, unique = true)
    private String name;
    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name = "SYS_GROUP_AUTHORITY", joinColumns = { @JoinColumn(name = "GROUP_ID") }, inverseJoinColumns = { @JoinColumn(name = "AUTHORITY") })
    private Set<Authority> authorities;
    @Column(name="DESCRIPTION")
    private String description;
    
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public static String getSystemUsersCroupName() {
        return SYSTEM_USERS_CROUP_NAME;
    }
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
