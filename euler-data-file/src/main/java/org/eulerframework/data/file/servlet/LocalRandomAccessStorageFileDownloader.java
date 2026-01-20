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
package org.eulerframework.data.file.servlet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.common.util.MIMEUtils;
import org.eulerframework.data.file.AbstractLocalFileStorage;
import org.eulerframework.data.file.JdbcFileStorage;
import org.eulerframework.data.file.StorageFileNotFoundException;
import org.eulerframework.web.core.exception.web.api.ResourceNotFoundException;
import org.eulerframework.web.util.ResponseUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class LocalRandomAccessStorageFileDownloader implements RandomStorageFileDownloader {

    private final AbstractLocalFileStorage fileStorage;

    public LocalRandomAccessStorageFileDownloader(AbstractLocalFileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    @Override
    public ResponseEntity<Resource> download(String fileId) throws IOException {
        try {
            HttpHeaders headers = new HttpHeaders();
            // 设置支持 Range 请求
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            File file = this.fileStorage.getFile(fileId, fileIndex -> {
                ResponseUtils.writeFileHeader(headers,
                        fileIndex.getFilename(),
                        Optional.ofNullable(fileIndex.getAttribute(JdbcFileStorage.ATTR_FILE_SIZE))
                                .map(v -> (Integer) v)
                                .map(Integer::longValue)
                                .orElse(null),
                        false);

            });

            // 返回 ResponseEntity，Spring 会自动处理 Range 请求
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new FileSystemResource(file));

        } catch (StorageFileNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage(), e);
        }
    }




    @Override
    public boolean support(String type) {
        return fileStorage.support(type);
    }
}
