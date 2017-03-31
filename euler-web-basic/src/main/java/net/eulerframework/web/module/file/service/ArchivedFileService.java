package net.eulerframework.web.module.file.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.eulerframework.common.util.Assert;
import net.eulerframework.common.util.DateUtils;
import net.eulerframework.common.util.StringUtils;
import net.eulerframework.common.util.io.file.SimpleFileIOUtils;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.file.dao.IArchivedFileDao;
import net.eulerframework.web.module.file.entity.ArchivedFile;
import net.eulerframework.web.module.file.exception.FileArchiveException;
import net.eulerframework.web.module.file.util.WebFileTool;
import net.eulerframework.web.util.ServletUtils;

@Service
public class ArchivedFileService extends BaseService {

    @Resource
    private IArchivedFileDao archivedFileDao;

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
        af.setExtension(WebFileTool.extractFileExtension(originalFilename));
        af.setFileByteSize(fileSize);
        af.setMd5(md5);
        af.setArchiveDate(new Date());
        Object userId = ServletUtils.getRequest().getAttribute("__USER_ID");
        if(userId != null)
            af.setArchiveUserId(userId.toString());
        else
            af.setArchiveUserId("anonymousUser");

        this.archivedFileDao.save(af);

        return af;
    }

    public ArchivedFile saveFile(File file) throws FileArchiveException {
        String archiveFilePath = WebConfig.getUploadPath();
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
        String archiveFilePath = WebConfig.getUploadPath();
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

        return this.archivedFileDao.load(archivedFileId);
    }

    public void deleteArchivedFile(String... archivedFileId) {
        Assert.notNull(archivedFileId);

        List<ArchivedFile> archivedFile = this.archivedFileDao.load(archivedFileId);

        if (archivedFile == null)
            return;

        this.archivedFileDao.deleteByIds(archivedFileId);

        for (ArchivedFile each : archivedFile) {
            File file = WebFileTool.getArchivedFile(each);
            SimpleFileIOUtils.deleteFile(file);
        }
    }

}
