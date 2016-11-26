package net.eulerframework.web.module.ueditor.entity;

import java.util.List;

public class UeConfig {

    private String imageActionName;
    private String imageFieldName;
    private String imageUrlPrefix;
    private List<String> imageAllowFiles;
    private Boolean imageCompressEnable;
    private Integer imageCompressBorder;
    
    private String fileActionName;
    private String fileFieldName;
    private String fileUrlPrefix;
    private List<String> fileAllowFiles;

    public String getImageActionName() {
        return imageActionName;
    }

    public void setImageActionName(String imageActionName) {
        this.imageActionName = imageActionName;
    }

    public String getImageFieldName() {
        return imageFieldName;
    }

    public void setImageFieldName(String imageFieldName) {
        this.imageFieldName = imageFieldName;
    }

    public String getImageUrlPrefix() {
        return imageUrlPrefix;
    }

    public void setImageUrlPrefix(String imageUrlPrefix) {
        this.imageUrlPrefix = imageUrlPrefix;
    }

    public List<String> getImageAllowFiles() {
        return imageAllowFiles;
    }

    public void setImageAllowFiles(List<String> imageAllowFiles) {
        this.imageAllowFiles = imageAllowFiles;
    }

    public Boolean getImageCompressEnable() {
        return imageCompressEnable;
    }

    public void setImageCompressEnable(Boolean imageCompressEnable) {
        this.imageCompressEnable = imageCompressEnable;
    }

    public Integer getImageCompressBorder() {
        return imageCompressBorder;
    }

    public void setImageCompressBorder(Integer imageCompressBorder) {
        this.imageCompressBorder = imageCompressBorder;
    }

    public String getFileActionName() {
        return fileActionName;
    }

    public void setFileActionName(String fileActionName) {
        this.fileActionName = fileActionName;
    }

    public String getFileFieldName() {
        return fileFieldName;
    }

    public void setFileFieldName(String fileFieldName) {
        this.fileFieldName = fileFieldName;
    }

    public String getFileUrlPrefix() {
        return fileUrlPrefix;
    }

    public void setFileUrlPrefix(String fileUrlPrefix) {
        this.fileUrlPrefix = fileUrlPrefix;
    }

    public List<String> getFileAllowFiles() {
        return fileAllowFiles;
    }

    public void setFileAllowFiles(List<String> fileAllowFiles) {
        this.fileAllowFiles = fileAllowFiles;
    }
    
}
