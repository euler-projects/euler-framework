package org.eulerframework.web.config;

import org.eulerframework.common.util.CommonUtils;
import org.eulerframework.common.util.StringUtils;
import org.eulerframework.web.util.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.FileSystemException;
import java.util.function.Supplier;

public class ConfigUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtils.class);

    /**
     * 处理应用程序运行需要用到的各种系统目录, 例如RuntimePath, TmpPath.
     * 如果当前运行环境是Windows, 而目录没有指定盘符, 则默认放到C盘
     * 如果目录不存在将会创建该目录.
     *
     * @param path                 要处理的目录路径
     * @param defaultIfPathIsEmpty 当path为空时使用的默认目录路径
     * @param propertyName         配置项名称
     * @param mkDirsIfNotExits     目录不存在时自动创建目录
     * @return 处理后的可用目录路径
     * @throws FileSystemException 当尝试创建目录失败时会抛出此异常
     */
    public static String handleApplicationPath(String path, Supplier<String> defaultIfPathIsEmpty, String propertyName, boolean mkDirsIfNotExits) throws FileSystemException {
        if (StringUtils.isEmpty(path)) {
            path = defaultIfPathIsEmpty.get();
            LOGGER.info("'{}' is not configured, use {} as the default.", propertyName, path);
        }

        path = CommonUtils.convertDirToUnixFormat(path, false);

        if (path.startsWith("file://")) {
            path = path.substring("file://".length());
        }

        if (path.startsWith("/")) {
            // *nix 绝对路径，除Windows外不做任何处理
            if (SystemUtils.isWindows()) {
                // 当配置的路径为*inx格式的绝对路径，且当前环境是Windows时，默认放在C盘
                LOGGER.warn("Application is running under Windows. '{}' does not specify a partition, use C: for default", propertyName);
                path = "C:" + path;
            }
        } else if (SystemUtils.isWindows() && path.matches("^\\w+:/.*$")) {
            // Windows 绝对路径，不做任何处理
        } else {
            // 相对路径，不做任何处理
        }

        LOGGER.info("'{}' is '{}'.", propertyName, path);

        File pathFile = new File(path);
        if (mkDirsIfNotExits && !pathFile.exists()) {
            LOGGER.info("Path '{}' not exits, create it.", path);
            if (!pathFile.mkdirs()) {
                throw new FileSystemException(path, null, "Path create failed, pleas check the permissions of this path");
            }
            LOGGER.info("Path '{}' created.", pathFile.getAbsolutePath());
        }

        return path;
    }
}
