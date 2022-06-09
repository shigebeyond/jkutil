package net.jkcode.jkutil.common

import net.jkcode.jkutil.scope.ClosingOnShutdown
import net.jkcode.jkutil.singleton.BeanSingletons
import java.net.URL
import java.util.*
import kotlin.collections.HashSet

/**
 * 插件加载器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-06-09 4:57 PM
 */
object PluginLoader: ClosingOnShutdown() {

    /**
     * 插件配置文件
     *   多行，每行是一个插件类
     */
    private val file = "plugin.list"

    /**
     * 已加载的插件
     */
    @Volatile
    private var plugins: List<IPlugin>? = null

    /**
     * 加载插件
     *   只加载一次
     */
    public fun loadPlugins(): List<IPlugin> {
        if(plugins == null){
            synchronized(this){
                if(plugins == null)
                    plugins = doLoadPlugins()
            }
        }
        return plugins!!
    }

    /**
     * 真正的加载插件
     */
    private fun doLoadPlugins(): List<IPlugin> {
        // 加载所有的 plugin.list 文件
        val urls: Enumeration<*> = Thread.currentThread().contextClassLoader.getResources(file)
        // 合并去重
        val classes = HashSet<String>()
        for (url in urls) {
            // 每个文件中每行是一个插件类
            val lines = (url as URL).openStream().reader().readLines()
            for(it in lines){
                var line = it
                if(line.startsWith('#')) // 过滤注释行
                    continue
                if(line.contains('#')) // 去掉注释部分
                    line = line.substringBefore('#')
                // 合并
                classes.add(line)
            }
        }
        return classes!!.map { clazz ->
            // 实例化插件
            val p = BeanSingletons.instance(clazz) as IPlugin
            // 初始化插件
            p.start()
            p
        }
    }

    /**
     * 关闭插件
     */
    override fun close() {
        plugins?.forEach {
            it.close()
        }
    }
}