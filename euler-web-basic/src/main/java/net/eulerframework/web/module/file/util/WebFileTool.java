package net.eulerframework.web.module.file.util;

import java.io.File;

import net.eulerframework.common.util.StringUtils;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.module.file.entity.ArchivedFile;

public class WebFileTool {
    
    public static String extractFileExtension(String fileName) {
        String extension = "";
        
        if(StringUtils.isEmpty(fileName))
            return extension;
        
        int dot = fileName.lastIndexOf('.');
        if(dot > -1) {
            extension = fileName.substring(dot);
        }
        return extension;
    }
    
    public static File getArchivedFile(ArchivedFile archivedFile) {
        
        String archivedFilePath = WebConfig.getUploadPath();
        
        if(archivedFile.getArchivedPathSuffix() != null)
            archivedFilePath += "/" + archivedFile.getArchivedPathSuffix();
        
        return new File(archivedFilePath, archivedFile.getArchivedFilename());
    }
}
