package net.eulerframework.web.module.file.controller;

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

import net.eulerframework.common.util.io.file.FileReadException;
import net.eulerframework.common.util.io.file.FileUtils;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.core.base.response.easyuisupport.EasyUIAjaxResponse;
import net.eulerframework.web.core.exception.web.api.ResourceNotFoundException;
import net.eulerframework.web.module.file.conf.FileConfig;
import net.eulerframework.web.module.file.enmus.FileType;
import net.eulerframework.web.module.file.entity.ArchivedFile;
import net.eulerframework.web.module.file.exception.FileArchiveException;
import net.eulerframework.web.module.file.service.ArchivedFileService;

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
        this.getRequest().setAttribute("maxFileSize", WebConfig.getMultiPartConfig().getMaxFileSize() / 1024 / 1014);
        return this.display("/common/plupload");
    }
    
    @ResponseBody
    @RequestMapping(value = "file/{param}", method = RequestMethod.GET)
    public void downloadArchivedFile(
            @PathVariable("param") String param) throws FileReadException, IOException {
        ArchivedFile archivedFile = this.getRequestFile(param);
        
        this.setNoCacheHeader();
        
        try {
            this.writeFile(archivedFile.getOriginalFilename(), archivedFile.getArchivedFile());
        } catch (FileNotFoundException e) {
            throw new ResourceNotFoundException(e);
        } catch (FileReadException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "image/{param}", method = RequestMethod.GET)
    public void image(@PathVariable("param") String param) throws FileReadException, IOException {
        ArchivedFile archivedFile = this.getRequestFile(param);
        
        this.setNoCacheHeader();
        
        try {
            this.writeFile(archivedFile.getOriginalFilename(), archivedFile.getArchivedFile());
        } catch (FileNotFoundException e) {
            throw new ResourceNotFoundException(e);
        } catch (FileReadException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }
    
    private ArchivedFile getRequestFile(String requestParam) {
        String extensions = FileUtils.extractFileExtension(requestParam);
        String archivedFileId = FileUtils.extractFileNameWithoutExtension(requestParam);
        ArchivedFile archivedFile = this.archivedFileService.findArchivedFile(archivedFileId);
        
        if(archivedFile == null)
            throw new ResourceNotFoundException("File id is '" + archivedFileId + "' not exists.");
        
        if(extensions != null) {
            if(!extensions.equals(archivedFile.getExtension())) {
                throw new ResourceNotFoundException("The file extension does not match, specified as " + extensions + ", actually " + archivedFile.getExtension());
            }
        }
        
        String archivedFilePath = FileConfig.getFileArchivedPath();
        
        if(archivedFile.getArchivedPathSuffix() != null)
            archivedFilePath += archivedFile.getArchivedPathSuffix();
        
        File file = new File(archivedFilePath, archivedFile.getArchivedFilename());
        archivedFile.setArchivedFile(file);
        return archivedFile;
    }
    
    @ResponseBody
    @RequestMapping(value = "uploadFile", method = RequestMethod.POST)
    public EasyUIAjaxResponse<ArchivedFile> uploadArchivedFile(@RequestParam(value="file") MultipartFile multipartFile) throws FileArchiveException {
        return new EasyUIAjaxResponse<>(this.archivedFileService.saveMultipartFile(multipartFile));        
    }

}
