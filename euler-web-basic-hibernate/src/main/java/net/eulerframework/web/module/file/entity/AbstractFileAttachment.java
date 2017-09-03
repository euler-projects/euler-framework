package net.eulerframework.web.module.file.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractFileAttachment extends AbstractAttachment {
    @Column(name="FILE_ID", length=36, nullable=false)
    private String fileId;
    @Transient
    private String fileName;
    
    public String getFileId() {
        return fileId;
    }
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
