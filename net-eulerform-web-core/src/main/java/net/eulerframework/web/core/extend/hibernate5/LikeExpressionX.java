package net.eulerframework.web.core.extend.hibernate5;

import java.util.Locale;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.TypedValue;

@SuppressWarnings("serial")
public class LikeExpressionX implements Criterion {
    private final String propertyName;
    private final Object value;
    private final Character escapeChar;
    private boolean ignoreCase;
    
    protected LikeExpressionX(String propertyName, String value, Character escapeChar) {
        this.propertyName = propertyName;
        this.value = value;
        this.escapeChar = escapeChar;
    }

    protected LikeExpressionX(String propertyName, String value) {
        this(propertyName, value, null);
    }

    @Override
    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
        final Dialect dialect = criteriaQuery.getFactory().getDialect();
        final String[] columns = criteriaQuery.findColumns(propertyName, criteria);
        if (columns.length != 1) {
            throw new HibernateException("Like may only be used with single-column properties");
        }

        final String escape = escapeChar == null ? "" : " escape \'" + escapeChar + "\'";
        final String column = columns[0];
        if (ignoreCase) {
            if (dialect.supportsCaseInsensitiveLike()) {
                return column + " " + dialect.getCaseInsensitiveLike() + " ?" + escape;
            } else {
                return dialect.getLowercaseFunction() + '(' + column + ')' + " like ?" + escape;
            }
        } else {
            return column + " like ?" + escape;
        }
    }

    @Override
    public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) {
        final String matchValue = ignoreCase ? value.toString().toLowerCase(Locale.ROOT) : value.toString();

        return new TypedValue[] { criteriaQuery.getTypedValue(criteria, propertyName, matchValue) };
    }

    /**
     * Make case insensitive.  No effect for non-String values
     *
     * @return {@code this}, for method chaining
     */
    public LikeExpressionX ignoreCase() {
        ignoreCase = true;
        return this;
    }

}
