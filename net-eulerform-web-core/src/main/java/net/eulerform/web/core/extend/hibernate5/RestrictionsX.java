package net.eulerform.web.core.extend.hibernate5;

import java.util.Collection;

import org.hibernate.criterion.Criterion;
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

    /**
     * Apply an "or" constraint to the named property.
     *
     * @param propertyName The name of the property
     * @param values The literal values to use in the OR restriction
     *
     * @return The Criterion
     *
     * @see InExpressionX
     */
    public static Criterion in(String propertyName, Object[] values) {
        return new InExpressionX( propertyName, values );
    }

    /**
     * Apply an "or" constraint to the named property.
     *
     * @param propertyName The name of the property
     * @param values The literal values to use in the OR restriction
     *
     * @return The Criterion
     *
     * @see InExpressionX
     */
    public static Criterion in(String propertyName, Collection<?> values) {
        return new InExpressionX( propertyName, values.toArray() );
    }
    
}
