package net.jkcode.jkutil.cache

import io.netty.util.Timeout
import io.netty.util.TimerTask
import net.jkcode.jkutil.common.CommonMilliTimer
import net.jkcode.jkutil.common.trySupplierFuture
import net.jkcode.jkutil.lock.IKeyLock
import java.io.Serializable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * 空的缓存对象, 不能用Unit, 因为: Class kotlin.Unit does not implement Serializable or externalizable
 */
object EmptyCacheItem: Serializable

/**
 * 基础缓存类
 * @author shijianhang<772910474@qq.com>
 * @date 2019-03-06 10:42 AM
 */
abstract class BaseCache: ICache {

    /**
     * 对key的锁, 防止并发回源
     */
    protected val lock: IKeyLock = IKeyLock.instance("local")

    /**
     * 根据键获得值
     *
     * @param key 键
     * @return
     */
    public override operator fun get(key: Any): Any?{
        val v = doGet(key)
        return if(v == EmptyCacheItem) null else v
    }

    /**
     * 根据键获得值
     *
     * @param key 键
     * @return
     */
    public abstract fun doGet(key: Any): Any?

    /**
     * 设置键值
     *   如果源数据是null, 则缓存空对象, 防止缓存穿透
     *
     * @param key 键
     * @param value 值
     * @param expireSencond 过期秒数
     */
    public override fun put(key: Any, value: Any?, expireSencond:Long){
        doPut(key, value ?: EmptyCacheItem, expireSencond)
    }

    /**
     * 设置键值
     *
     * @param key 键
     * @param value 值
     * @param expireSencond 过期秒数
     */
    public abstract fun doPut(key: Any, value: Any, expireSencond:Long)

    /**
     * 根据键获得值
     *   如果源数据是null, 则缓存空对象, 防止缓存穿透
     *
     * @param key 键
     * @param expireSeconds 过期秒数
     * @param waitMillis 等待的毫秒数
     * @param dataLoader 回源函数, 兼容函数返回类型是CompletableFuture, 同一个key的并发下只调用一次
     * @return
     */
    public override fun getOrPut(key: Any, expireSeconds:Long, waitMillis:Long, dataLoader: () -> Any?): CompletableFuture<Any?> {
        // 1 尝试读缓存
        val v = doGet(key)
        if (v != null)
            return CompletableFuture.completedFuture(if(v == EmptyCacheItem) null else v)

        // 2 无缓存, 则回源
        val result = CompletableFuture<Any?>()
        // 2.1 锁住key, 则回源, 防止并发回源
        val locked = lock.quickLockCleanly(key){
            // 回源
            trySupplierFuture(dataLoader).whenComplete { r, ex ->
                if(ex != null) {
                    result.completeExceptionally(ex)
                    throw ex
                }

                try {
                    // 写缓存
                    this.put(key, r, expireSeconds)
                    result.complete(r)
                }catch (e: Exception){
                    result.completeExceptionally(e)
                    throw e
                }

            }
        }
        // 2.2 锁不住key, 则等待指定毫秒数后读缓存
        if(!locked){
            CommonMilliTimer.newTimeout(object : TimerTask {
                override fun run(timeout: Timeout) {
                    // 读缓存
                    val v = get(key)
                    result.complete(v)
                }
            }, waitMillis, TimeUnit.MILLISECONDS)
        }

        return result
    }

}