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
package org.eulerframework.web.module.file.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import org.eulerframework.common.util.io.file.FileReadException;
import org.eulerframework.common.util.io.file.FileUtils;
import org.eulerframework.web.config.WebConfig;
import org.eulerframework.web.core.annotation.JspController;
import org.eulerframework.web.core.base.controller.JspSupportWebController;
import org.eulerframework.web.core.exception.web.PageNotFoundException;
import org.eulerframework.web.module.file.conf.FileConfig;
import org.eulerframework.web.module.file.enmus.FileType;
import org.eulerframework.web.module.file.entity.ArchivedFile;
import org.eulerframework.web.module.file.exception.FileArchiveException;
import org.eulerframework.web.module.file.service.ArchivedFileService;

@JspController
@RequestMapping("/")
public class FileUploadAndDownloadWebController extends JspSupportWebController {

    @Resource
    private ArchivedFileService archivedFileService;

    @RequestMapping(value = "plupload", method = RequestMethod.GET)
    public String plupload(
            @RequestParam boolean multi,
            @RequestParam FileType fileType, 
            @RequestParam String app) {
        this.getRequest().setAttribute("multi", multi);
        this.getRequest().setAttribute("fileType", fileType.toJson());
        this.getRequest().setAttribute("extensions", fileType.getExtensions());
        this.getRequest().setAttribute("app", app);
        this.getRequest().setAttribute("maxFileSize", WebConfig.getMultipartConfig().getMaxFileSize().toMegabytes());
        return this.display("/common/plupload");
    }
    
    @ResponseBody
    @RequestMapping(
            path = {
                FileConfig.FILE_DOWNLOAD_PATH +"/{param}", 
                FileConfig.IMAGE_DOWNLOAD_PATH +"/{param}", 
                FileConfig.VIDEO_DOWNLOAD_PATH +"/{param}"}, 
            method = RequestMethod.GET)
    public void downloadArchivedFile(
            @PathVariable("param") String param) throws FileReadException, IOException {
        ArchivedFile archivedFile = this.getRequestFile(param);
        
        try {
            this.writeFile(archivedFile.getOriginalFilename(), archivedFile.getArchivedFile());
        } catch (FileNotFoundException e) {
            this.logger.warn(e.getMessage(), e);
            throw new PageNotFoundException();
        } catch (IOException e) {
            throw e;
        }
    }
    
    private ArchivedFile getRequestFile(String requestParam) {
        String extensions = FileUtils.extractFileExtension(requestParam);
        String archivedFileId = FileUtils.extractFileNameWithoutExtension(requestParam);
        ArchivedFile archivedFile = this.archivedFileService.findArchivedFile(archivedFileId);
        
        if(archivedFile == null)
            throw new PageNotFoundException();
        
        if(extensions != null) {
            if(!extensions.equals(archivedFile.getExtension())) {
                if(this.logger.isInfoEnabled()) {
                    this.logger.info("The file extension does not match, specified as " + extensions + ", actually " + archivedFile.getExtension());
                }
                throw new PageNotFoundException();
            }
        }
        
        String archivedFilePath = FileConfig.getFileArchivedPath();
        
        if(archivedFile.getArchivedPathSuffix() != null)
            archivedFilePath = archivedFilePath + "/" + archivedFile.getArchivedPathSuffix();
        
        File file = new File(archivedFilePath, archivedFile.getArchivedFilename());
        archivedFile.setArchivedFile(file);
        return archivedFile;
    }
    
    @ResponseBody
    @RequestMapping(path = FileConfig.FILE_UPLOAD_ACTION, method = RequestMethod.POST)
    public ArchivedFile uploadArchivedFile(@RequestParam(value="file") MultipartFile multipartFile) throws FileArchiveException {
        return this.archivedFileService.saveMultipartFile(multipartFile);        
    }

}
