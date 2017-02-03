package net.eulerframework.web.core.base.controller;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerframework.common.util.io.file.FileReadException;
import net.eulerframework.common.util.io.file.SimpleFileIOUtil;
import net.eulerframework.web.core.base.WebContextAccessable;

public abstract class BaseController extends WebContextAccessable {
    
    @Resource private ObjectMapper objectMapper;
    
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }
    
    protected void writeString(String string) throws IOException{
        this.getResponse().getOutputStream().write(string.getBytes("UTF-8"));
    }
    
    protected void writeFile(String fileName, File file) throws FileReadException, IOException {
        HttpServletResponse response = this.getResponse();

        //response.setCharacterEncoding("utf-8");
        String contentType = "application/octet-stream";//new MimetypesFileTypeMap().getContentType(fileName);
        response.setContentType(contentType);
        //response.setContentType(MimeType.getFileContentType("*"));
        response.setHeader("Content-Disposition", 
                "attachment;fileName=" + new String(fileName.getBytes("utf-8"), "ISO8859-1"));
        response.setHeader("Content-Length", String.valueOf(file.length()));
        SimpleFileIOUtil.readFileToOutputStream(file, response.getOutputStream());
    }
    
    protected void setNoCacheHeader() {
        HttpServletResponse response = this.getResponse();
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Date", new Date().getTime());
        response.setIntHeader("Expires", 0);
    }
}
