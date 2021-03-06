/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.core.base.controller;

import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eulerframework.common.util.MIMEUtils;
import org.eulerframework.common.util.MIMEUtils.MIME;
import org.eulerframework.common.util.io.file.FileUtils;
import org.eulerframework.common.util.io.file.SimpleFileIOUtils;
import org.eulerframework.web.core.base.WebContextAccessable;

public abstract class AbstractWebController extends WebContextAccessable {
    
    /**
     * 尝试以时间戳的方式格式化时间,如果失败则传递原始字符串
     * @param binder
     */
    @InitBinder
    protected void initBinder(WebDataBinder binder) {  
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {  
            @Override  
            public void setAsText(String value) {
                if(StringUtils.hasText(value)) {
                    try {
                        long timestamp = Long.valueOf(value);
                        setValue(new Date(timestamp)); 
                    } catch (NumberFormatException e) {
                        setValue(value);
                    }      
                } else {
                    setValue(value);
                }
            }  
        });  
    
    } 
    
    @Resource private ObjectMapper objectMapper;
    
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }
    
    protected void writeString(String string) throws IOException{
        this.getResponse().getOutputStream().write(string.getBytes("UTF-8"));
    }
    
    protected void writeFile(String fileName, File file) throws FileNotFoundException, IOException {
        HttpServletResponse response = this.getResponse();
        String extension = FileUtils.extractFileExtension(fileName);
        MIME mime;
        if(StringUtils.hasText(extension)) {
            mime = MIMEUtils.getMIME(extension);
        } else {
            mime = MIMEUtils.getDefaultMIME();
        }
        this.getResponse().setHeader("Content-Type", mime.getContentType());
        response.setHeader("Content-Disposition", mime.getContentDisposition() + 
                ";fileName=\"" + new String(fileName.getBytes("utf-8"), "ISO8859-1") + "\"");
        response.setHeader("Content-Length", String.valueOf(file.length()));
        SimpleFileIOUtils.readFileToOutputStream(file, response.getOutputStream(), 2048);
    }
    
    protected void setNoCacheHeader() {
        HttpServletResponse response = this.getResponse();
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Date", new Date().getTime());
        response.setIntHeader("Expires", 0);
    }
}
