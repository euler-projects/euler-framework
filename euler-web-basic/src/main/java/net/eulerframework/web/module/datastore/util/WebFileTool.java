package net.eulerframework.web.module.datastore.util;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import net.eulerframework.common.util.FileReader;
import net.eulerframework.common.util.StringTool;

public class WebFileTool {

    public static void writeFileToResponse(String fileName, File file) throws IOException {
        byte[] fileData = FileReader.readFileByMultiBytes(file, 1024);
        
        HttpServletResponse response = getResponse();

        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        response.setHeader("Content-Disposition", 
                "attachment;fileName=" + new String(fileName.getBytes("utf-8"), "ISO8859-1"));

        response.setStatus(HttpStatus.OK.value());
        response.getOutputStream().write(fileData);
    }
    
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
    
    private static ServletRequestAttributes getServletRequestAttributes() {
        return (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
    }
    
    @SuppressWarnings("unused")
    private static HttpServletRequest getRequest() {
        return getServletRequestAttributes().getRequest();
    }
    
    private static HttpServletResponse getResponse() {
        return getServletRequestAttributes().getResponse();
    }
}
