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
@Table(name="sys_group")
public class Group extends UUIDEntity<Group> {

    @Column(name="code", nullable = false, unique = true)
    private String code;
    @Column(name="name", nullable = false, unique = true)
    private String name;
    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name = "sys_group_authority", joinColumns = {
            @JoinColumn(name = "group_id")
    }, inverseJoinColumns = {
            @JoinColumn(name = "authority")
    })
    private Set<Authority> authorities;
    @Column(name="description")
    private String description;
    
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
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
