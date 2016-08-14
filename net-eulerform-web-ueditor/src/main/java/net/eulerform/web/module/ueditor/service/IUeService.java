package net.eulerform.web.module.ueditor.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.core.exception.MultipartFileSaveException;
import net.eulerform.web.module.ueditor.entity.UeConfig;
import net.eulerform.web.module.ueditor.entity.UeImgUploadResult;

public interface IUeService extends IBaseService {
    
    public UeConfig config(HttpServletRequest request);
    
    public UeImgUploadResult uploadImg(HttpServletRequest request, MultipartFile file) throws MultipartFileSaveException;

}
