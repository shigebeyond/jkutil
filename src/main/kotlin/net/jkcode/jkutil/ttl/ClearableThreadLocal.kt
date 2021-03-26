package net.jkcode.jkutil.ttl

/**
 * 当前线程数据持有者
 */
data class LocalHolder<T>(
        public val version: Long,
        public val data: T
)

/**
 * 可清空的 ThreadLocal
 *  需要主动清空 ThreadLocal 所有线程的数据
 *  如 jkerp中的 PluginManager.uninstall()方法, 需要清空ThreadLocal, 但根据ThreadLocal的设计与api只能清空当前线程的数据, 其他线程的数据无法访问与清理
 *  => 添加版本号(时间戳), 如果当前线程版本号与最新版本号不匹配, 则刷新当前线程的数据
 */
class ClearableThreadLocal<T>(protected val supplier:()->T) {

    /**
     * 全局版本
     */
    @Transient
    protected var globalVersion: Long = System.currentTimeMillis()

    /**
     * 线程数据
     */
    protected val threadLocal: ThreadLocal<LocalHolder<T>> = ThreadLocal()

    /**
     * 清空
     *   其实是刷新一下版本, 然后延迟清空
     */
    public fun clear(){
        // 刷新全局版本
        globalVersion = System.currentTimeMillis()
    }

    public fun get(): T {
        // 取得当前线程数据
        val local = threadLocal.get()
        // 如果当期线程的版本 = 全局版本, 则使用当前线程的数据
        if(local != null && local.version == globalVersion)
            return local.data

        // 否则, 创建新数据
        val data = supplier()
        threadLocal.set(LocalHolder(globalVersion, data))
        return data
    }

}