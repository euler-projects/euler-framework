package net.eulerform.web.module.basedata.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import net.eulerform.web.core.base.entity.UUIDModifyInfoEntity;

/**
 * CODE_TABLE<br>
 * 用来存储JS字典数据或系统配置参数
 * @author cFrost
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "SYS_CODE_TABLE")
public class CodeTable extends UUIDModifyInfoEntity<CodeTable> {

    @NotNull
    @Pattern(regexp="[a-z][a-zA-Z0-9]*", message="{validation.codeTable.name}")
    @Column(name = "CODE_NAME", nullable = false)
    private String name;    
    @Column(name = "CODE_KEY", nullable = false)
    private String key;
    @Column(name = "CODE_VALUE")
    private String value;
    @Column(name = "CODE_VALUE_ZH_CN")
    private String valueZhCn;
    @Column(name = "CODE_VALUE_EN_US")
    private String valueEnUs;
    @Column(name="DESCRIPTION")
    private String description;
    
    @Column(name = "CODE_TYPE", nullable = false)
    private Integer codeType;
    
    @Column(name = "SHOW_ORDER")
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
    public String getValueZhCn() {
        return valueZhCn;
    }
    public void setValueZhCn(String valueZhCn) {
        this.valueZhCn = valueZhCn;
    }
    public String getValueEnUs() {
        return valueEnUs;
    }
    public void setValueEnUs(String valueEnUs) {
        this.valueEnUs = valueEnUs;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * 获取数据类型
     * @return 1:JS字典;2:系统配置参数
     */
    public Integer getCodeType() {
        return codeType;
    }
    /**
     * 设置数据类型
     * @param codeType 1:JS字典;2:系统配置参数
     */
    public void setCodeType(Integer codeType) {
        this.codeType = codeType;
    }
    /**
     * 获取数据在页面的显示顺序,只对JS字典类数据有效
     * @return 显示顺序序号
     */
    public Integer getShowOrder() {
        return showOrder;
    }
    /**
     * 设置数据再页面的显示顺序,只对JS字典类数据有效
     * @param showOrder 显示顺序序号
     */
    public void setShowOrder(Integer showOrder) {
        this.showOrder = showOrder;
    }
}
