package net.eulerframework.web.module.datastore.util;

import net.eulerframework.common.util.StringTool;

public class WebFileTool {
    
    public static String extractFileExtension(String fileName) {
        String extension = "";
        
        if(StringTool.isNull(fileName))
            return extension;
        
        int dot = fileName.lastIndexOf('.');
        if(dot > -1) {
            extension = fileName.substring(dot);
        }
        return extension;
    }
}
