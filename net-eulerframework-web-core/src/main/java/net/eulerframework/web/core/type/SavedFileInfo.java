package net.eulerframework.web.core.type;

import java.io.File;

public class SavedFileInfo {

    private final String savedPath;
    private final File savedFile;
    public String getSavedPath() {
        return savedPath;
    }
    public File getSavedFile() {
        return savedFile;
    }
    
    public SavedFileInfo(String savedPath, File savedFile) {
        this.savedFile = savedFile;
        this.savedPath = savedPath;
    }
}
