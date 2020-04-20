package net.jkcode.jkutil.cache

import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jkutil.singleton.NamedConfiguredSingletons
import java.util.concurrent.CompletableFuture

/**
 * 缓存操作接口
 *
 * @author shijianhang
 * @create 2018-02-27 下午8:20
 **/
interface ICache {

    // 可配置的单例
    companion object: NamedConfiguredSingletons<ICache>() {
        /**
         * 单例类的配置，内容是哈希 <单例名 to 单例类>
         */
        public override val instsConfig: IConfig = Config.instance("cache", "yaml")
    }

    /**
     * 根据键获得值
     *
     * @param key 键
     * @return
     */
    operator fun get(key: Any): Any?

    /**
     * 根据键获得值, 如果无则构建
     *   如果源数据是null, 则缓存空对象, 防止缓存穿透
     *
     * @param key 键
     * @param expireSeconds 过期秒数
     * @param waitMillis 等待的毫秒数
     * @param dataLoader 回源函数, 兼容函数返回类型是CompletableFuture, 同一个key的并发下只调用一次
     * @return
     */
    fun getOrPut(key: Any, expireSeconds:Long, waitMillis:Long = 200, dataLoader: () -> Any?): CompletableFuture<Any?>

    /**
     * 设置键值
     *   如果源数据是null, 则缓存空对象, 防止缓存穿透
     *
     * @param key 键
     * @param value 值
     * @param expireSencond 过期秒数
     */
    fun put(key: Any, value: Any?, expireSencond:Long = 6000)

    /**
     * 设置键值
     *   如果源数据是null, 则缓存空对象, 防止缓存穿透
     *
     * @param key 键
     * @param value 值
     */
    operator fun set(key: Any, value: Any?){
        put(key, value)
    }

    /**
     * 删除指定的键的值
     * @param key 要删除的键
     */
    fun remove(key: Any)

    /**
     * 删除指定正则的值
     * @param pattern 要删除的键的正则
     */
    fun removeByPattern(pattern: String){
        throw UnsupportedOperationException()
    }

    /**
     * 清空缓存
     */
    fun clear()

}
