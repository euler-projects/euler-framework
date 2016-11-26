package net.eulerframework.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropertyReader {

    private Properties props;

    public PropertyReader(String path) throws IOException{
        InputStream inputStream=this.getClass().getClassLoader().getResourceAsStream(path);
        try {
            BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream));
            this.props = new Properties();        
            this.props.load(bufferedReader);
        } catch (NullPointerException e) {
            throw new IOException("Property file \"" + this.getClass().getResource("")+path + "\" read error. Does this file exist?", e);
        }
//        this.props = PropertiesLoaderUtils.loadAllProperties(path);
    }

    public Object getProperty(String key) throws NullValueException {
        Object value = this.props.get(key);
        if (value == null){
            throw new NullValueException("Key read error, no such key: "+ key);
        }
        return value;
    }
}
