package net.jkcode.jkutil.common

import java.util.concurrent.atomic.AtomicBoolean

/**
 * 使用AtomicBoolean实现一次性的启动
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-17 6:45 PM
 */
class AtomicStarter {

    /**
     * 是否已启动
     */
    protected val started: AtomicBoolean = AtomicBoolean(false)

    /**
     * 是否已启动过
     */
    public val isStarted: Boolean
        get() = started.get()

    /**
     * 启动一次
     * @param block 处理
     * @return
     */
    public inline fun startOnce(block: () -> Unit): Boolean {
        val started = (!started.get()) // 未开始过
                && started.compareAndSet(false, true) // 第一个开始
        if(started)
            block()
        return started
    }

}