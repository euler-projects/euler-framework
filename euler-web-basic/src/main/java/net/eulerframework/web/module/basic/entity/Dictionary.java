package net.eulerframework.web.module.basic.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import net.eulerframework.web.core.base.entity.UUIDEntity;

/**
 * CODE_TABLE<br>
 * 用来存储JS字典数据或系统配置参数
 * @author cFrost
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "SYS_DICT")
public class Dictionary extends UUIDEntity<Dictionary> {

    @NotNull
    @Pattern(regexp="[a-z][a-zA-Z0-9_]*", message="{validation.codeTable.name}")
    @Column(name = "NAME", nullable = false)
    private String name;
    @NotNull
    @Column(name = "DICT_KEY", nullable = false)
    private String key;
    @Column(name = "DICT_VALUE")
    private String value;   
    @Column(name = "SHOW_ORDER")
    private Integer showOrder;  
    @Column(name = "CSS_STYLE")
    private String cssStyle;
    @Column(name="REMARK")
    private String remark;
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
    public String getCssStyle() {
        return cssStyle;
    }
    public void setCssStyle(String cssStyle) {
        this.cssStyle = cssStyle;
    }
    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
}
