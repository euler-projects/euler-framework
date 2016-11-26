package net.eulerframework.common.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class Generic {
    
    public static Class<?> findSuperClassGenricType(Class<?> clazz, int index){
        if(clazz == null)
            return null;
        
        Type type = clazz.getGenericSuperclass();
        
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;            
            Type[] argTypes = pType.getActualTypeArguments();
            
            if(index >= argTypes.length || index < 0){
                throw new IndexOutOfBoundsException("Genric args types find error，Index out of bounds: total="+argTypes.length+", index="+index);
            }
            
            Type argType = argTypes[index];
            
            if (argType instanceof Class) {
                return ((Class<?>) argType);
            }
            
            return null;
        }
    
        return findSuperClassGenricType(clazz.getSuperclass(), index);        
    }
}
