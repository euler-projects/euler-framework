package net.eulerframework.web.module.file.util;

import java.io.File;

import net.eulerframework.common.util.StringUtils;
import net.eulerframework.web.module.file.conf.FileConfig;
import net.eulerframework.web.module.file.entity.ArchivedFile;

public class WebFileTool {
    
    public static String extractFileExtension(String fileName) {
        String extension = null;
        
        if(StringUtils.isNull(fileName))
            return extension;
        
        int dot = fileName.lastIndexOf('.');
        if(dot > -1) {
            extension = fileName.substring(dot);
        }
        return extension;
    }
    
    public static String extractFileNameWithoutExtension(String fileName) {
        String extension = extractFileExtension(fileName);
        if(extension == null) {
            return fileName;
        } else {
            return fileName.substring(0, fileName.lastIndexOf(extension));
        }
    }
    
    public static File getArchivedFile(ArchivedFile archivedFile) {
        
        String archivedFilePath = FileConfig.getFileArchivedPath();
        
        if(archivedFile.getArchivedPathSuffix() != null)
            archivedFilePath += archivedFile.getArchivedPathSuffix();
        
        return new File(archivedFilePath, archivedFile.getArchivedFilename());
    }
}
