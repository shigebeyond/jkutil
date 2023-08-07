package net.jkcode.jkutil.common

import net.jkcode.jkutil.singleton.BeanSingletons
import java.util.*

/**
 * 配置数据，用于加载配置文件，并读取配置数据
 * Config data, can load properties file from CLASSPATH or File object.
 *
 * @author shijianhang
 * @date 2016-10-8 下午8:02:47
 */
abstract class IConfig {

    /**
     * 配置文件
     */
    public abstract val file: String

    /**
     * 配置项
     */
    public abstract val props: Map<String, *>;

    /**
     * 判断是否含有配置项
     * @param key
     * @return
     */
    public fun containsKey(key: String): Boolean {
        return props.containsKey(key)
    }

    /**
     * 获得配置项的值
     *    注：调用时需明确指定返回类型，来自动转换参数值为指定类型
     * @param key
     * @param defaultValue
     * @return
     */
    public operator inline fun <reified T:Any> get(key: String, defaultValue: T? = null): T?{
        return props.getAndConvert(key, defaultValue)
    }

    /**
     * 获得string类型的配置项
     * @param key
     * @param defaultValue
     * @return
     */
    public fun getString(key: String, defaultValue: String? = null): String? {
        val value = props.get(key)
        return if(value == null)
            defaultValue
        else
            value.toString()
    }

    /**
     * 获得int类型的配置项
     * @param key
     * @param defaultValue
     * @return
     */
    public fun getInt(key: String, defaultValue: Int? = null): Int? {
        return props.getAndConvert(key, defaultValue)
    }

    /**
     * 获得long类型的配置项
     * @param key
     * @param defaultValue
     * @return
     */
    public fun getLong(key: String, defaultValue: Long? = null): Long? {
        return props.getAndConvert(key, defaultValue)
    }


    /**
     * 获得float类型的配置项
     * @param key
     * @param defaultValue
     * @return
     */
    public fun getFloat(key: String, defaultValue: Float? = null): Float? {
        return props.getAndConvert(key, defaultValue)
    }

    /**
     * 获得double类型的配置项
     * @param key
     * @param defaultValue
     * @return
     */
    public fun getDouble(key: String, defaultValue: Double? = null): Double? {
        return props.getAndConvert(key, defaultValue)
    }

    /**
     * 获得bool类型的配置项
     * @param key
     * @param defaultValue
     * @return
     */
    public fun getBoolean(key: String, defaultValue: Boolean? = null): Boolean? {
        return props.getAndConvert(key, defaultValue)
    }

    /**
     * 获得short类型的配置项
     * @param key
     * @param defaultValue
     * @return
     */
    public fun getShort(key: String, defaultValue: Short? = null): Short?{
        return props.getAndConvert(key, defaultValue)
    }

    /**
     * 获得Date类型的配置项
     * @param key
     * @param defaultValue
     * @return
     */
    public fun getDate(key: String, defaultValue: Date? = null): Date?{
        return props.getAndConvert(key, defaultValue)
    }

    /**
     * 获得Map类型的配置项
     * @param key
     * @param defaultValue
     * @return
     */
    public fun getMap(key: String, defaultValue: Map<String, *>? = null): Map<String, *>?{
        return props.getAndConvert(key, defaultValue)
    }

    /**
     * 获得List类型的配置项
     * @param key
     * @param defaultValue
     * @return
     */
    public fun getList(key: String, defaultValue: List<*>? = null): List<*>?{
        return props.getAndConvert(key, defaultValue)
    }

    /**
     * 获得Config类型的子配置项
     * @param key
     * @param defaultValue
     * @return
     */
    public fun getPathConfig(path: String): Config{
        try{
            val subprops = PropertyUtil.getPath(props, path) as Map<String, *>
            return Config(subprops)
        }catch (e:ClassCastException){
            throw NoSuchElementException("构建配置子项失败：配置数据为$props, 但路径[$path]的子项不是Map")
        }
    }

    /**
     * 获得Properties类型的子配置项
     * @param key
     * @param defaultValue
     * @return
     */
    public fun getPathProperties(path: String): Properties{
        try{
            val subprops = PropertyUtil.getPath(props, path) as Map<String, *>
            val result = Properties()
            result.putAll(subprops)
            return result
        }catch (e:ClassCastException){
            throw NoSuchElementException("构建配置子项失败：配置数据为$props, 但路径[$path]的子项不是Map")
        }
    }

    /**
     * 尝试设置配置项，仅内部使用
     * @param key
     * @param value
     */
    public operator fun set(key: String, value: Any?){
        (props as MutableMap<String, Any?>).set(key, value)
    }

    /**
     * 配置项是类的列表, 对应返回实例列表
     * @param prop
     * @return
     */
    public fun <T> classes2Instances(prop: String): List<T>{
        val classes: List<String>? = this[prop]
        if(classes.isNullOrEmpty())
            return LinkedList() // 空也返回可写的list, 外面可能要用到, 特别是对配置的插件/拦截器列表而言

        return classes!!.map { clazz ->
            BeanSingletons.instance(clazz) as T
        }
    }
    
}
