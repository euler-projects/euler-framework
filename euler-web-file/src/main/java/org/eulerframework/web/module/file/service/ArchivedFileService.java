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
package org.eulerframework.web.module.file.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.eulerframework.common.util.Assert;
import org.eulerframework.common.util.DateUtils;
import org.eulerframework.common.util.StringUtils;
import org.eulerframework.common.util.io.file.FileUtils;
import org.eulerframework.common.util.io.file.SimpleFileIOUtils;
import org.eulerframework.web.core.base.service.impl.BaseService;
import org.eulerframework.web.module.file.conf.FileConfig;
import org.eulerframework.web.module.file.entity.ArchivedFile;
import org.eulerframework.web.module.file.exception.FileArchiveException;
import org.eulerframework.web.module.file.repository.ArchivedFileRepository;
import org.eulerframework.web.module.file.util.WebFileTool;
import org.eulerframework.web.util.ServletUtils;

@Service
public class ArchivedFileService extends BaseService {

    @Resource
    private ArchivedFileRepository archivedFileRepository;

    private ArchivedFile saveFileInfo(String originalFilename, String archivedPathSuffix, File archivedFile)
            throws IOException {
        InputStream inputStream = new FileInputStream(archivedFile);
        String md5 = DigestUtils.md5Hex(inputStream);
        long fileSize = archivedFile.length();
        String archivedFilename = archivedFile.getName();

        ArchivedFile af = new ArchivedFile();

        af.setOriginalFilename(originalFilename);
        af.setArchivedPathSuffix(archivedPathSuffix);
        af.setArchivedFilename(archivedFilename);
        af.setExtension(FileUtils.extractFileExtension(originalFilename));
        af.setFileByteSize(fileSize);
        af.setMd5(md5);
        af.setUploadedDate(new Date());
        Object userId = ServletUtils.getRequest().getAttribute("__USER_ID");
        if(userId != null)
            af.setUploadedUserId(userId.toString());
        else
            af.setUploadedUserId("anonymousUser");

        this.archivedFileRepository.save(af);

        return af;
    }

    public ArchivedFile saveFile(File file) throws FileArchiveException {
        String archiveFilePath = FileConfig.getFileArchivedPath();
        String archivedPathSuffix = DateUtils.formatDate(new Date(), "yyyy-MM-dd");

        String originalFilename = file.getName();
        String targetFilename = UUID.randomUUID().toString();

        File targetFile = new File(archiveFilePath + "/" + archivedPathSuffix, targetFilename);

        try {
            Files.copy(file.toPath(), targetFile.toPath());

            this.logger.info("已保存文件: " + targetFile.getPath());

            return this.saveFileInfo(originalFilename, archivedPathSuffix, targetFile);
        } catch (IllegalStateException | IOException e) {
            if (targetFile.exists())
                SimpleFileIOUtils.deleteFile(targetFile);

            throw new FileArchiveException(e);
        }
    }

    public ArchivedFile saveMultipartFile(MultipartFile multipartFile) throws FileArchiveException {
        String archiveFilePath = FileConfig.getFileArchivedPath();
        String archivedPathSuffix = DateUtils.formatDate(new Date(), "yyyy-MM-dd");

        String originalFilename = multipartFile.getOriginalFilename();
        String targetFilename = UUID.randomUUID().toString();

        File targetFile = new File(archiveFilePath + "/" + archivedPathSuffix, targetFilename);

        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }

        try {
            multipartFile.transferTo(targetFile);

            this.logger.info("已保存文件: " + targetFile.getPath());

            return this.saveFileInfo(originalFilename, archivedPathSuffix, targetFile);
        } catch (IllegalStateException | IOException e) {
            if (targetFile.exists())
                SimpleFileIOUtils.deleteFile(targetFile);

            throw new FileArchiveException(e);
        }
    }

    public ArchivedFile findArchivedFile(String archivedFileId) {
        Assert.isFalse(StringUtils.isNull(archivedFileId), "archivedFileId is null");

        return this.archivedFileRepository.findArchivedFileById(archivedFileId);
    }

    public void deleteArchivedFile(String... archivedFileId) {
        Assert.notNull(archivedFileId);

        List<ArchivedFile> archivedFile = this.archivedFileRepository.findAllById(Arrays.asList(archivedFileId));

        if (archivedFile == null)
            return;

        for(String each : archivedFileId) {
            this.archivedFileRepository.deleteById(each);
        }

        for (ArchivedFile each : archivedFile) {
            File file = WebFileTool.getArchivedFile(each);
            SimpleFileIOUtils.deleteFile(file);
        }
    }

}
