package net.eulerframework.web.module.authentication.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.eulerframework.web.core.base.entity.BaseEntity;

@MappedSuperclass
public abstract class AbstractUserProfile implements BaseEntity<AbstractUserProfile, String> {
    @Id
    @Column(name = "USER_ID", length = 36)
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    @JsonIgnore
    public String getId() {
        return this.userId;
    }
    
    @Override
    public void setId(String id) {
        this.setUserId(id);
    }

    @Override
    public int compareTo(AbstractUserProfile o) {
        return this.getUserId().compareTo(o.getUserId());
    }

}
