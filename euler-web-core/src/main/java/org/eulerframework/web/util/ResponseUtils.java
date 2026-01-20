package org.eulerframework.web.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.common.util.MIMEUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class ResponseUtils {

    public static void writeFileHeader(HttpHeaders headers, String fileName, Long fileSize) {
        writeFileHeader(headers, fileName, fileSize, false);
    }

    public static void writeFileHeader(HttpHeaders headers, String fileName, Long fileSize, boolean forceAttachment) {
        String extension = FilenameUtils.getExtension(fileName);
        MIMEUtils.MIME mime;
        if (StringUtils.hasText(extension)) {
            mime = MIMEUtils.getMIME(extension);
        } else {
            mime = MIMEUtils.getDefaultMIME();
        }
        headers.setContentType(MediaType.parseMediaType(mime.getContentType()));
        try {
            headers.setContentDisposition(ContentDisposition.parse((forceAttachment ? "attachment" : mime.getContentDisposition()) +
                    ";fileName=\"" + new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO8859-1") + "\""));
        } catch (UnsupportedEncodingException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        if (fileSize != null && fileSize > 0) {
            headers.setContentLength(fileSize);
        }
    }
}
