package net.eulerframework.web.module.file.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractFileAttachment extends AbstractAttachment {
    @Column(name="FILE_ID", length=36, nullable=false)
    private String fileId;
    
    public String getFileId() {
        return fileId;
    }
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
