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
package org.eulerframework.web.model;

import org.eulerframework.web.core.base.response.BaseResponse;
import org.springframework.util.Assert;

import java.util.List;

public class PageResponse<T> implements BaseResponse {
    private final List<T> rows;
    private final int pageNum;
    private final int pageSize;
    private final long total;

    public PageResponse(List<T> rows, int pageNum, int pageSize, long total) {
        this.rows = rows;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotal() {
        return total;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private List<T> rows;
        private int pageNum;
        private int pageSize;
        private long total;

        public Builder<T> rows(List<T> rows) {
            this.rows = rows;
            return this;
        }

        public Builder<T> pageNum(int pageNum) {
            this.pageNum = pageNum;
            return this;
        }

        public Builder<T> pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder<T> total(long total) {
            this.total = total;
            return this;
        }

        public PageResponse<T> build() {
            Assert.notNull(rows, "rows must not be null");
            return new PageResponse<>(rows, pageNum, pageSize, total);
        }
    }
}
