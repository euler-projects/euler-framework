package net.eulerform.web.core.util;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public class WebFileTool {

	public static File saveMultipartFile(MultipartFile multipartFile, String path) throws IllegalStateException, IOException {
		File targetDir = new File(path);
		File targetFile = new File(path, UUID.randomUUID().toString());
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}
		multipartFile.transferTo(targetFile);
		return targetFile;
	}

}
