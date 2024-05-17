package org.eulerframework.web.model;

import org.eulerframework.common.util.Assert;
import org.eulerframework.web.core.base.response.BaseResponse;

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
