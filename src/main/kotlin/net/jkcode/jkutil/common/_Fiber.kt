package net.jkcode.jkutil.common

import co.paralleluniverse.fibers.DefaultFiberScheduler
import co.paralleluniverse.fibers.FiberForkJoinScheduler
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.fiber
import co.paralleluniverse.strands.dataflow.Val
import java.util.concurrent.*

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
private val defaultFiberThreadPool: ForkJoinPool by lazy {
    // val scheduler = Fiber.defaultScheduler() // 私有方法
    val scheduler = DefaultFiberScheduler.getInstance() as FiberForkJoinScheduler
    scheduler.executor as ForkJoinPool
}

/**
 * 基于 ExecutorService 实现的 ExecutorService
 *    执行在协程, 不在线程
 */
class FiberExecutorService : ExecutorService by defaultFiberThreadPool {

    override fun execute(command: Runnable) {
        fiber @Suspendable {
            command.run()
        }
    }

    override fun <T> submit(task: Callable<T>): Future<T> {
        //AsyncCompletionStage(); // 私有构造函数
        return fiber @Suspendable {
            task.call()
        }
    }

    override fun <T> submit(task: Runnable, result: T): Future<T> {
        return fiber @Suspendable {
            task.run()
            result
        }
    }

    override fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>> {
        return tasks.map {
            submit(it)
        }
    }

    override fun <T> invokeAll(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): List<Future<T>> {
        return tasks.map {
            submit(it)
        }
    }

    override fun <T> invokeAny(tasks: Collection<Callable<T>>): T {
        return submit(tasks.first()).get()
    }

    override fun <T> invokeAny(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): T{
        return submit(tasks.first()).get()
    }
}
