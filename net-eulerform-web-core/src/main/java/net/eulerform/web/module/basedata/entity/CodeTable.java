package net.eulerform.web.module.basedata.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import net.eulerform.web.core.base.entity.UUIDEntity;

@SuppressWarnings("serial")
@Entity
@Table(name = "SYS_CODE_TABLE")
public class CodeTable extends UUIDEntity<CodeTable> {

    @Column(name = "CODE_NAME", nullable = false)
    private String name;
    @Column(name = "CODE_KEY", nullable = false)
    private String key;
    @Column(name = "CODE_VALUE")
    private String value;
    @Column(name = "SHOW_ORDER", nullable = false)
    private Integer showOrder;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
}
