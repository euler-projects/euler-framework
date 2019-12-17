/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.config;

import org.springframework.util.unit.DataSize;

public class MultipartConfig {
    private final String location;
    private final DataSize maxFileSize;
    private final DataSize maxRequestSize;
    private final DataSize fileSizeThreshold;
    
    protected MultipartConfig(String location, DataSize maxFileSize, DataSize maxRequestSize, DataSize fileSizeThreshold) {
        this.location = location;
        this.maxFileSize = maxFileSize;
        this.maxRequestSize = maxRequestSize;
        this.fileSizeThreshold = fileSizeThreshold;
    }
    public String getLocation() {
        return location;
    }
    public DataSize getMaxFileSize() {
        return maxFileSize;
    }
    public DataSize getMaxRequestSize() {
        return maxRequestSize;
    }
    public DataSize getFileSizeThreshold() {
        return fileSizeThreshold;
    }
    
}
