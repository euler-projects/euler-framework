package net.eulerframework.web.config;

public class MultiPartConfig {
    private final String location;
    private final long maxFileSize;
    private final long maxRequestSize;
    private final int fileSizeThreshold;
    
    protected MultiPartConfig(String location, long maxFileSize, long maxRequestSize, int fileSizeThreshold) {
        this.location = location;
        this.maxFileSize = maxFileSize;
        this.maxRequestSize = maxRequestSize;
        this.fileSizeThreshold = fileSizeThreshold;
    }
    public String getLocation() {
        return location;
    }
    public long getMaxFileSize() {
        return maxFileSize;
    }
    public long getMaxRequestSize() {
        return maxRequestSize;
    }
    public int getFileSizeThreshold() {
        return fileSizeThreshold;
    }
    
}
