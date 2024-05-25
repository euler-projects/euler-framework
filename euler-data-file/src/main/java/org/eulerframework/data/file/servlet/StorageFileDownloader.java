package org.eulerframework.data.file.servlet;

import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.core.function.Handler;

import java.io.IOException;

public interface StorageFileDownloader extends Handler<String> {
    void download(String fileId, HttpServletResponse response) throws IOException;
}
