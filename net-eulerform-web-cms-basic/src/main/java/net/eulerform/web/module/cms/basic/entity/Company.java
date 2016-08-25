package net.eulerform.web.module.cms.basic.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import net.eulerform.web.core.base.entity.UUIDEntity;

@Entity
@XmlRootElement
@Table(name="CMS_COMPANY")
@SuppressWarnings("serial")
public class Company extends UUIDEntity<Company> {

    @NotNull
    @Column(name="NAME",nullable=false)
    private String name;
    @Column(name="LOGO_FILENAME")
    private String logoFileName;
    @Column(name="DISPLAY_ORDER",nullable=false)
    private Integer order;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLogoFileName() {
        return logoFileName;
    }
    public void setLogoFileName(String logoFileName) {
        this.logoFileName = logoFileName;
    }
    public Integer getOrder() {
        return order;
    }
    public void setOrder(Integer order) {
        this.order = order;
    }
    
    
}
