package net.eulerframework.web.module.file.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import net.eulerframework.web.core.base.entity.UUIDEntity;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractAttachment extends UUIDEntity<AbstractAttachment> {
    @Column(name="FILE_ID", length=36, nullable=false)
    private String fileId;
    
    public String getFileId() {
        return fileId;
    }
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    public abstract void setOwnerId(Serializable ownerId);
    public abstract Serializable getOwnerId();
}
