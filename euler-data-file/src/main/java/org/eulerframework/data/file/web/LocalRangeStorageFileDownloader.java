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
package org.eulerframework.data.file.web;

import org.eulerframework.common.util.Assert;
import org.eulerframework.data.file.AbstractLocalFileStorage;
import org.eulerframework.data.file.JdbcFileStorage;
import org.eulerframework.data.file.StorageFileNotFoundException;
import org.eulerframework.data.file.web.security.FileToken;
import org.eulerframework.data.file.web.security.FileTokenRegistry;
import org.eulerframework.web.core.exception.web.api.ResourceNotFoundException;
import org.eulerframework.web.util.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.util.Optional;

public class LocalRangeStorageFileDownloader implements RangeStorageFileDownloader {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AbstractLocalFileStorage fileStorage;
    private final FileTokenRegistry fileTokenRegistry;

    public LocalRangeStorageFileDownloader(AbstractLocalFileStorage fileStorage, FileTokenRegistry fileTokenRegistry) {
        this.fileStorage = fileStorage;
        this.fileTokenRegistry = fileTokenRegistry;
    }

    @Override
    public ResponseEntity<Resource> download(String fileId, String token) throws IOException {
        try {
            FileToken fileToken = this.fileTokenRegistry.getTokenByTokenValue(token);
            Assert.isTrue(fileId.equals(fileToken.getFileId()));
        } catch (Exception e) {
            this.logger.warn("File download access denied: {}", e.getMessage(), e);
            throw new AccessDeniedException("Access denied");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            // 设置支持 Range 请求
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            Resource resource = this.fileStorage.getFileResource(fileId, fileIndex -> {
                ResponseUtils.writeFileHeader(headers,
                        fileIndex.getFilename(),
                        Optional.ofNullable(fileIndex.getAttribute(JdbcFileStorage.ATTR_FILE_SIZE))
                                .map(v -> (Integer) v)
                                .map(Integer::longValue)
                                .orElse(null));
            });

            // 返回 ResponseEntity，Spring 会自动处理 Range 请求
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (StorageFileNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage(), e);
        }
    }


    @Override
    public boolean support(String type) {
        return fileStorage.support(type);
    }
}
