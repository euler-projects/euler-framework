package net.eulerform.web.core.util;

import net.eulerform.web.core.base.exception.IllegalParamException;

public class ParamCheck {

    public static void require(String param) {
        if (param == null)
            throw new IllegalParamException("Param is required.");
    }

    public static void exclusive(String... params) {
        int count = 0;
        for (String param : params) {
            if (param != null) {
                count++;
            }
        }
        if (count != 1) {
            throw new IllegalParamException("Param exclusive.");
        }
    }

    public static void matches(String param, String regex) {
        if (!param.matches(regex))
            throw new IllegalParamException("Param format error.");
    }
}
