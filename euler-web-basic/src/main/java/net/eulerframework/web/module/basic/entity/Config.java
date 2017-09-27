package net.eulerframework.web.module.basic.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import net.eulerframework.web.core.base.entity.NonIDEntity;

@Entity
@Table(name = "SYS_CONF")
public class Config extends NonIDEntity<Config, String> {

    @Id
    @Column(name = "CONF_KEY")
    private String key;
    @Column(name = "CONF_VALUE")
    private String value;
    @Column(name = "CONF_DESCRIPTION")
    private String description;
    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getId() {
        return this.key;
    }
    
    @Override
    public void setId(String id) {
        this.setKey(id);
    }

    @Override
    public int compareTo(Config o) {
        return this.getId().compareTo(o.getId());
    }

}
