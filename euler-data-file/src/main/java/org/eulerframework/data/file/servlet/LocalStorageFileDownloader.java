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

import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.data.file.AbstractLocalFileStorage;
import org.eulerframework.data.file.JdbcFileStorage;
import org.eulerframework.data.file.StorageFileNotFoundException;
import org.eulerframework.web.core.exception.web.api.ResourceNotFoundException;
import org.eulerframework.web.servlet.util.ServletUtils;

import java.io.IOException;
import java.util.Optional;

public class LocalStorageFileDownloader implements StorageFileDownloader {

    private final AbstractLocalFileStorage fileStorage;

    public LocalStorageFileDownloader(AbstractLocalFileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    @Override
    public void download(String fileId, HttpServletResponse response) throws IOException {
        try {
            this.fileStorage.get(fileId, response.getOutputStream(), storageFile -> ServletUtils.writeFileHeader(
                    response,
                    storageFile.getFilename(),
                    Optional.ofNullable(storageFile.getAttribute(JdbcFileStorage.ATTR_FILE_SIZE))
                            .map(v -> (Integer) v)
                            .map(Integer::longValue)
                            .orElse(null)
            ));
        } catch (StorageFileNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage(), e);
        }
    }


    @Override
    public boolean support(String type) {
        return fileStorage.support(type);
    }
}
