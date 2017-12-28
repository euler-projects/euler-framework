/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2017 cFrost.sun(孙宾, SUN BIN) 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * https://eulerproject.io
 * https://github.com/euler-projects/euler-framework
 * https://cfrost.net
 */
package net.eulerframework.constant;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cFrost
 *
 */
public enum EulerSysAttributes { 

    WEB_URL("__WEB_URL"),
    
    CONTEXT_PATH("__CONTEXT_PATH"),
    ASSETS_PATH("__ASSETS_PATH"),
    AJAX_PATH("__AJAX_PATH"),
    ADMIN_PATH("__ADMIN_PATH"),
    ADMIN_AJAX_PATH("__ADMIN_AJAX_PATH"),
    
    DEBUG_MODE("__DEBUG_MODE"),
    
    PROJECT_VERSION("__PROJECT_VERSION"),
    PROJECT_MODE("__PROJECT_MODE"),
    PROJECT_BUILDTIME("__PROJECT_BUILDTIME"),
    
    SITENAME("__SITENAME"),
    COPYRIGHT_HOLDER("__COPYRIGHT_HOLDER"),
    ADMIN_DASHBOARD_BRAND_ICON("__ADMIN_DASHBOARD_BRAND_ICON"),
    ADMIN_DASHBOARD_BRAND_TEXT("__ADMIN_DASHBOARD_BRAND_TEXT"),
    
    FRAMEWORK_VERSION("__FRAMEWORK_VERSION"),
    
    LOCALE_COOKIE_NAME("__LOCALE_COOKIE_NAME"),
    LOCALE("__LOCALE"),
    
    FILE_DOWNLOAD_PATH_ATTR("__FILE_DOWNLOAD_PATH"),
    IMAGE_DOWNLOAD_PATH_ATTR("__IMAGE_DOWNLOAD_PATH"),
    VIDEO_DOWNLOAD_PATH_ATTR("__VIDEO_DOWNLOAD_PATH"),
    FILE_UPLOAD_ACTION_ATTR("__FILE_UPLOAD_ACTION"),
    
    USER_INFO("__USER_INFO");
    
    private String value;
    
    EulerSysAttributes(String value) {
        this.value = value;
    }
    
    public String value() {
        return this.value;
    }
    
    public static Set<String> getEulerSysAttributeNames() {
        Class<EulerSysAttributes> clz = EulerSysAttributes.class;
        EulerSysAttributes[] eulerSysAttributes= clz.getEnumConstants();
        return Arrays.asList(eulerSysAttributes).stream().map(eulerSysAttribute -> eulerSysAttribute.value()).collect(Collectors.toSet());
    }
}
