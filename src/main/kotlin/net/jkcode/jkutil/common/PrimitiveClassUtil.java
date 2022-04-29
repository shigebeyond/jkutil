package net.jkcode.jkutil.common;

import java.util.HashMap;

/**
 * 原始类型工具类
 */
public class PrimitiveClassUtil {

    private static HashMap<String, Class<?>> PRIMITIVE_Class = new HashMap();

    static {
        PRIMITIVE_Class.put(Boolean.TYPE.toString(), Boolean.TYPE);
        PRIMITIVE_Class.put(Byte.TYPE.toString(), Byte.TYPE);
        PRIMITIVE_Class.put(Character.TYPE.toString(), Character.TYPE);
        PRIMITIVE_Class.put(Double.TYPE.toString(), Double.TYPE);
        PRIMITIVE_Class.put(Float.TYPE.toString(), Float.TYPE);
        PRIMITIVE_Class.put(Integer.TYPE.toString(), Integer.TYPE);
        PRIMITIVE_Class.put(Long.TYPE.toString(), Long.TYPE);
        PRIMITIVE_Class.put(Short.TYPE.toString(), Short.TYPE);
        PRIMITIVE_Class.put(Void.TYPE.toString(), Void.TYPE);
    }

    /**
     * 获得原始类型
     * @param className
     * @return
     */
    public static Class<?> getPrimitiveClass(String className){
        return PRIMITIVE_Class.get(className);
    }
}
