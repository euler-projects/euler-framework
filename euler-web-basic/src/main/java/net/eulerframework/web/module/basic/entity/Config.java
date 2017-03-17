package net.eulerframework.web.module.basic.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import net.eulerframework.web.core.base.entity.NonIDEntity;

@SuppressWarnings("serial")
@Entity
@Table(name = "SYS_CONF")
public class Config extends NonIDEntity<Config> {

    @Id
    @Column(name = "CONF_KEY")
    private String key;
    @Column(name = "CONF_VALUE")
    private String value;

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

    @Override
    public String getId() {
        return this.key;
    }

    @Override
    public int compareTo(Config o) {
        return this.getId().compareTo(o.getId());
    }

}
