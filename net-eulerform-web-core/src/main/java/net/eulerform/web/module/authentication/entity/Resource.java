package net.eulerform.web.module.authentication.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import net.eulerform.web.core.base.entity.UUIDEntity;

@SuppressWarnings("serial")
@Entity
@Table(name="SYS_RESOURCE")
public class Resource extends UUIDEntity<Resource> {
    
    @Column(name="RESOURCE_NAME",nullable=false,unique=true)
    private String resourceName;
    @Column(name="DESCRIPTION")
    private String description;
    
    public String getResourceName() {
        return resourceName;
    }
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

}
