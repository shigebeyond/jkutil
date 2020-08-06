package net.jkcode.jkutil.fiber

import co.paralleluniverse.fibers.DefaultFiberScheduler
import co.paralleluniverse.fibers.FiberForkJoinScheduler
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool

/**
 * 公共的协程池
 *   执行任务时要处理好异常
 */
public val CommonFiberPool: ExecutorService by lazy {
    FiberExecutorService()
}

/**
 * 默认的协程的线程池
 */
val defaultFiberThreadPool: ForkJoinPool by lazy {
    // val scheduler = Fiber.defaultScheduler() // 私有方法
    val scheduler = DefaultFiberScheduler.getInstance() as FiberForkJoinScheduler
    scheduler.executor as ForkJoinPool
}