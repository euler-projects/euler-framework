package org.eulerframework.web.module.file;

import org.eulerframework.core.model.AbstractResourceModel;

public class StorageFile extends AbstractResourceModel {
    private String fileId;
    private String filename;
    private String extension;
    private String storageType;
    private String storageIndex;
    private Long fileSize;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getStorageIndex() {
        return storageIndex;
    }

    public void setStorageIndex(String storageIndex) {
        this.storageIndex = storageIndex;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getFileSize() {
        return fileSize;
    }
}
