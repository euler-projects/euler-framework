package net.eulerform.web.core.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.multipart.MultipartFile;

import net.eulerform.common.CalendarTool;
import net.eulerform.common.GlobalProperties;
import net.eulerform.common.GlobalPropertyReadException;
import net.eulerform.web.core.exception.MultipartFileSaveException;

public class WebFileTool {

	public static File saveMultipartFile(MultipartFile multipartFile) throws MultipartFileSaveException {
        try {
            ServletContext scx = ContextLoader.getCurrentWebApplicationContext().getServletContext();

            String uploadPath = scx.getRealPath(GlobalProperties.get(GlobalProperties.UPLOAD_PATH));
        
    	    File targetDir = new File(uploadPath);
    		String sourceFileName = multipartFile.getOriginalFilename();
//    		sourceFileName = FilePathTool.changeToUnixFormat(sourceFileName);
    		int dot = sourceFileName.lastIndexOf('.');
//            int slash = sourceFileName.lastIndexOf('/');
            
            String prefix = CalendarTool.formatDate(new Date(), "yyyyMMddHHmmss-");
            
    		String extension = "";
            if(dot > -1)
                extension = sourceFileName.substring(sourceFileName.lastIndexOf('.'));
            
            String targetFileName = prefix + UUID.randomUUID().toString() + extension;
            
    		File targetFile = new File(uploadPath, targetFileName);
    		if (!targetDir.exists()) {
    			targetDir.mkdirs();
    		}
    		multipartFile.transferTo(targetFile);
    		return targetFile;
        } catch (GlobalPropertyReadException | IllegalStateException | IOException e) {
            throw new MultipartFileSaveException(e);
        }
	}

}
