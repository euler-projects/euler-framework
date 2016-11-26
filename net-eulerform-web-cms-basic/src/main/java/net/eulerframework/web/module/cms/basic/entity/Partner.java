package net.eulerframework.web.module.cms.basic.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import net.eulerframework.web.core.base.entity.UUIDEntity;

@Entity
@XmlRootElement
@Table(name="CMS_PARTNER")
@SuppressWarnings("serial")
public class Partner extends UUIDEntity<Partner> {

    @NotNull
    @Column(name="NAME",nullable=false)
    private String name;
    @Column(name="SUMMARY", length=1000)
    private String summary;
    @Column(name="LOGO_FILE_NAME")
    private String logoFileName;
    @Column(name="DISPLAY_ORDER",nullable=false)
    private Integer order;
    @Column(name="URL")
    private String url;
    @Column(name="IS_SHOW",nullable=false)
    private Boolean show;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
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
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public Boolean getShow() {
        return show;
    }
    public void setShow(Boolean show) {
        this.show = show;
    }
}
