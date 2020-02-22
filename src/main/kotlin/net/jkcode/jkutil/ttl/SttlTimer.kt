package net.jkcode.jkutil.ttl

import io.netty.util.Timeout
import io.netty.util.Timer
import io.netty.util.TimerTask
import java.util.concurrent.TimeUnit

/**
 * 可传递ScopedTransferableThreadLocal的Timer, 只传递ScopedTransferableThreadLocal, 无关ThreadLocal
 *   特性: worker thread 在执行任务时, 会传递 caller thread的 ScopedTransferableThreadLocal数据
 *   目标: 主要是为了解决异步执行时, 线程状态(ScopedTransferableThreadLocal)的传递问题, 如 jkmvc 将当前 Db/HttpRequest 等对象都是记录到 ScopedTransferableThreadLocal对象中, 以方便访问, 但是一旦异步执行后就丢失了
 *   实现: 改写 execute() 方法, 在执行之前传递一下 ScopedTransferableThreadLocal对象, 在执行后就恢复一下 ScopedTransferableThreadLocal对象
 *   优化: 所有 SttlInterceptor.intercept()方法的 caller thread 的 ScopedTransferableThreadLocal对象引用都是使用 `ScopedTransferableThreadLocal.weakCopyLocal2Value()`, 为 `WeakHashMap`, GC会回收, 但不频繁, 适用于短时间引用
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-18 4:28 PM
 */
class SttlTimer(protected val pool: Timer) : Timer by pool {

    /**
     * 改写 newTimeout() 方法
     *    在执行之前传递一下 ScopedTransferableThreadLocal对象, 在执行后就恢复一下 ScopedTransferableThreadLocal对象
     */
    override fun newTimeout(task: TimerTask, delay: Long, unit: TimeUnit): Timeout {
        val newTask = SttlInterceptor.intercept(ScopedTransferableThreadLocal.getLocal2Value(), task)
        return pool.newTimeout(newTask, delay, unit)
    }

}
