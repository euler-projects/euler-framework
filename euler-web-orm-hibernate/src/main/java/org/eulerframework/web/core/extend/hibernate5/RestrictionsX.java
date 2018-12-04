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
package org.eulerframework.web.core.extend.hibernate5;

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
