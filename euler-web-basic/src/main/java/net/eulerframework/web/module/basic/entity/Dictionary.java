package net.eulerframework.web.module.basic.entity;

import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import net.eulerframework.web.core.base.entity.UUIDEntity;

@Entity
@Table(name = "SYS_DICT", uniqueConstraints = {
        @UniqueConstraint(columnNames={"CODE", "DICT_KEY", "LOCALE"}),
        @UniqueConstraint(columnNames={"CODE", "DICT_VALUE"}),
        @UniqueConstraint(columnNames={"CODE", "SHOW_ORDER", "LOCALE"})})
public class Dictionary extends UUIDEntity<Dictionary> {

    @Column(name = "CODE", nullable = false)
    private String code;
    @NotNull
    @Column(name = "DICT_KEY", nullable = false)
    private String key;
    @Column(name = "DICT_VALUE")
    private String value;
    @Column(name = "LOCALE", nullable = false)
    private Locale locale;
    @Column(name = "SHOW_ORDER", nullable = false)
    private Integer showOrder;
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
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
    public Integer getShowOrder() {
        return showOrder;
    }
    public void setShowOrder(Integer showOrder) {
        this.showOrder = showOrder;
    }
    public Locale getLocale() {
        return locale;
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
}
