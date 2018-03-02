package net.eulerframework.web.core.extend;

import org.hibernate.dialect.MySQL5InnoDBDialect;

public class MySQL5InnoDBUTF8Dialect extends MySQL5InnoDBDialect {

    @Override
    public String getTableTypeString() {
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8";
    }

}
