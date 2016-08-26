package net.eulerform.web.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.eulerform.common.FilePathTool;
import net.eulerform.common.GlobalProperties;
import net.eulerform.common.GlobalPropertyReadException;

public abstract class WebConfig {
    
    protected final static Logger log = LogManager.getLogger();
    
    private final static String UPLOAD_PATH = "upload.path";
    private static String uploadPath = "/upload";

    public static String getUploadPath() {

            try {
                uploadPath = FilePathTool.changeToUnixFormat(GlobalProperties.get(UPLOAD_PATH));
            } catch (GlobalPropertyReadException e) {
                log.warn("Couldn't load " + UPLOAD_PATH + " , use " + uploadPath + " for default.");
            }
        
        return uploadPath;
        
    }
}
