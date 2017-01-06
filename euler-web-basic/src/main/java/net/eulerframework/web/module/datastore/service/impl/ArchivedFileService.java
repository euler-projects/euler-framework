package net.eulerframework.web.module.datastore.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.eulerframework.common.util.Assert;
import net.eulerframework.common.util.FileReader;
import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.util.UserContext;
import net.eulerframework.web.module.datastore.dao.IArchivedFileDao;
import net.eulerframework.web.module.datastore.entity.ArchivedFile;
import net.eulerframework.web.module.datastore.service.IArchivedFileService;
import net.eulerframework.web.module.datastore.util.WebFileTool;

import net.eulerframework.web.module.datastore.exception.FileArchiveException;

@Service
public class ArchivedFileService extends BaseService implements IArchivedFileService {
    
    @Resource
    private IArchivedFileDao archivedFileDao;

    @Override
    public ArchivedFile saveFileInfo(String originalFilename, File archivedFile) throws IOException {
        InputStream inputStream = new FileInputStream(archivedFile);
        String md5 = DigestUtils.md5Hex(inputStream);
        long fileSize = archivedFile.length();
        String archivedFilename = archivedFile.getName();
        
        ArchivedFile af = new ArchivedFile();
        
        af.setOriginalFilename(originalFilename);
        af.setArchivedFilename(archivedFilename);
        af.setExtension(WebFileTool.extractFileExtension(originalFilename));
        af.setFileByteSize(fileSize);
        af.setMd5(md5);
        af.setArchiveDate(new Date());
        
        User currUser = UserContext.getCurrentUser();
        
        af.setArchiveUserId(currUser.getId());
        
        this.archivedFileDao.save(af);
        
        return af;
    }

    @Override
    public ArchivedFile saveFile(File file) throws FileArchiveException {
        String archiveFilePath = WebConfig.getUploadPath();        
        
        String originalFilename = file.getName();     
        String targetFilename = UUID.randomUUID().toString();
        
        File targetFile = new File(archiveFilePath, targetFilename);
        
        try {
            Files.copy(file.toPath(), targetFile.toPath());
            
            this.logger.info("已保存文件: " + targetFile.getPath());
            
            return this.saveFileInfo(originalFilename, targetFile);
        } catch (IllegalStateException | IOException e) {
            if(targetFile.exists())
                FileReader.deleteFile(targetFile);
            
            throw new FileArchiveException(e);
        }
    }
    
    @Override
    public ArchivedFile saveMultipartFile(MultipartFile multipartFile) throws FileArchiveException {
        String archiveFilePath = WebConfig.getUploadPath();        
        
        String originalFilename = multipartFile.getOriginalFilename();        
        String targetFilename = UUID.randomUUID().toString();
        
        File targetFile = new File(archiveFilePath, targetFilename);
        
        try {
            multipartFile.transferTo(targetFile);
            
            this.logger.info("已保存文件: " + targetFile.getPath());
            
            return this.saveFileInfo(originalFilename, targetFile);
        } catch (IllegalStateException | IOException e) {
            if(targetFile.exists())
                FileReader.deleteFile(targetFile);
            
            throw new FileArchiveException(e);
        }
    }

    @Override
    public ArchivedFile findArchivedFile(String archivedFileId) {
        Assert.isFalse(StringTool.isNull(archivedFileId), "archivedFileId is null");
        
        return this.archivedFileDao.load(archivedFileId);
    }

}