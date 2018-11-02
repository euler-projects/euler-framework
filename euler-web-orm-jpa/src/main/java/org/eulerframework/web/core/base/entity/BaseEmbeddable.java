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
package org.eulerframework.web.core.base.entity;

import java.io.Serializable;

/**
 * 复合组件接口<br>
 * 
 * @param <T> 实现类类名&nbsp;例如:<br><code>public class Example implements BaseEmbeddable&lt;Example&gt;<code>
 * @author cFrost
 */
public interface BaseEmbeddable<T extends BaseEmbeddable<?>> extends Serializable, Comparable<T> {

}
