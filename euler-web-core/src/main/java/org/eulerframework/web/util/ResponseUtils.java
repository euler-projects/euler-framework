package org.eulerframework.web.util;

import org.apache.commons.io.FilenameUtils;
import org.eulerframework.common.util.MIMEUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

public class ResponseUtils {

    public static void writeFileHeader(HttpHeaders headers, String filename, Long fileSize) {
        writeFileHeader(headers, filename, fileSize, false);
    }

    public static void writeFileHeader(HttpHeaders headers, String filename, Long fileSize, boolean forceAttachment) {
        String extension = FilenameUtils.getExtension(filename);
        MIMEUtils.MIME mime;
        if (StringUtils.hasText(extension)) {
            mime = MIMEUtils.getMIME(extension);
        } else {
            mime = MIMEUtils.getDefaultMIME();
        }
        headers.setContentType(MediaType.parseMediaType(mime.getContentType()));
        headers.setContentDisposition(rfc6266ContentDisposition(forceAttachment ? "attachment" : mime.getContentDisposition(), filename));
        if (fileSize != null && fileSize > 0) {
            headers.setContentLength(fileSize);
        }
    }

    public static ContentDisposition rfc6266ContentDisposition(String filename) {
        return rfc6266ContentDisposition(filename, false);
    }

    public static ContentDisposition rfc6266ContentDisposition(String filename, boolean forceAttachment) {
        String extension = FilenameUtils.getExtension(filename);
        MIMEUtils.MIME mime;
        if (StringUtils.hasText(extension)) {
            mime = MIMEUtils.getMIME(extension);
        } else {
            mime = MIMEUtils.getDefaultMIME();
        }
        return rfc6266ContentDisposition(forceAttachment ? "attachment" : mime.getContentDisposition(), filename);
    }

    private static ContentDisposition rfc6266ContentDisposition(String type, String filename) {
        Assert.isTrue("attachment".equalsIgnoreCase(type) || "inline".equalsIgnoreCase(type),
                "Type of the response Content-Disposition must be 'inline' or 'attachment'");
        return ContentDisposition.builder(type)
                .filename(filename, StandardCharsets.UTF_8)
                .build();
    }

    public static String legacyContentDisposition(String filename) {
        return legacyContentDisposition(filename, false);
    }

    public static String legacyContentDisposition(String filename, boolean forceAttachment) {
        String extension = FilenameUtils.getExtension(filename);
        MIMEUtils.MIME mime;
        if (StringUtils.hasText(extension)) {
            mime = MIMEUtils.getMIME(extension);
        } else {
            mime = MIMEUtils.getDefaultMIME();
        }
        return legacyContentDisposition(filename, mime, forceAttachment);
    }

    private static String legacyContentDisposition(String filename, MIMEUtils.MIME mime, boolean forceAttachment) {
        String rfc5987Filename = java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        String hackedFilename = new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);

        return (forceAttachment ? "attachment" : mime.getContentDisposition()) +
                ";filename=\"" + hackedFilename + "\"" +
                ";filename*=UTF-8''" + rfc5987Filename;
    }
}
