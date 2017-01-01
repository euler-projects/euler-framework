package net.eulerframework.web.module.oauth2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import net.eulerframework.web.core.base.entity.UUIDEntity;

@SuppressWarnings("serial")
@Entity
@Table(name="SYS_SCOPE")
public class Scope extends UUIDEntity<Scope> {

    @NotNull
    @Pattern(regexp="[A-Z][A-Z_]*", message="{validation.scope.scope}")
    @Column(name="SCOPE",nullable=false,unique=true)
    private String scope;
    @Column(name="NAME",nullable=false,unique=true)
    private String name;
    @Column(name="DESCRIPTION")
    private String description;
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getScope() {
        return scope;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
}
