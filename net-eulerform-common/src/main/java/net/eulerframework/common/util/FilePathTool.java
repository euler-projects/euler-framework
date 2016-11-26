package net.eulerframework.common.util;

public abstract class FilePathTool {
    
    /**
     * 统一路径为UNIX格式,结尾的"/"会去掉<br>
     * 如果只有一个"/"，则会保留<br>
     * <p>
     * Examples:
     * <blockquote><pre>
     * changeToUnixFormat("\") returns "/"
     * changeToUnixFormat("D:\floder\") returns "D:/floder"
     * changeToUnixFormat("D:\floder\file") returns "D:/floder/file"
     * </pre></blockquote>
     * @param path 原始路径
     * @return unix路径
     */
    public static String changeToUnixFormat(String path){
        if(path == null)
            return null;
        
        String unixPath =  path.replace("\\", "/");
        if(unixPath.endsWith("/") && unixPath.length()>1){
            unixPath = unixPath.substring(0, unixPath.length()-1);
        }
        return unixPath;
    }

}

