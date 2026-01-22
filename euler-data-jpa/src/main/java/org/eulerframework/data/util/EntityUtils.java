package org.eulerframework.data.util;

import org.eulerframework.data.entity.ResourceEntity;
import org.eulerframework.model.AbstractResourceModel;
import org.eulerframework.model.ResourceModel;
import org.springframework.util.StringUtils;

import java.util.function.Consumer;

public class EntityUtils {
    /**
     * 更新可以为空的字符串实体字段
     * <p>
     * 若<code>newValue == null</code>, 则不更新; <code>newValue == ""</code>, 则更新为<code>null</code>;
     *
     * @param newValue 待更新的值
     * @param consumer Entity赋值回调
     */
    public static void updateNullableField(String newValue, Consumer<String> consumer) {
        if (newValue == null) {
            return;
        }

        if (StringUtils.hasLength(newValue)) {
            consumer.accept(newValue);
        } else {
            consumer.accept(null);
        }
    }

    public static void updateResourceModel(ResourceEntity entity, AbstractResourceModel model) {
        model.setTenantId(entity.getTenantId());
        model.setUserId(entity.getUserId());
        model.setCreatedBy(entity.getCreatedBy());
        model.setLastModifiedBy(entity.getModifiedBy());
        model.setCreatedDate(entity.getCreatedDate());
        model.setLastModifiedDate(entity.getModifiedDate());
    }
}
