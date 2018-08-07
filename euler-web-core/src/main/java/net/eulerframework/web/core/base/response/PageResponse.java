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
package net.eulerframework.web.core.base.response;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来作为分页查询的响应实体
 * 
 * @author cFrost
 *
 * @param <T> 返回数据类型
 */
public class PageResponse<T> implements BaseResponse {

    private final List<T> rows;

    private final long total;
    
    private final int pageIndex;
    
    private final int pageSize;
    
    /**
     * 新建分页响应实体
     * @param rows 分页数据
     * @param total 数据总数
     * @param pageIndex 页码
     * @param pageSize 每页数据量
     */
    public PageResponse(List<T> rows, long total, int pageIndex, int pageSize) {
        if(rows == null)
            this.rows = new ArrayList<>();
        else
            this.rows = rows;
        
        this.total = total;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }
    
    public List<T> getRows() {
        return rows;
    }
    
    public Long getTotal() {
        return total;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }
}
