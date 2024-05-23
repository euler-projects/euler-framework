/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.core.base.controller;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.common.util.MIMEUtils;
import org.eulerframework.common.util.MIMEUtils.MIME;
import org.eulerframework.common.util.io.file.FileUtils;
import org.eulerframework.common.util.io.file.SimpleFileIOUtils;
import org.eulerframework.web.core.base.WebContextAccessible;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public abstract class AbstractWebController extends WebContextAccessible {
    protected void writeFileHeader(String fileName, long fileSize) {
        HttpServletResponse response = this.getResponse();
        String extension = FileUtils.extractFileExtension(fileName);
        MIME mime;
        if (StringUtils.hasText(extension)) {
            mime = MIMEUtils.getMIME(extension);
        } else {
            mime = MIMEUtils.getDefaultMIME();
        }
        this.getResponse().setHeader("Content-Type", mime.getContentType());
        try {
            response.setHeader("Content-Disposition", mime.getContentDisposition() +
                    ";fileName=\"" + new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO8859-1") + "\"");
        } catch (UnsupportedEncodingException e) {
            throw ExceptionUtils.<RuntimeException>rethrow(e);
        }
        response.setHeader("Content-Length", String.valueOf(fileSize));
    }

    protected void writeString(String string) throws IOException {
        this.getResponse().getOutputStream().write(string.getBytes(StandardCharsets.UTF_8));
    }

    protected void writeFile(String fileName, File file) throws FileNotFoundException, IOException {
        this.writeFileHeader(fileName, file.length());
        SimpleFileIOUtils.readFileToOutputStream(file, this.getResponse().getOutputStream(), 2048);
    }

    protected void setNoCacheHeader() {
        HttpServletResponse response = this.getResponse();
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Date", new Date().getTime());
        response.setIntHeader("Expires", 0);
    }
}
