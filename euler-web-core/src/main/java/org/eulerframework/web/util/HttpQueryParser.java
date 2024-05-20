package org.eulerframework.web.util;

import org.apache.commons.lang3.StringUtils;
import org.eulerframework.common.util.ArrayUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HttpQueryParser {
    private final Map<String, String[]> queryMap = new HashMap<>();

    public HttpQueryParser(String query) {
        if (StringUtils.isEmpty(query)) {
            return;
        }

        String[] queryItems = query.split("&");
        for (String queryItem : queryItems) {
            String[] tmp = queryItem.split("=");
            String key = tmp[0];
            String value = tmp.length > 1 ? tmp[1] : "";
            String[] existsValues = queryMap.get(key);
            if (existsValues == null) {
                queryMap.put(key, new String[]{value});
            } else {
                queryMap.put(key, ArrayUtils.concat(existsValues, new String[]{value}));
            }
        }
    }

    public String[] getParameterValues(String parameter) {
        return this.queryMap.get(parameter);
    }

    public String getParameterValue(String parameter) {
        String[] values = this.getParameterValues(parameter);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    public Long getLongParameterValue(String parameter) {
        String value = this.getParameterValue(parameter);
        return value == null ? null : Long.parseLong(value);
    }

    public Integer getIntegerParameterValue(String parameter) {
        String value = this.getParameterValue(parameter);
        return value == null ? null : Integer.parseInt(value);
    }

    public Date getDateParameterValue(String parameter) {
        Long longParameterValue = this.getLongParameterValue(parameter);
        return longParameterValue == null ? null : new Date(longParameterValue);
    }

    public <T extends Enum<T>> T getEnumParameterValue(String parameter, Class<T> enumType) {
        String value = this.getParameterValue(parameter);
        return value == null ? null : Enum.valueOf(enumType, value);
    }
}
