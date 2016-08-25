package net.eulerform.web.module.cms.basic.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import net.eulerform.web.core.base.entity.UUIDEntity;

@Entity
@XmlRootElement
@Table(name="CMS_SLIDESHOW")
@SuppressWarnings("serial")
public class Slideshow extends UUIDEntity<Slideshow> {

    @Column(name="IMG_FILE_NAME",nullable=false)
    private String imgFileName;
    @Column(name="URL")
    private String url;
    @Column(name="SHOW_ORDER",nullable=false, unique=true)
    private Integer order;
    public String getImgFileName() {
        return imgFileName;
    }
    public void setImgFileName(String imgFileName) {
        this.imgFileName = imgFileName;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public Integer getOrder() {
        return order;
    }
    public void setOrder(Integer order) {
        this.order = order;
    }
}
