package org.eulerframework.web.util;

import org.apache.commons.io.FilenameUtils;
import org.eulerframework.common.util.MIMEUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ResponseUtilsTest {
    @Test
    public void rfc6266ContentDisposition() {
        String filename = "Unicode文件名😂.mp3";
        ContentDisposition contentDisposition = ResponseUtils.rfc6266ContentDisposition(filename);
        Assert.assertEquals(filename, contentDisposition.getFilename());
    }

    @Test
    public void legacyContentDisposition() {
        String filename = "Unicode文件名😂.mp3";

        String exceptedContentDisposition = "attachment;" +
                "filename=\"" + new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1) + "\";" +
                "filename*=" + StandardCharsets.UTF_8 + "''" + URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        String contentDispositionStr = ResponseUtils.legacyContentDisposition(filename);
        ContentDisposition contentDisposition = ContentDisposition.parse(contentDispositionStr);
        Assert.assertEquals(exceptedContentDisposition, contentDispositionStr);
        Assert.assertEquals(filename, contentDisposition.getFilename());
    }

    @Test
    public void setResponseHeaders() {
        String filename = "Unicode文件名😂.mp3";
        MIMEUtils.MIME mime = MIMEUtils.getMIME(FilenameUtils.getExtension(filename));
        long fileSize = 1024;
        HttpHeaders headers = new HttpHeaders();
        ResponseUtils.writeFileHeader(headers, filename, fileSize, false);
        Assert.assertEquals(3, headers.size());
        Assert.assertNotNull(headers.getContentType());
        Assert.assertNotNull(headers.getContentDisposition());
        Assert.assertEquals(filename, headers.getContentDisposition().getFilename());
        Assert.assertEquals(fileSize, headers.getContentLength());
        Assert.assertEquals(mime.getContentType(), headers.getContentType().toString());
    }
}
