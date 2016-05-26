package net.eulerform.web.core.security.authentication.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import net.eulerform.web.core.base.entity.UUIDEntity;

@SuppressWarnings("serial")
@Entity
@Table(name="SYS_SCOPE")
public class Scope extends UUIDEntity<Scope> {

    @Column(name="SCOPE",nullable=false,unique=true)
    private String scope;
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
}
