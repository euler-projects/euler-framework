package org.eulerframework.data.file;

import org.eulerframework.core.model.AbstractResourceModel;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

public class FileIndex extends AbstractResourceModel {
    private String fileId;
    private String filename;
    private String extension;
    private String storageType;
    private String storageIndex;
    private final Map<String, Object> attributes = new HashMap<>();

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

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        if (CollectionUtils.isEmpty(attributes)) {
            return;
        }
        this.attributes.putAll(attributes);
    }

    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }
}
