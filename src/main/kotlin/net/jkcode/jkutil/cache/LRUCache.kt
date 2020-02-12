package net.jkcode.jkutil.cache

import java.util.*

/**
 * 基于lru策略的本地缓存
 * 
 * @author shijianhang
 * @create 2018-02-27 下午8:49
 **/
class LRUCache(protected val maxSize:Int = 10000 /*最大个数*/) : BaseCache() {

    /**
     * 存储数据的map
     */
    protected val storage = object : LinkedHashMap<Any, Any?>(maxSize + 1, .75f, true) {
        // This method is called just after a new entry has been added
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Any, Any?>?): Boolean {
            return size > maxSize
        }
    }

    /**
     * 根据键获得值
     *
     * @param key 键
     * @return
     */
    @Synchronized
    public override fun doGet(key: Any): Any? {
        return storage.get(key)
    }

    /**
     * 设置键值
     *
     * @param key 键
     * @param value 值
     * @param expireSencond 过期秒数
     */
    @Synchronized
    public override fun doPut(key: Any, value: Any, expireSencond: Long) {
        storage.put(key, value)
    }

    /**
     * 删除指定的键的值
     * @param key 要删除的键
     */
    @Synchronized
    public override fun remove(key: Any) {
        storage.remove(key)
    }

    /**
     * 清空缓存
     */
    @Synchronized
    public override fun clear() {
        storage.clear()
    }
}