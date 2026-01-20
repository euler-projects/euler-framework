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

import org.apache.commons.io.FilenameUtils;
import org.eulerframework.data.file.registry.FileIndex;
import org.eulerframework.data.file.registry.FileIndexRegistry;
import org.eulerframework.data.file.registry.JdbcFileIndexRegistry;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractFileStorage implements FileStorage {

    private final JdbcOperations jdbcOperations;

    private final FileIndexRegistry fileIndexRegistry;

    public AbstractFileStorage(JdbcOperations jdbcOperations, FileIndexRegistry fileIndexRegistry) {
        this.jdbcOperations = jdbcOperations;
        this.fileIndexRegistry = fileIndexRegistry;
    }

    protected JdbcOperations getJdbcOperations() {
        return jdbcOperations;
    }

    protected abstract String saveFileData(File file, String filename) throws IOException;

    /**
     * 读取并保存一个 {@link InputStream} 中的全部数据
     *
     * @param in       {@link InputStream} 对象, <code>FileStorage</code> 将读取其全部数据作为一个文件存储
     * @param filename 原始文件名, 由于无法从 {@link InputStream} 中获取文件的原始文件名, 所以要从过此参数传入
     * @return 保存后的文件索引, <code>FileStorage</code> 将会存储此索引,
     * 并在需要的时候用此索引从具体的 <code>FileStorage</code> 实现获取对应文件
     */
    protected abstract String saveFileData(InputStream in, String filename) throws IOException;

    protected abstract void applyAttributes(FileIndex storageFile);

    protected abstract void writeFileData(String fileIndex, File dest) throws IOException, StorageFileNotFoundException;

    protected abstract void writeFileData(String fileIndex, OutputStream out) throws IOException, StorageFileNotFoundException;

    @Override
    @Transactional
    public FileIndex save(File file, String filename) throws IOException {
        return this.createFileIndex(this.saveFileData(file, filename), filename);
    }

    @Override
    @Transactional
    public FileIndex save(InputStream in, String filename) throws IOException {
        return this.createFileIndex(this.saveFileData(in, filename), filename);
    }

    @Override
    public FileIndex getStorageIndex(String fileId) {
        String baseName = FilenameUtils.getBaseName(fileId);
        String exceptedExtension = FilenameUtils.getExtension(fileId);

        FileIndex storageFile = this.fileIndexRegistry.getFileIndex(baseName);

        if (storageFile == null) {
            return null;
        }

        if (StringUtils.hasText(exceptedExtension) && !exceptedExtension.equalsIgnoreCase(storageFile.getExtension())) {
            return null;
        }

        this.applyAttributes(storageFile);
        return storageFile;
    }

    @Override
    public void get(String fileId, File dest, Consumer<FileIndex> storageFileConsumer) throws IOException, StorageFileNotFoundException {
        FileIndex storageFile = this.getStorageIndex(fileId);
        if (storageFile == null) {
            throw new StorageFileNotFoundException("Storage file '" + fileId + "' not exists");
        }
        storageFileConsumer.accept(storageFile);
        this.writeFileData(storageFile.getStorageIndex(), dest);
    }

    @Override
    public void get(String fileId, OutputStream out, Consumer<FileIndex> storageFileConsumer) throws IOException, StorageFileNotFoundException {
        FileIndex fileIndex = this.getStorageIndex(fileId);
        if (fileIndex == null) {
            throw new StorageFileNotFoundException("Storage file '" + fileId + "' not exists");
        }
        storageFileConsumer.accept(fileIndex);
        this.writeFileData(fileIndex.getStorageIndex(), out);
    }

    private FileIndex createFileIndex(String storageIndex, String filename) {
        String fileId = UUID.randomUUID().toString();
        FileIndex fileIndex = new FileIndex();
        fileIndex.setFileId(fileId);
        fileIndex.setFilename(filename);
        fileIndex.setExtension(FilenameUtils.getExtension(filename));
        fileIndex.setStorageType(this.getType());
        fileIndex.setStorageIndex(storageIndex);
        return this.fileIndexRegistry.createFileIndex(fileIndex);

    }
}
