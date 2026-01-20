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
import org.eulerframework.data.file.registry.FileIndex;
import org.eulerframework.util.function.Handler;
import org.eulerframework.data.file.servlet.LocalStorageFileDownloader;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
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
     * @param fileId           文件ID, 可以在ID后携带文件扩展名
     * @param dest             待写入的目标文件
     * @param fileIndexConsumer 文件索引对象回调, 会在正式写入文件前回调, 调用方可以做一些预处理
     */
    void get(String fileId, File dest, Consumer<FileIndex> fileIndexConsumer) throws IOException, StorageFileNotFoundException;

    /**
     * 将文件数据写入输出流
     *
     * @param fileId           文件ID, 可以在ID后携带文件扩展名
     * @param out              待写入的输出流
     * @param fileIndexConsumer 文件索引对象回调, 会在正式写入输出流前回调, 调用方可以做一些预处理, 一个常见的场景是在下载文件时，
     *                         可以在在数据正式写入Response的OutputStream前设置Response Header,
     *                         可以参考{@link LocalStorageFileDownloader#download(String, HttpServletResponse)}
     */
    void get(String fileId, OutputStream out, Consumer<FileIndex> fileIndexConsumer) throws IOException, StorageFileNotFoundException;

    /**
     * 获取文件资源对象
     * <p>
     * 返回一个 Spring {@link Resource} 对象, 用于支持 HTTP Range 请求等高级文件访问场景.
     * 通过 {@link Resource} 可以实现断点续传、部分内容下载等功能.
     *
     * @param fileId           文件ID, 可以在ID后携带文件扩展名
     * @param fileIndexConsumer 文件索引对象回调, 会在返回资源对象前回调, 调用方可以做一些预处理,
     *                         例如在返回前设置 Response Header
     * @return Spring {@link Resource} 对象, 可用于支持 Range 请求的文件访问
     */
    Resource getFileResource(String fileId, Consumer<FileIndex> fileIndexConsumer) throws StorageFileNotFoundException, IOException;

    /**
     * 获取文件 <code>URI</code>
     * <p>
     * 在一些使用远程文件存储服务的场景, 例如使用云厂商的对象存储服务, 可以不通过本地缓存转发的形式获取文件,
     * 而是直接生成一个指向对象存储云服务的临时 <code>URI</code>, 用户通过这个 <code>URI</code> 就能直接获取文件,
     * 但要注意权限管控, 防止具有永久访问权限的 <code>URI</code> 泄露.
     *
     * @param fileId 文件ID, 可以在ID后携带文件扩展名
     * @return 指向文件存储服务的 <code>URI</code>, 可以通过该 <code>URI</code> 直接下载文件, 无需通过本地转发
     */
    URI getUri(String fileId) throws StorageFileNotFoundException, IOException;
}
