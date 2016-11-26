package net.eulerframework.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class BeanTool {
    public static void clearEmptyProperty(Object obj) {
        if (obj == null)
            return;
        try {
            for (Class<? extends Object> clazz = obj.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields)
                    if ((field.getType() == String.class) && (!(Modifier.isStatic(field.getModifiers())))) {
                        boolean accessible = field.isAccessible();
                        field.setAccessible(true);
                        String value = (String) field.get(obj);
                        if ((value != null) && ("".equals(value.trim()))) {
                            field.set(obj, null);
                        }
                        field.setAccessible(accessible);
                    }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Field[] getBeanFields(Class<?> clazz, boolean includeParentClass) {
        Field[] fields = clazz.getDeclaredFields();
        if (includeParentClass) {
            Class<?> parentClazz = clazz.getSuperclass();
            if (parentClazz != Object.class) {
                Field[] parentClazzFields = getBeanFields(parentClazz, true);
                Field[] result = ArrayTool.concatAll(fields, parentClazzFields);
                return result;
            }
        }
        return fields;
    }
}
