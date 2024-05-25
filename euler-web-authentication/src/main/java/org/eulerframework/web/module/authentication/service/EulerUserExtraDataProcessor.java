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
package org.eulerframework.web.module.authentication.service;

import java.util.Map;

/**
 * @author cFrost
 *
 */
public interface EulerUserExtraDataProcessor {

    /**
     * 处理用户的附加数据, 处理成功返回{@code true}, 失败返回{@code false}
     * @param userId 对应用户的用户ID
     * @param extraData 附加数据
     * @return 处理成功返回{@code true}, 失败返回{@code false}
     */
    boolean process(String userId, Map<String, Object> extraData);

    /**
     * @param userId
     */
    Map<String, Object> loadExtraData(String userId);
}
