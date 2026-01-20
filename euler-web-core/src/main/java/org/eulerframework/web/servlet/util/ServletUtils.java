/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.servlet.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.common.util.MIMEUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ServletUtils {

    public static ServletContext getServletContext() {
        return ServletContextHolder.getServletContext();
    }

    public static ServletRequestAttributes getServletRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    public static HttpServletRequest getRequest() {
        return Optional.ofNullable(getServletRequestAttributes())
                .map(ServletRequestAttributes::getRequest)
                .orElse(null);
    }

    public static HttpSession getSession() {
        return Optional.ofNullable(getServletRequestAttributes())
                .map(ServletRequestAttributes::getRequest)
                .map(HttpServletRequest::getSession)
                .orElse(null);
    }

    public static HttpServletResponse getResponse() {
        return Optional.ofNullable(getServletRequestAttributes()).map(ServletRequestAttributes::getResponse).orElse(null);
    }

    public static String getWebDomain() {
        HttpServletRequest request = getRequest();
        StringBuffer url = request.getRequestURL();
        String uri = request.getRequestURI();
        return url.delete(url.length() - uri.length(), url.length()).toString();
    }

    public static String findRealURI(HttpServletRequest httpServletRequest) {
        String requestURI = httpServletRequest.getRequestURI();
        String contextPath = httpServletRequest.getContextPath();
        return requestURI.replaceFirst(contextPath, "");
    }

    public static String getRealIP() {
        return getRealIP(getRequest());
    }

    public static String getRealIP(HttpServletRequest httpServletRequest) {
        String ip = httpServletRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getRemoteAddr();
        }
        return ip;
    }

    public static  void writeString(HttpServletResponse response, String string) throws IOException {
        response.getOutputStream().write(string.getBytes(StandardCharsets.UTF_8));
    }

    public static  void writeFile(HttpServletResponse response, File file, String fileName) throws IOException {
        writeFile(response, file, fileName, false);
    }

    public static  void writeFile(HttpServletResponse response, File file, String fileName, boolean forceAttachment) throws IOException {
        ServletUtils.writeFileHeader(response, fileName, file.length(), forceAttachment);
        try (InputStream in = FileUtils.openInputStream(file)) {
            IOUtils.copy(in, response.getOutputStream());
        }
    }

    public static void writeFileHeader(HttpServletResponse response, String fileName, Long fileSize) {
        writeFileHeader(response, fileName, fileSize, false);
    }

    public static void writeFileHeader(HttpServletResponse response, String fileName, Long fileSize, boolean forceAttachment) {
        String extension = FilenameUtils.getExtension(fileName);
        MIMEUtils.MIME mime;
        if (StringUtils.hasText(extension)) {
            mime = MIMEUtils.getMIME(extension);
        } else {
            mime = MIMEUtils.getDefaultMIME();
        }
        response.setHeader("Content-Type", mime.getContentType());
        try {
            response.setHeader("Content-Disposition", (forceAttachment ? "attachment" : mime.getContentDisposition()) +
                    ";fileName=\"" + new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO8859-1") + "\"");
        } catch (UnsupportedEncodingException e) {
            throw ExceptionUtils.<RuntimeException>rethrow(e);
        }
        if (fileSize != null && fileSize > 0) {
            response.setHeader("Content-Length", String.valueOf(fileSize));
        }
    }
}
