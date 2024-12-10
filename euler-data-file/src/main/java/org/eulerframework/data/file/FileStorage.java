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

import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.util.function.Handler;
import org.eulerframework.data.file.servlet.JdbcStorageFileDownloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * 一个通用文件存储器
 * <p>
 * 存储器本身只保存文件索引数据, 可对接给类不同类型的存储, 例如数据库, 本地磁盘, 对象存储等等
 *
 * @see JdbcFileStorage
 */
public interface FileStorage extends Handler<String> {

    /**
     * 文件存储器类型
     */
    String getType();

    /**
     * 使用已存在的{@link File}对象存储一个文件
     *
     * @param file     已存在的文件对象
     * @param filename 文件名, 存储器存储文件时, 使用此参数传入的文件名, 而不是使用{@link File#getName()}获取测文件名
     * @return 文件索引对象
     */
    FileIndex save(File file, String filename) throws IOException;

    /**
     * 从输入流读取数据作为文件存储
     *
     * @param in       数据流
     * @param filename 文件名
     * @return 文件索引对象
     */
    FileIndex save(InputStream in, String filename) throws IOException;

    /**
     * 获取文件索引数据
     *
     * @param fileId 文件ID, 可以在ID后携带文件扩展名, 例如42263799-b526-4209-adb1-b064e015b28e.txt,
     *               如果携带的文件扩展名与实际扩展名不符, 则返回<code>null</code>
     * @return 文件索引对象
     */
    FileIndex getStorageIndex(String fileId);

    /**
     * 将文件数据写入指定文件
     *
     * @param fileId              文件ID, 可以在ID后携带文件扩展名
     * @param dest                待写入的目标文件
     * @param storageFileConsumer 文件索引对象回调, 会在正式写入文件前回调, 调用方可以做一些预处理
     */
    void get(String fileId, File dest, Consumer<FileIndex> storageFileConsumer) throws IOException, StorageFileNotFoundException;

    /**
     * 将文件数据写入输出流
     *
     * @param fileId              文件ID, 可以在ID后携带文件扩展名
     * @param out                 待写入的输出流
     * @param storageFileConsumer 文件索引对象回调, 会在正式写入输出流前回调, 调用方可以做一些预处理, 一个常见的场景是在下载文件时，
     *                            可以在在数据正式写入Response的OutputStream前设置Response Header,
     *                            可以参考{@link JdbcStorageFileDownloader#download(String, HttpServletResponse)}
     */
    void get(String fileId, OutputStream out, Consumer<FileIndex> storageFileConsumer) throws IOException, StorageFileNotFoundException;
}
