package net.eulerframework.web.module.file.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import net.eulerframework.common.util.io.FileReadException;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AbstractWebController;
import net.eulerframework.web.core.base.response.AjaxResponse;
import net.eulerframework.web.core.exception.ResourceNotFoundException;
import net.eulerframework.web.module.file.entity.ArchivedFile;
import net.eulerframework.web.module.file.exception.FileArchiveException;
import net.eulerframework.web.module.file.service.IArchivedFileService;

@WebController
@Scope("prototype")
@RequestMapping("/")
public class FileUploadAndDownloadWebController extends AbstractWebController {

    @Resource
    private IArchivedFileService archivedFileService;
    
    @RequestMapping(value = "file/{id}", method = RequestMethod.GET)
    public void downloadArchivedFile(@PathVariable("id") String archivedFileId, HttpServletResponse response) throws FileReadException, IOException {
        ArchivedFile archivedFile = this.archivedFileService.findArchivedFile(archivedFileId);
        
        if(archivedFile == null)
            throw new ResourceNotFoundException();
        
        String archivedFilePath = WebConfig.getUploadPath();
        
        if(archivedFile.getArchivedPathSuffix() != null)
            archivedFilePath += "/" + archivedFile.getArchivedPathSuffix();
        
        File file = new File(archivedFilePath, archivedFile.getArchivedFilename());
        String fileName = archivedFile.getOriginalFilename();
        
        this.setNoCacheHeader();
        
        try {
            this.writeFile(fileName, file);
        } catch (FileNotFoundException e) {
            throw new ResourceNotFoundException(e);
        } catch (FileReadException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "uploadFile", method = RequestMethod.POST)
    public AjaxResponse<ArchivedFile> uploadArchivedFile(@RequestParam(value="file") MultipartFile multipartFile) throws FileArchiveException {
        return new AjaxResponse<>(this.archivedFileService.saveMultipartFile(multipartFile));        
    }

}
