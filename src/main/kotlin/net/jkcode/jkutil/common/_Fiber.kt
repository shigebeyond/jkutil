package net.jkcode.jkutil.common

import co.paralleluniverse.fibers.DefaultFiberScheduler
import co.paralleluniverse.fibers.FiberForkJoinScheduler
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.fibers.futures.AsyncCompletionStage
import co.paralleluniverse.kotlin.fiber
import co.paralleluniverse.strands.dataflow.Val
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.Future

/**
 * 默认的协程的线程池
 */
private val defaultFiberThreadPool: ForkJoinPool by lazy{
    // val scheduler = Fiber.defaultScheduler() // 私有方法
    val scheduler = DefaultFiberScheduler.getInstance() as FiberForkJoinScheduler
    scheduler.executor as ForkJoinPool
}

class FiberExecutorService: ExecutorService by defaultFiberThreadPool{

    override fun execute(command: Runnable) {
        fiber @Suspendable {
            command.run()
        }
    }

    override fun <T : Any?> submit(task: Callable<T>): Future<T> {
        //AsyncCompletionStage(); // 私有构造函数
        return fiber @Suspendable {
            task.call()
        }
    }

}

/**
 * 公共的协程池
 *   执行任务时要处理好异常
 */
public val CommonFiberPool: ExecutorService by lazy{
    // val scheduler = Fiber.defaultScheduler() // 私有方法
    val scheduler = DefaultFiberScheduler.getInstance() as FiberForkJoinScheduler
    scheduler.executor as ForkJoinPool
}