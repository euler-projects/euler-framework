package net.eulerframework.web.module.file.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;
import net.eulerframework.common.util.MIMEUtils;
import net.eulerframework.common.util.MIMEUtils.MIME;
import net.eulerframework.common.util.StringUtils;
import net.eulerframework.common.util.io.file.FileUtils;
import net.eulerframework.common.util.io.file.SimpleFileIOUtils;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.core.base.response.easyuisupport.EasyUIAjaxResponse;
import net.eulerframework.web.core.exception.web.api.ResourceNotFoundException;
import net.eulerframework.web.module.file.entity.ArchivedFile;
import net.eulerframework.web.module.file.exception.FileArchiveException;
import net.eulerframework.web.module.file.service.ArchivedFileService;

@WebController
@RequestMapping("/")
public class FileUploadAndDownloadWebController extends JspSupportWebController {

    @Resource
    private ArchivedFileService archivedFileService;
    
    @ResponseBody
    @RequestMapping(
            path = {"file/{param}"}, 
            method = RequestMethod.GET)
    public void downloadArchivedFile(
            @PathVariable("param") String param) throws IOException {
        ArchivedFile archivedFile = this.getRequestFile(param);
        
        try {
            this.writeFile(archivedFile.getOriginalFilename(), archivedFile.getArchivedFile());
        } catch (FileNotFoundException e) {
            this.logger.warn(e.getMessage(), e);
            throw new ResourceNotFoundException();
        } catch (IOException e) {
            throw e;
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "image/{param}", method = RequestMethod.GET)
    public void downloadImage(
            @PathVariable("param") String param,
            @RequestParam(required = false, defaultValue = "-1") int perfectWidth,
            @RequestParam(required = false, defaultValue = "-1") int perfectHeight) throws IOException {
        
        if(perfectHeight <= 0 && perfectWidth <= 0) {
            this.downloadArchivedFile(param);
        } else {
            ArchivedFile archivedFile = this.getRequestFile(param);
            BufferedImage bufferedImage = ImageIO.read(archivedFile.getArchivedFile());

            int originalHeight = bufferedImage.getHeight();
            int originalWidth = bufferedImage.getWidth();

            double scale= 1;
            
            if (perfectHeight <= 0 && perfectWidth <= 0){ // 高宽都不为正,不缩放
                scale = 1;
            } else if (perfectHeight <= 0){ // 高不为正,以宽为基准缩放
                double scaleWidth = (double) perfectWidth / (double) originalWidth;
                scale = scaleWidth;
            } else if (perfectWidth <= 0){ // 宽不为正,以高为基准缩放
                double scaleHeight = (double) perfectHeight / (double) originalHeight;
                scale = scaleHeight;
            } else { // 其他情况以缩放比较大的为基准
                double scaleHeight = (double) perfectHeight / (double) originalHeight;
                double scaleWidth = (double) perfectWidth / (double) originalWidth;
                scale = scaleHeight > scaleWidth ? scaleHeight : scaleWidth;
            }
            
            if(scale > 1) { //不允许放大
                scale = 1;
            }

            try {
                if(scale == 1) {
                    this.writeFile(archivedFile.getOriginalFilename(), archivedFile.getArchivedFile());
                } else {
                    this.writeImage(archivedFile.getOriginalFilename(), archivedFile.getArchivedFile(), scale);
                }
            } catch (FileNotFoundException e) {
                this.logger.warn(e.getMessage(), e);
                throw new ResourceNotFoundException();
            } catch (IOException e) {
                throw e;
            }
        }
    }
    
    private ArchivedFile getRequestFile(String requestParam) {
        String extensions = FileUtils.extractFileExtension(requestParam);
        String archivedFileId = FileUtils.extractFileNameWithoutExtension(requestParam);
        ArchivedFile archivedFile = this.archivedFileService.findArchivedFile(archivedFileId);
        
        if(archivedFile == null)
            throw new ResourceNotFoundException();
        
        if(extensions != null) {
            if(!extensions.equalsIgnoreCase(archivedFile.getExtension())) {
                if(this.logger.isInfoEnabled()) {
                    this.logger.info("The file extension does not match, specified as " + extensions + ", actually " + archivedFile.getExtension());
                }
                throw new ResourceNotFoundException();
            }
        }

        String archivedFilePath = WebConfig.getUploadPath();
        
        if(archivedFile.getArchivedPathSuffix() != null)
            archivedFilePath += "/" + archivedFile.getArchivedPathSuffix();
        
        File file = new File(archivedFilePath, archivedFile.getArchivedFilename());
        archivedFile.setArchivedFile(file);
        return archivedFile;
    }
    
    protected void writeImage(String fileName, File file, double scale) throws FileNotFoundException, IOException {
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
        //response.setHeader("Transfer-Encoding", "chunked");
        //response.setHeader("Content-Length", String.valueOf(file.length()));
        
        if(scale != 1) {
            Thumbnails.of(file)
            .scale(scale) 
            .outputQuality(1f) 
            .toOutputStream(response.getOutputStream());
        } else {
            SimpleFileIOUtils.readFileToOutputStream(file, response.getOutputStream(), 2048);
        }
        
    }
    
    @ResponseBody
    @RequestMapping(value = "uploadFile", method = RequestMethod.POST)
    public EasyUIAjaxResponse<ArchivedFile> uploadArchivedFile(@RequestParam(value="file") MultipartFile multipartFile) throws FileArchiveException {
        return new EasyUIAjaxResponse<>(this.archivedFileService.saveMultipartFile(multipartFile));        
    }

}
