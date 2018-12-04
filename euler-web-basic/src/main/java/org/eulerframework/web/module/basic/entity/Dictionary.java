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
package org.eulerframework.web.module.basic.entity;

import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.eulerframework.web.core.base.entity.UUIDEntity;

@Entity
@Table(name = "sys_dict", uniqueConstraints = {
        @UniqueConstraint(columnNames={"code", "dict_key", "locale"}),
        @UniqueConstraint(columnNames={"code", "dict_value"}),
        @UniqueConstraint(columnNames={"code", "show_order", "locale"})})
public class Dictionary extends UUIDEntity<Dictionary> {

    @Column(name = "code", nullable = false)
    private String code;
    @NotNull
    @Column(name = "dict_key", nullable = false)
    private String key;
    @Column(name = "dict_value")
    private String value;
    @Column(name = "locale", nullable = false)
    private Locale locale;
    @Column(name = "show_order", nullable = false)
    private Integer showOrder;
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public Integer getShowOrder() {
        return showOrder;
    }
    public void setShowOrder(Integer showOrder) {
        this.showOrder = showOrder;
    }
    public Locale getLocale() {
        return locale;
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
}
