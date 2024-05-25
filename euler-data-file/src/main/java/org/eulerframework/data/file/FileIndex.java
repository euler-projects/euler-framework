/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
