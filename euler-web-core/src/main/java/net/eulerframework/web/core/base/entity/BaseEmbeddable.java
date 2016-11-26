package net.eulerframework.web.core.base.entity;

import java.io.Serializable;

/**
 * 复合组件接口<br>
 * 
 * @param <T> 实现类类名&nbsp;例如:<br><code>public class Example implements BaseEmbeddable&lt;Example&gt;<code>
 * @author cFrost
 */
public interface BaseEmbeddable<T extends BaseEmbeddable<?>> extends Serializable, Comparable<T> {

}
