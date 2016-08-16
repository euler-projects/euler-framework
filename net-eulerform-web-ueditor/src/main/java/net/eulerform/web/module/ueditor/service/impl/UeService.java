package net.eulerform.web.module.ueditor.service.impl;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.eulerform.common.GlobalProperties;
import net.eulerform.common.GlobalPropertyReadException;
import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.core.exception.MultipartFileSaveException;
import net.eulerform.web.core.util.WebFileTool;
import net.eulerform.web.module.ueditor.entity.UeConfig;
import net.eulerform.web.module.ueditor.entity.UeImgUploadResult;
import net.eulerform.web.module.ueditor.service.IUeService;

@Service
public class UeService extends BaseService implements IUeService {

    @Override
    public UeConfig config(HttpServletRequest request) {
        UeConfig c = new UeConfig();
        c.setImageActionName("uploadImg");
        c.setImageFieldName("file");
        c.setImageUrlPrefix(request.getContextPath());
        return c;
    }

    @Override
    public UeImgUploadResult uploadImg(HttpServletRequest request, MultipartFile file) throws MultipartFileSaveException {
        File savedImg = WebFileTool.saveMultipartFile(file);
        String uploadPath;
        try {
            uploadPath = GlobalProperties.get(GlobalProperties.UPLOAD_PATH);
        } catch (GlobalPropertyReadException e) {
            throw new RuntimeException(e);
        }
        
        String sourceFileName = file.getOriginalFilename();
        int dot = sourceFileName.lastIndexOf('.');
        
        String extension = "";
        if(dot > -1)
            extension = sourceFileName.substring(sourceFileName.lastIndexOf('.'));
        
        UeImgUploadResult ret = new UeImgUploadResult();
        ret.setOriginal(file.getOriginalFilename());
        ret.setSize(String.valueOf(savedImg.length()));
        ret.setState("SUCCESS");
        ret.setTitle(savedImg.getName());
        ret.setType(extension);
        ret.setUrl(uploadPath+"/"+savedImg.getName());
        return ret;
        
    }
    
}
