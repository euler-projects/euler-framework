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
//@Entity
@Table(name = "SYS_CODE_TABLE")
public class CodeTable extends UUIDEntity<CodeTable> {

    @NotNull
    @Pattern(regexp="[a-z][a-zA-Z0-9_]*", message="{validation.codeTable.name}")
    @Column(name = "NAME", nullable = false)
    private String name;
    @NotNull
    @Column(name = "KEY", nullable = false)
    private String key;
    @NotNull
    @Column(name = "VALUE")
    private String value;
    @Column(name = "VALUE_I18N_CODE")
    private String valueI18nCode;    
    @Column(name = "SHOW_ORDER")
    private Integer showOrder;  
    @Column(name = "CSS_STYLE")
    private String cssStyle;
    @Column(name="DESCRIPTION")
    private String description;
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
    public String getValueI18nCode() {
        return valueI18nCode;
    }
    public void setValueI18nCode(String valueI18nCode) {
        this.valueI18nCode = valueI18nCode;
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
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
