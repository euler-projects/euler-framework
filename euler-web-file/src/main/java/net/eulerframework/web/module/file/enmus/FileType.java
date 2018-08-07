package net.eulerframework.web.module.file.enmus;

/**
 * @author cFrost
 *
 */
public enum FileType {
    IMAGE_FILES("image", "jpeg,gif,jpg,png,bmp,pic"),
    ALL_FILES("all", ".*");
    
    FileType(String title, String extensions) {
        this.title = title;
        this.extensions = extensions;
    }
    
    private String title;
    private String extensions;
    
    public String getTitle() {
        return this.title;
    }
    
    public String getExtensions() {
        return this.extensions;
    }
    
    public String toJson() {
        return "{\"title\":\"" + this.getTitle() + "\", \"extensions\":\"" + this.getExtensions() + "\"}";
    }
}
