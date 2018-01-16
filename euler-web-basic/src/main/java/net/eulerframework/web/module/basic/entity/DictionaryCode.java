package net.eulerframework.web.module.basic.entity;

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

import net.eulerframework.web.core.base.entity.NonIDEntity;

/**
 * CODE_TABLE<br>
 * 用来存储JS字典数据或系统配置参数
 * @author cFrost
 *
 */
@Entity
@Table(name = "SYS_DICT_CODE")
public class DictionaryCode extends NonIDEntity<DictionaryCode, String> {

    @Id
    @Column(name = "CODE")
    private String code;
    @Column(name = "NAME", nullable = false, unique = true)
    private String name;
    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="CODE")
    @OrderBy(value="showOrder ASC")
    private List<Dictionary> dictionarys;
    
    @Override
    public String getId() {
        return code;
    }

    @Override
    public void setId(String id) {
        this.code = id;
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
