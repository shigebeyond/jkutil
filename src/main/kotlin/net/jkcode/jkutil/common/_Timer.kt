package net.jkcode.jkutil.common

import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.Timer
import io.netty.util.TimerTask
import net.jkcode.jkutil.scope.ClosingOnShutdown
import net.jkcode.jkutil.ttl.SttlTimer
import java.io.Closeable
import java.util.concurrent.TimeUnit

/**
 * 公共的毫秒级定时器
 *   HashedWheelTimer 是单线程的, 因此每个定时任务执行耗时不能太长, 如果有耗时任务, 则扔到其他线程池中处理
 */
public val CommonMilliTimer: Timer by lazy{
    createTimer(1, TimeUnit.MILLISECONDS, 256 /* 2的次幂 */)
}

/**
 * 公共的秒级定时器
 *   HashedWheelTimer 是单线程的, 因此每个定时任务执行耗时不能太长, 如果有耗时任务, 则扔到其他线程池中处理
 */
public val CommonSecondTimer: Timer by lazy{
    createTimer(200, TimeUnit.MILLISECONDS, 64 /* 2的次幂 */)
}

/**
 * 创建定时器
 */
private fun createTimer(tickDuration: Long, unit: TimeUnit, ticksPerWheel: Int): Timer{
    // 创建timer
    val timer = HashedWheelTimer(tickDuration, unit, ticksPerWheel)
    // 关机时关闭timer
    ClosingOnShutdown.addClosing(object: Closeable{
        override fun close() {
            timer.stop()
        }
    })
    return if(JkApp.useSttl) // 包装sttl
                SttlTimer(timer)
            else
                timer
}

/**
 * 添加周期性任务
 * @param task 任务
 * @param period 周期时间
 * @param unit 时间单位
 * @return
 */
public fun Timer.newPeriodic(task: () -> Unit, period: Long, unit: TimeUnit): Timeout{
    // 定时触发
    return newTimeout(object : TimerTask {
        override fun run(timeout: Timeout) {
            // 执行任务
            task.invoke()

            // 递归
            newTimeout(this, period, unit)
        }
    }, period, unit)
}

/**
 * 添加周期性任务
 * @param predicateTask 任务, 带bool返回值, 如果为false则中断周期执行
 * @param period 周期时间
 * @param unit 时间单位
 * @return
 */
public fun Timer.newPeriodicWhile(predicateTask: () -> Boolean, period: Long, unit: TimeUnit): Timeout{
    // 定时触发
    return newTimeout(object : TimerTask {
        override fun run(timeout: Timeout) {
            // 执行任务
            val b = predicateTask.invoke()

            // 递归
            if(b)
                newTimeout(this, period, unit)
        }
    }, period, unit)
}

/**
 * 添加周期性任务
 * @param predicateTask 任务, 带bool返回值, 如果为true则中断周期执行
 * @param period 周期时间
 * @param unit 时间单位
 * @return
 */
public fun Timer.newPeriodicUntil(predicateTask: () -> Boolean, period: Long, unit: TimeUnit): Timeout{
    // 定时触发
    return newTimeout(object : TimerTask {
        override fun run(timeout: Timeout) {
            // 执行任务
            val b = predicateTask.invoke()

            // 递归
            if(!b)
                newTimeout(this, period, unit)
        }
    }, period, unit)
}

/**
 * 收集耗时
 */
public fun collectCostTime(tag: String = "", action: ()->Unit){
    var start = System.currentTimeMillis()
    action()
    val costtime = System.currentTimeMillis() - start
    println("${tag}耗时: $costtime ms")
}