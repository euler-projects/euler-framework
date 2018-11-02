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

import java.util.ArrayList;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;

public class InExpressionX implements Criterion {
    private final String propertyName;
    private final Object[] values;

    /**
     * Constructs an InExpressionX
     *
     * @param propertyName The property name to check
     * @param values The values to check against
     *
     * @see RestrictionsX#in(String, java.util.Collection)
     * @see RestrictionsX#in(String, Object[])
     */
    protected InExpressionX(String propertyName, Object[] values) {
        this.propertyName = propertyName;
        this.values = values;
    }

    @Override
    public String toSqlString( Criteria criteria, CriteriaQuery criteriaQuery ) {
        final String[] columns = criteriaQuery.findColumns( propertyName, criteria );
        String cols = StringHelper.join( " = ? and ", columns ) + "= ?";
        cols = values.length > 0
                ? StringHelper.repeat( cols + " or ", values.length - 1 ) + cols
                : "";
        cols = " (" + cols + ") ";
        return cols;
    }

    @Override
    public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) {
        final ArrayList<TypedValue> list = new ArrayList<TypedValue>();
        final Type type = criteriaQuery.getTypeUsingProjection( criteria, propertyName );
        if ( type.isComponentType() ) {
            final CompositeType compositeType = (CompositeType) type;
            final Type[] subTypes = compositeType.getSubtypes();
            for ( Object value : values ) {
                for ( int i = 0; i < subTypes.length; i++ ) {
                    final Object subValue = value == null
                            ? null
                            : compositeType.getPropertyValues( value, EntityMode.POJO )[i];
                    list.add( new TypedValue( subTypes[i], subValue ) );
                }
            }
        }
        else {
            for ( Object value : values ) {
                list.add( new TypedValue( type, value ) );
            }
        }

        return list.toArray( new TypedValue[ list.size() ] );
    }

    @Override
    public String toString() {
        return propertyName + " in (" + StringHelper.toString( values ) + ')';
    }

}
