package net.eulerframework.web.module.cms.basic.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import net.eulerframework.web.core.base.entity.UUIDEntity;

@Entity
@XmlRootElement
@Table(name="CMS_NEWS")
@SuppressWarnings("serial")
public class News extends UUIDEntity<News> {

    @NotNull
    @Column(name="TITLE",nullable=false)
    private String title;
    @NotNull
    @Column(name="AUTHOR",nullable=false)
    private String author;
    @Column(name="TOP",nullable=false)
    private Boolean top;
    @NotNull
    @Column(name="SUMMARY",nullable=false)
    private String summary;
    @Column(name="PUB_DATE",nullable=false)
    private Date pubDate;
    @Column(name="IMG_FILE_NAME")
    private String imageFileName;
    @NotNull
    @Column(name="TEXT", columnDefinition="TEXT", nullable=false)
    private String text;
    
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Date getPubDate() {
        return pubDate;
    }
    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }
    public String getImageFileName() {
        return imageFileName;
    }
    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public Boolean getTop() {
        return top;
    }
    public void setTop(Boolean top) {
        this.top = top;
    }
    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    
}
