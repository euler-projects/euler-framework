package net.eulerform.web.core.security.entity;

/**
 * 具有逻辑删除和修改人记录功能的基础实体接口<br>
 * 实现时可扩展{@link net.eulerform.web.core.security.entity.BaseModifyInfoEntity}的某种实现以实现某种主键策略, 
 * 并<strong>获得修改人记录</strong>功能，否则必须<strong>自行实现修改人记录</strong>功能
 * 
 * @param <T> 实现类类名&nbsp;例如:<br>
 * <code>public class Example implements BaseLogicDelEntity&lt;Example&gt;<code>
 * <br><strong>OR</strong><br>
 * <code>public class Example extends IDModifyInfoEntity&lt;Example&gt; implements BaseLogicDelEntity&lt;Example&gt;<code>
 * @author cFrost
 * @see net.eulerform.web.core.security.entity.NonIDLogicDelEntity
 * @see net.eulerform.web.core.security.entity.IDLogicDelEntity
 * @see net.eulerform.web.core.security.entity.UUIDLogicEntity
 */
public interface BaseLogicDelEntity<T extends BaseLogicDelEntity<?>> extends BaseModifyInfoEntity<T> {

    /**
     * 获取删除标记
     * @return
     */
    public Boolean getIfDel();

    /**
     * 设置删除标记
     * @param ifDel
     */
    public void setIfDel(Boolean ifDel);
}
