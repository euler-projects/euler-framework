package net.eulerframework.web.core.base.entity;

import java.util.Date;

import net.eulerframework.web.core.base.entity.BaseEntity;

/**
 * 具有修改人记录功能的基础实体接口<br>
 * 可记录数据的创建人,创建时间,修改人,修改时间<br>
 * 实现时可扩展{@link net.eulerframework.web.core.base.entity.BaseEntity}的某种实现以实现某种主键策略
 * 
 * @param <T> 实现类类名&nbsp;例如:<br>
 * <code>public class Example implements BaseModifyInfoEntity&lt;Example&gt;<code>
 * <br><strong>OR</strong><br>
 * <code>public class Example extends IDEntity&lt;Example&gt; implements BaseModifyInfoEntity&lt;Example&gt;<code>
 * @author cFrost
 * @see NonIDModifyInfoEntity
 * @see IDModifyInfoEntity
 * @see UUIDModifyInfoEntity
 */
public interface BaseModifyInfoEntity<T extends BaseModifyInfoEntity<?>> extends BaseEntity<T> {

    /**
     * 获取记录创建者<br>
     * 一般为用户的ID或UUID
     * @return
     */
    public String getCreateBy();

    /**
     * 设置记录创建者<br>
     * 一般传入用户的ID或UUID
     * @param createBy
     */
    public void setCreateBy(String createBy);

    /**
     * 获取记录修改者<br>
     * 一般为用户的ID或UUID
     * @return
     */
    public String getModifyBy();

    /**
     * 设置记录修改者<br>
     * 一般传入用户的ID或UUID
     * @param modifyBy
     */
    public void setModifyBy(String modifyBy);

    /**
     * 创建时间<br>
     * @return
     */
    public Date getCreateDate();

    /**
     * 创建时间<br>
     * @param createDate
     */
    public void setCreateDate(Date createDate);

    /**
     * 修改时间<br>
     * @return
     */
    public Date getModifyDate();

    /**
     * 修改时间<br>
     * @param modifyDate
     */
    public void setModifyDate(Date modifyDate) ;
    
    /**
     * 获取记录创建者用于显示<br>
     * 一般用户的姓名，不做数据库字段映射
     * @return
     */
    public String getCreateByName();
    
    /**
     * 设置记录创建者姓名<br>
     * 一般传入用户的姓名用于页面显示，不做数据库字段映射
     * @param createByName
     */
    public void setCreateByName(String createByName);
    
    /**
     * 获取记录修改者用于显示<br>
     * 一般为用户的姓名，不做数据库字段映射
     * @return
     */
    public String getModifyByName();
    
    /**
     * 设置记录修改者姓名<br>
     * 一般传入用户的姓名用于页面显示，不做数据库字段映射
     * @param modifyByName
     */
    public void setModifyByName(String modifyByName);
}
