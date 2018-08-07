package net.eulerframework.web.module.authentication.service;

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
