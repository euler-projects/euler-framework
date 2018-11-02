/*
 * Copyright 2013-2018 the original author or authors.
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
package org.eulerframework.web.core.base.request;

import javax.servlet.http.HttpServletRequest;

import org.eulerframework.common.util.StringUtils;

public class PageQueryRequest extends QueryRequest {

    private static final String PAGE_SIZE_NAME = "pageSize";
    private static final String PAGE_INDEX_NAME = "pageIndex";

//    public static final String EASYUI_PAGE_SIZE_NAME = "rows";
//    public static final String EASYUI_PAGE_INDEX_NAME = "page";
    
    private int pageIndex;
    private int pageSize;

    public PageQueryRequest(HttpServletRequest request) {
        this(request, PAGE_INDEX_NAME, PAGE_SIZE_NAME);
    }
    public PageQueryRequest(HttpServletRequest request, String pageIndexParamName, String pageSizeParamName) {
        super(request);
        
        String pageSizeStr = request.getParameter(pageSizeParamName);        
        if(StringUtils.isNull(pageSizeStr)){
            throw new IllegalArgumentException("Param '" +pageSizeParamName+ "' is required");
        }        
        this.pageSize = Integer.parseInt(request.getParameter(pageSizeParamName));
        
        if(this.pageSize <= 0) {
            this.pageIndex = -1;
        } else {
            String pageIndexStr = request.getParameter(pageIndexParamName);        
            if(StringUtils.isNull(pageIndexStr)){
                throw new IllegalArgumentException("Param '" +pageIndexParamName+ "' is required");
            }        
            this.pageIndex = Integer.parseInt(request.getParameter(pageIndexParamName));
            
            if(this.pageIndex < 0) {
                throw new IllegalArgumentException("Param '" +pageIndexParamName+ "' must larger than 0");                
            }
        }
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }
    
    public boolean enablePageQuery() {
        return this.pageSize > 0;
    }
}
