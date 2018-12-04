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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.eulerframework.web.core.base.entity.NonIDEntity;

/**
 * CODE_TABLE<br>
 * 用来存储JS字典数据或系统配置参数
 * @author cFrost
 *
 */
@Entity
@Table(name = "sys_dict_code")
public class DictionaryCode extends NonIDEntity<DictionaryCode, String> {

    @Id
    @Column(name = "code")
    private String code;
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="code")
    @OrderBy(value="showOrder ASC")
    private List<Dictionary> dictionarys;
    
    @Override
    public String getId() {
        return code;
    }
    
    @Override
    public void setId(String id) {
        this.setCode(id);
    }
    
    @Override
    public int compareTo(DictionaryCode o) {
        return this.getId().compareTo(o.getId());
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<Dictionary> getDictionarys() {
        return dictionarys;
    }
    public void setDictionarys(List<Dictionary> dictionarys) {
        this.dictionarys = dictionarys;
    }
    
}
