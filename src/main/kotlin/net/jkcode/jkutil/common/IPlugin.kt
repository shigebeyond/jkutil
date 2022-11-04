package net.jkcode.jkutil.common

import java.io.Closeable

/**
 * 插件, 主要2个方法
 *    1 start() 初始化
 *    2 close() 关闭
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-03 4:57 PM
 */
interface IPlugin: Closeable {

    /**
     * 初始化
     */
    fun start(){
        commonLogger.debug(ColorFormatter.applyTextColor("Load plugin: {}", 34), this.javaClass)
        try {
            doStart()
        }catch (ex: Exception){
            commonLogger.errorAndPrint("Fail to load plugin [${this.javaClass}]", ex)
            throw ex
        }
    }

    /**
     * 初始化
     */
    fun doStart()

}