package net.jkcode.jkutil.common

import io.netty.util.concurrent.DefaultThreadFactory
import io.netty.util.concurrent.EventExecutor
import io.netty.util.concurrent.MultithreadEventExecutorGroup
import io.netty.util.concurrent.SingleThreadEventExecutor
import net.jkcode.jkutil.fiber.FiberExecutorService
import net.jkcode.jkutil.scope.ClosingOnShutdown
import net.jkcode.jkutil.ttl.SttlThreadPool
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

/**
 * 公共的线程池配置
 */
private val config = Config.instance("common-pool", "yaml")

/**
 * 公共的线程/协程池
 */
public val CommonExecutor: ExecutorService by lazy{
    if(JkApp.useFiber)
        CommonFiberPool
    else
        CommonThreadPool
}

/**
 * 公共的协程池
 *   执行任务时要处理好异常
 */
public val CommonFiberPool: ExecutorService by lazy {
    FiberExecutorService()
}

/**
 * 公共的线程池
 *   执行任务时要处理好异常
 */
public val CommonThreadPool: ExecutorService by lazy{
    // 1 创建线程池
    // 初始线程数
    var corePoolSize: Int = config["corePoolSize"]!!
    if(corePoolSize == 0)
        corePoolSize = Runtime.getRuntime().availableProcessors()
    // 最大线程数
    var maximumPoolSize: Int = config["maximumPoolSize"]!!
    if(maximumPoolSize == 0)
        maximumPoolSize = corePoolSize * 8
    // 队列大小
    var queueSize: Int =config["queueSize"]!!
    if(queueSize == 0)
        queueSize = Integer.MAX_VALUE
    // 创建线程池
    val pool = StandardThreadExecutor(corePoolSize, maximumPoolSize, queueSize, DefaultThreadFactory("CommonPool", true))
    // 预创建线程
    pool.prestartAllCoreThreads()

    // 2 关机后关闭线程池
    ClosingOnShutdown.addClosing(object: Closeable{
        override fun close() {
            println("-- 关闭线程池, 并等待任务完成 --")
            /**
             * 停止工作线程: 不接收新任务
             * shutdown()只是将线程池的状态设置为SHUTWDOWN状态，正在执行的任务会继续执行下去，没有被执行的则中断。
             * shutdownNow()则是将线程池的状态设置为STOP，正在执行的任务则被停止，没被执行任务的则返回。
             */
            pool.shutdown()

            // 等待任务完成
            pool.awaitTermination(1, TimeUnit.DAYS) // 等长一点 = 死等
        }
    })

    // 3 包装sttl
    if(JkApp.useSttl)
        SttlThreadPool(pool)
    else
        pool
}

/**
 * 异步执行, 并返回异步结果
 * @param runnable
 * @return
 */
public fun ExecutorService.runAsync(runnable: Runnable): CompletableFuture<Void> {
    return CompletableFuture.runAsync(runnable, this)
}

/**
 * 异步执行, 并返回异步结果
 * @param runnable
 * @return
 */
public fun <T> ExecutorService.supplyAsync(supplier: Supplier<T>): CompletableFuture<T> {
    return CompletableFuture.supplyAsync(supplier, this)
}

/**
 * 单个线程的启动+等待
 * @param join 是否等待线程结束
 * @return
 */
public fun Thread.start(join: Boolean = true): Thread {
    start()
    if(join)
        join()
    return this
}

/**
 * 多个个线程的启动+等待
 * @param join 是否等待线程结束
 * @return
 */
public fun List<Thread>.start(join: Boolean = true): List<Thread> {
    for(t in this)
        t.start()
    if(join)
        for(t in this)
            t.join()
    return this
}

/**
 * 创建线程
 * @param num 线程数
 * @param join 是否等待线程结束
 * @param runnable 线程体
 * @return
 */
public fun makeThreads(num: Int, join: Boolean = true, runnable: (Int) -> Unit): List<Thread> {
    return (0 until num).map { i ->
        Thread({
            runnable.invoke(i)
        }, "test-thread_$i")
    }.start(join)
}

/**
 * 创建线程
 * @param num 线程数
 * @param join 是否等待线程结束
 * @param runnable 线程体
 * @return
 */
public fun makeThreads(num: Int, runnable: (Int) -> Unit): List<Thread>{
    return makeThreads(num, true, runnable)
}

/****************************** 每个线程有独立任务队列 的线程池 *****************************/
/**
 * MultithreadEventExecutorGroup.children 属性
 */
private val childrenProp: KProperty1<MultithreadEventExecutorGroup, Array<EventExecutor>> by lazy{
    val prop = MultithreadEventExecutorGroup::class.getInheritProperty("children") as KProperty1<MultithreadEventExecutorGroup, Array<EventExecutor>>
    prop.javaField!!.isAccessible = true
    prop
}

// 获得子执行器个数: executorGroup.executorCount()
// 使用某个子子执行器来执行任务: executorGroup.getExecutor(i).execute(runnable)
/**
 * 获得某个子执行器(单线程)
 * @param index 子执行器下标
 * @return
 */
public fun MultithreadEventExecutorGroup.getExecutor(index: Int): SingleThreadEventExecutor {
    val children: Array<EventExecutor> = childrenProp.get(this)
    return children.get(index) as SingleThreadEventExecutor
}

/**
 * 根据 arg 来选择一个固定的线程
 * @param arg
 * @return
 */
public fun MultithreadEventExecutorGroup.selectExecutor(arg: Any): SingleThreadEventExecutor {
    return selectExecutor(arg.hashCode())
}

/**
 * 根据 arg 来选择一个固定的线程
 * @param arg
 * @return
 */
public fun MultithreadEventExecutorGroup.selectExecutor(arg: Int): SingleThreadEventExecutor {
    return getExecutor(Math.abs(arg) % executorCount())
}

/**
 * 执行命令
 * @param cmd 命令
 * @return 执行结果
 */
public fun execCommand(cmd: String): String {
    val pro: Process = doExecCommand(cmd)
    return pro.output()
}

/**
 * 执行命令
 * @param cmd 命令
 * @param outputLineHandler 输出行处理
 */
public fun execCommand(cmd: String, outputLineHandler:(String)->Unit) {
    val pro = doExecCommand(cmd)
    pro.inputStream.bufferedReader().forEachLine(outputLineHandler)
}

/**
 * 执行命令
 */
private inline fun doExecCommand(cmd: String): Process {
    val pro: Process = Runtime.getRuntime().exec(cmd)
    val status = pro.waitFor()
    if (status != 0)
        throw IOException("Failed to call command: $cmd")
    return pro
}

/**
 * 输出命令行进程的执行结果
 * @return
 */
public fun Process.output(): String {
    return inputStream.bufferedReader().readText()
}