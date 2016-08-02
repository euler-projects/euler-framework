package net.eulerform.web.module.basedata.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import net.eulerform.web.core.base.entity.UUIDEntity;

/**
 * CODE_TABLE<br>
 * 用来存储JS字典数据或系统配置参数
 * 
 * @author cFrost
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "SYS_PAGE")
public class Page extends UUIDEntity<Page> {

    @Column(name = "NAME", nullable = false)
    private String name;
    @Column(name = "URL", nullable = false)
    private String url;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "SHOW_ORDER", nullable = false)
    private Integer showOrder;
    @NotNull
    @Pattern(regexp="[A-Z][A-Z_]*", message="{validation.authority.authority}")
    @Column(name = "REQUIRE_AUTHORITY")
    private String requireAuthority;
    @Column(name = "MODULE_ID")
    private String moduleId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取数据在页面的显示顺序,只对JS字典类数据有效
     * 
     * @return 显示顺序序号
     */
    public Integer getShowOrder() {
        return showOrder;
    }

    /**
     * 设置数据再页面的显示顺序,只对JS字典类数据有效
     * 
     * @param showOrder
     *            显示顺序序号
     */
    public void setShowOrder(Integer showOrder) {
        this.showOrder = showOrder;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRequireAuthority() {
        return requireAuthority;
    }

    public void setRequireAuthority(String requireAuthority) {
        this.requireAuthority = requireAuthority;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
}
