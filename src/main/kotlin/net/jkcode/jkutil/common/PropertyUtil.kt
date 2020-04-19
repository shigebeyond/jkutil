package net.jkcode.jkutil.common

import java.lang.reflect.Array
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

/**
 * 属性处理器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-12-23 2:57 PM
 */
object PropertyUtil {

    /**
     * 获得属性值
     *
     * @param obj
     * @param key
     * @return
     */
    @JvmStatic
    public fun get(obj: Any?, key: String): Any? {
        // null
        if (obj == null)
            return null

        // 数组
        if (obj.javaClass.isArray())
            return Array.get(obj, key.toInt())

        // 集合
        if (obj is Collection<*>)
            return obj[key.toInt()]

        // 哈希
        if (obj is Map<*, *>)
            return obj[key]

        // 对象
        val prop = obj::class.getInheritProperty(key) as KProperty1<Any, Any?>?
        if(prop == null)
            throw NoSuchElementException("获得对象属性失败: 对象为[$obj], 属性名为[$prop]")

        return prop.get(obj)
    }

    /**
     * 获得'.'分割的路径下的属性值
     *
     * @param obj
     * @param path '.'分割的路径
     * @return
     */
    @JvmStatic
    public fun getPath(obj: Any?, path:String): Any? {
        // 单层
        if(!path.contains('.'))
            return get(obj, path)

        // 多层
        val keys:List<String> = path.split('.')
        var data:Any? = obj
        for (key in keys)
            data = get(data, key)
        return data
    }

    /**
     * 获得属性类型
     *
     * @param obj
     * @param key
     * @return
     */
    @JvmStatic
    public fun getType(obj: Any?, key: String): Class<*>? {
        if (obj == null)
            return null

        // obj是数组/集合
        if (obj.javaClass.isArray() || obj is Collection<*>)
            return Int::class.java

        // obj是哈希
        if (obj is Map<*, *>) {
            val value = obj[key]
            // 值的类型
            if(value != null)
                return value.javaClass

            // key/value类型擦除, 只能返回Object
            Any::class.java
        }

        // obj是对象
        val prop = obj::class.getInheritProperty(key) as KProperty1<Any, Any?>
        if(prop == null)
            throw NoSuchElementException("获得对象属性失败: 对象为[$obj], 属性名为[$prop]")

        return (prop.getter.returnType.classifier as KClass<*>).java
    }

    /**
     * 获得'.'分割的路径下的属性类型
     *
     * @param obj
     * @param path '.'分割的路径
     * @return
     */
    @JvmStatic
    public fun getPathType(obj: Any?, path:String): Class<*>? {
        // 单层
        if(!path.contains('.'))
            return getType(obj, path)

        // 多层
        val keys = path.split('.') as MutableList<String>
        val last = keys.removeAt(keys.size - 1) // 最后一个
        var data:Any? = obj
        for (key in keys)
            data = get(data, key)
        return getType(data, last)
    }

    /**
     * 设置属性值
     *
     * @param obj
     * @param key
     * @return
     */
    @JvmStatic
    public fun set(obj: Any?, key: String, value: Any?) {
        // null
        if (obj == null)
            throw NoSuchElementException("设置对象属性失败: 对象为null, 属性名为[$key]")

        // 数组
        if (obj.javaClass.isArray()) {
            Array.set(obj, key.toInt(), value)
            return
        }

        // 集合
        if (obj is MutableList<*>) {
            (obj as MutableList<Any?>)[key.toInt()] = value
            return
        }

        // 哈希
        if (obj is MutableMap<*, *>) {
            (obj as MutableMap<String, Any?>)[key] = value
            return
        }

        // 对象
        val prop = obj::class.getInheritProperty(key) as KMutableProperty1<Any, Any?>?
        if(prop == null)
            throw NoSuchElementException("设置对象属性失败: 对象为[$obj], 属性名为[$key]")

        prop.set(obj, value)
    }


    /**
     * 设置'.'分割的路径下的子项值
     *
     * @param path '.'分割的路径
     * @param value 目标值
     */
    public fun setPath(obj: Any?, path:String, value:Any?) {
        // 单层
        if(!path.contains('.')){
            set(obj, path, value)
            return
        }

        // 多层
        val keys:List<String> = path.split('.')
        var data: Any? = obj
        for (i in 0 until (keys.size - 1)){
            val key = keys[i]
            // 一层层往下走
            data = get(data, key)
            if(data == null)
                throw NoSuchElementException("设置对象属性失败: 对象为[$obj], 属性名为[$key]")
        }
        set(data, keys.last(), value)
    }

}