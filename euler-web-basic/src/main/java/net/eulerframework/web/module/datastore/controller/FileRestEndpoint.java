package net.eulerframework.web.module.datastore.controller;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.ApiController;
import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AbstractRestEndpoint;
import net.eulerframework.web.core.base.response.WebServiceResponse;
import net.eulerframework.web.core.exception.ResourceNotFoundException;
import net.eulerframework.web.module.datastore.entity.ArchivedFile;
import net.eulerframework.web.module.datastore.exception.FileArchiveException;
import net.eulerframework.web.module.datastore.service.IArchivedFileService;
import net.eulerframework.web.module.datastore.util.WebFileTool;

@WebController
@ApiController
@Scope("prototype")
@RequestMapping("/datastore")
public class FileRestEndpoint extends AbstractRestEndpoint {

    @Resource
    private IArchivedFileService archivedFileService;
    
    @RequestMapping(value = { "archived/file/{id}" }, method = RequestMethod.GET)
    public void downloadArchivedFile(@PathVariable("id") String archivedFileId, HttpServletResponse response) throws IOException {
        ArchivedFile archivedFile = this.archivedFileService.findArchivedFile(archivedFileId);
        
        if(archivedFile == null)
            throw new ResourceNotFoundException();
        
        String archivedFilePath = WebConfig.getUploadPath();
        File file = new File(archivedFilePath, archivedFile.getArchivedFilename());
        String fileName = archivedFile.getOriginalFilename();
        
        this.setNoCacheHeader();
        
        WebFileTool.writeFileToResponse(fileName, file);
    }
    
    @RequestMapping(value = { "archived/file" }, method = RequestMethod.POST)
    public WebServiceResponse<ArchivedFile> uploadArchivedFile(@RequestParam(value="file") MultipartFile multipartFile) throws FileArchiveException {
        return new WebServiceResponse<>(this.archivedFileService.saveMultipartFile(multipartFile));        
    }

}
