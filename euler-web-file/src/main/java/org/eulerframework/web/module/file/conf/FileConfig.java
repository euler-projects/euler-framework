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
package org.eulerframework.web.module.file.conf;

import org.eulerframework.web.config.WebConfig;

/**
 * @author cFrost
 *
 */
public class FileConfig {
    
    public final static String FILE_DOWNLOAD_PATH = "/file";
    public final static String IMAGE_DOWNLOAD_PATH = "/image";
    public final static String VIDEO_DOWNLOAD_PATH = "/video";
    public final static String FILE_UPLOAD_ACTION = "/uploadFile";

    public static String getFileArchivedPath() {
        return WebConfig.getRuntimePath() + "/archived/file";
    }
}
