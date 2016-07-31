package net.eulerform.web.module.authentication.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import net.eulerform.web.core.base.entity.UUIDEntity;

@SuppressWarnings("serial")
@Entity
@Table(name="SYS_RESOURCE")
public class Resource extends UUIDEntity<Resource> {

    @NotNull
    @Pattern(regexp="[a-zA-Z][a-zA-Z0-9_]*", message="{validation.resource.resourceId}")
    @Column(name="RESOURCE_ID",nullable=false,unique=true)
    private String resourceId;
    @Column(name="NAME",nullable=false,unique=true)
    private String name;
    @Column(name="DESCRIPTION")
    private String description;
    
    public String getResourceId() {
        return resourceId;
    }
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

}
