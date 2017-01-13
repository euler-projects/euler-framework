package net.eulerframework.web.module.authentication.entity;

import javax.persistence.Column;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.eulerframework.web.core.base.entity.BaseEntity;

@SuppressWarnings("serial")
public abstract class AbstractUserProfile implements BaseEntity<AbstractUserProfile> {
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
    public int compareTo(AbstractUserProfile o) {
        return this.getUserId().compareTo(o.getUserId());
    }

}
