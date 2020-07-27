package net.jkcode.jkutil.singleton

import net.jkcode.jkutil.common.getAccessibleField
import net.jkcode.jkutil.common.getConstructorOrNull
import net.jkcode.jkutil.common.getOrPutOnce
import java.util.concurrent.ConcurrentHashMap

/**
 * 全局的bean的单例池
 *   根据类来获得或创建单例
 *
 * @author shijianhang
 * @create 2019-1-24 下午3:17
 **/
object BeanSingletons: IBeanSingletons {

    /**
     * bean单例池: <类 to 单例>
     */
    private val beans: ConcurrentHashMap<Class<*>, Any> = ConcurrentHashMap();

    /**
     * 根据类来获得单例
     *
     * @param clazz 类
     * @return
     */
    public override fun instance(clazz: Class<*>): Any{
        return beans.getOrPutOnce(clazz){
            buildInstance(clazz)
        }
    }

    /**
     * 构建类实例
     * @param clazz
     * @return
     */
    private fun buildInstance(clazz: Class<*>): Any {
        // 1 object:　有非空静态属性 INSTANCE
        val field = clazz.getAccessibleField("INSTANCE")
        val inst = field?.get(null)
        if(inst != null)
            return inst

        // 2 普通类
        // 检查bean类的默认构造函数
        if (clazz.getConstructorOrNull() == null)
            throw NoSuchMethodException("Bean Class [$clazz] has no no-arg constructor") // 无默认构造函数
        // 创建bean实例
        return clazz.newInstance()
    }
}