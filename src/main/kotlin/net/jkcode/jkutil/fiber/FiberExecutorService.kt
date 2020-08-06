package net.jkcode.jkutil.fiber

import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.FiberScheduler
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.fiber
import net.jkcode.jkutil.common.getAccessibleMethod
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * 基于 ExecutorService 实现的 ExecutorService
 *    执行在协程, 不在线程
 */
class FiberExecutorService : ExecutorService by defaultFiberThreadPool {

    companion object{
        /**
         * FiberScheduler.schedule()私有方法的引用
         */
        private val scheduleMethod = FiberScheduler::class.java.getAccessibleMethod("schedule", Fiber::class.java, Any::class.java, Long::class.java, TimeUnit::class.java)
    }

    /**
     * 执行单个任务
     */
    override fun execute(command: Runnable) {
        fiber @Suspendable {
            command.run()
        }
    }

    /**
     * 执行单个任务
     */
    override fun <T> submit(task: Callable<T>): Future<T> {
        return fiber @Suspendable {
            task.call()
        }
    }

    /**
     * 执行单个任务
     */
    override fun <T> submit(task: Runnable, result: T): Future<T> {
        return fiber @Suspendable {
            task.run()
            result
        }
    }

    /**
     * 执行所有任务
     */
    override fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>> {
        return tasks.map {
            submit(it)
        }
    }

    /**
     * 执行所有任务
     */
    override fun <T> invokeAll(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): List<Future<T>> {
        return tasks.map {
            //submit(it)
            val f = fiber @Suspendable {
                it.call()
            }

            // 设置协程超时
            //f.scheduler.schedule(f, it, timeout, unit)
            scheduleMethod.invoke(f.scheduler, f, it, timeout, unit)
            f
        }
    }

    /**
     * 执行任一任务
     *   直接第一个
     */
    override fun <T> invokeAny(tasks: Collection<Callable<T>>): T {
        return submit(tasks.first()).get()
    }

    /**
     * 执行任一任务
     *   直接第一个
     */
    override fun <T> invokeAny(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): T{
        //return submit(tasks.first()).get()
        val task = tasks.first()
        val f = fiber @Suspendable {
            task.call()
        }

        // 设置协程超时
        //f.scheduler.schedule(f, task, timeout, unit)
        scheduleMethod.invoke(f.scheduler, f, task, timeout, unit)

        return f.get()
    }
}
