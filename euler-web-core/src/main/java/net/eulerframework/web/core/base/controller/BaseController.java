package net.eulerframework.web.core.base.controller;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerframework.common.util.io.FileReadException;
import net.eulerframework.common.util.io.FileUtil;
import net.eulerframework.web.core.base.WebContextAccessable;

public abstract class BaseController extends WebContextAccessable {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());
    
    @Resource private ObjectMapper objectMapper;
    
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }
    
    protected void writeString(String string) throws IOException{
        this.getResponse().getOutputStream().write(string.getBytes("UTF-8"));
    }

    protected void writeFile(String fileName, File file) throws FileReadException, IOException {
        byte[] fileData = FileUtil.readFileByMultiBytes(file, 1024);
        
        HttpServletResponse response = this.getResponse();

        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        response.setHeader("Content-Disposition", 
                "attachment;fileName=" + new String(fileName.getBytes("utf-8"), "ISO8859-1"));

        response.setStatus(HttpStatus.OK.value());
        response.getOutputStream().write(fileData);
    }
    
    protected void setNoCacheHeader() {
        HttpServletResponse response = this.getResponse();
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Date", new Date().getTime());
        response.setIntHeader("Expires", 0);
    }
}
