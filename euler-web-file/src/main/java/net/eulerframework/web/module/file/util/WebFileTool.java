package net.eulerframework.web.module.file.util;

import java.io.File;

import net.eulerframework.web.module.file.conf.FileConfig;
import net.eulerframework.web.module.file.entity.ArchivedFile;

public class WebFileTool {
    
    public static File getArchivedFile(ArchivedFile archivedFile) {
        
        String archivedFilePath = FileConfig.getFileArchivedPath();
        
        if(archivedFile.getArchivedPathSuffix() != null)
            archivedFilePath = archivedFilePath + "/" + archivedFile.getArchivedPathSuffix();
        
        return new File(archivedFilePath, archivedFile.getArchivedFilename());
    }
}
