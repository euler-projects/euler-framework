package net.eulerframework.web.module.file.conf;

import net.eulerframework.web.config.WebConfig;

/**
 * @author cFrost
 *
 */
public class FileConfig {
    
    public final static String FILE_DOWNLOAD_PATH = "/file";
    public final static String IMAGE_DOWNLOAD_PATH = "/image";
    public final static String VIDEO_DOWNLOAD_PATH = "/video";
    public final static String FILE_UPLOAD_ACTION = "/uploadFile";

    public static String getFileArchivedPath() {
        return WebConfig.getRuntimePath() + "/archived/file";
    }
}
