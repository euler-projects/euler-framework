package net.eulerform.web.core.extend.hibernate5;

import org.hibernate.criterion.MatchMode;

public class RestrictionsX {
    
    private static final Character ESCAPE_CHAR = '/';

    public static LikeExpressionX like(String propertyName, String value) {
        return new LikeExpressionX(
                propertyName, 
                replaceSpecialChar(value), 
                ESCAPE_CHAR
                );
    }

    public static LikeExpressionX like(String propertyName, String value, MatchMode matchMode) {
        return new LikeExpressionX(
                propertyName, 
                matchMode.toMatchString(
                        replaceSpecialChar(value)
                        ), 
                ESCAPE_CHAR
                );
    }
    
    private static String replaceSpecialChar(String value){
        String escapeCharStr = ESCAPE_CHAR.toString();
        return value
                .replace(escapeCharStr, escapeCharStr + escapeCharStr)
                .replace("_", escapeCharStr+"_")
                .replace("%", escapeCharStr+"%");       
    }
    
}
