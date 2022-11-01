package net.jkcode.jkutil.tests

import net.jkcode.jkutil.common.VoidFuture
import net.jkcode.jkutil.common.makeThreads
import net.jkcode.jkutil.common.randomBoolean
import net.jkcode.jkutil.common.randomInt
import org.junit.Test
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

class CompletableFutureTests{

    @Test
    fun testVoidFuture() {
        VoidFuture.thenRun{
            println("hello")
        }

        Thread.sleep(1000)
    }

    /**
     * CompletableFuture的 thenAccept() / exceptionally() / whenComplete()
     */
    @Test
    fun testBasic() {
        /*
        1. 成功
        成功了有结果: 10
        异常依旧返回结果: 10
        最后汇总: 结果=10，异常=null

        2. 异常
        失败了有异常: java.util.concurrent.CompletionException: java.lang.Exception: 随机的异常
        异常依旧返回结果: -1
        最后汇总: 结果=null，异常=java.util.concurrent.CompletionException: java.lang.Exception: 随机的异常
         */
        val f = CompletableFuture.supplyAsync {
            if(randomBoolean())
                throw Exception("随机的异常")
            randomInt(100)
        }
        // 处理响应
        f.thenAccept {
            println("成功了有结果: $it")
        }
        val f2: CompletableFuture<Void>? = f.exceptionally {
            println("失败了有异常: $it")
            -1 // 发生异常后, 可以返回新的成功值, 但是只限于新future
        }.thenAccept {
            println("异常依旧返回结果: $it")
        }

        // 对上一行的异常捕获, 并返回新的值, 不会影响旧的future
        val f3: CompletableFuture<Int> = f.whenComplete{ r, ex ->
            println("最后汇总: 结果=" + r + "，异常=" + ex)
        }
        Thread.sleep(10000)
    }

    /**
     * 合并2个future
     */
    @Test
    fun testCombine() {
        val f = CompletableFuture.supplyAsync {
                val r = randomInt(100)
                println("结果1: $r")
                r
            }.thenCombine(
                CompletableFuture.supplyAsync{
                    val r = randomInt(100)
                    println("结果2: $r")
                    r
                }
            , { r1, r2 ->
                // 合并两个查询结果
                val r = r1 + r2
                println("结果3: $r")
                r
            });

        println(f.get())
    }

    /**
     * CompletableFuture.allOf()
     */
    @Test
    fun testAllOf() {
        val start = System.currentTimeMillis()
        // 结果集
        val list = ArrayList<String>()

        //val taskList = listOf(2, 1, 3, 4, 5, 6, 7, 8, 9, 10)
        val taskList = listOf(2)
        // 全流式处理转换成CompletableFuture[]+组装成一个无返回值CompletableFuture，join等待执行完毕。返回结果whenComplete获取
        val cfs = taskList.stream()
                .map<Any> { i ->
                    CompletableFuture.supplyAsync{
                                Thread.sleep(1000L* randomInt(5))
                                println("task线程：" + Thread.currentThread().name
                                        + "任务i=" + i + ",完成！+" + Date())
                                i
                            }
                            .thenApplyAsync{
                                Integer.toString(it.toInt())
                            }
                            .whenComplete{ r, ex ->
                                println("任务" + r + "完成!result=" + r + "，异常 e=" + ex + "," + Date())
                                list.add(r)
                            }
                }
                .toArray(){
                    arrayOfNulls<CompletableFuture<String>>(it)
                }
        // 封装后无返回值，必须自己whenComplete()获取
        CompletableFuture.allOf(*cfs).join()
        println("list=" + list + ",耗时=" + (System.currentTimeMillis() - start))
    }

    /**
     * CompletableFuture 多个 thenAccept()
     */
    @Test
    fun testMultiThen() {
        val f = CompletableFuture.supplyAsync {
            if(randomBoolean())
                throw Exception("随机的异常")
            randomInt(100)
        }

        var i = 0
        f.thenAccept {
            println("第${i++}个监听者: $it")
        }
        f.thenAccept {
            println("第${i++}个监听者: $it")
        }
        f.thenAccept {
            println("第${i++}个监听者: $it")
        }
        f.exceptionally {
            println("监听异常: $it")
            null
        }
    }

    /**
     * CompletableFuture 并发get()
     */
    @Test
    fun testConcurrentGet() {
        val f = CompletableFuture.supplyAsync {
            Thread.sleep(1000)
            randomInt(100)
        }

        var i = AtomicInteger(0)
        makeThreads(3){
            println("第${i.getAndIncrement()}个等待者: ${f.get()}")
        }
    }

    @Test
    fun testException(){
        val f = CompletableFuture<Any?>()
        // val result = f.whenComplete{ r, t -> // 不转换结果, 调用get()方法还是会抛异常
        val result = f.handle { r, t -> // 转换结果, 调用get()方法不抛异常 = whenComplete + 转换
            "result=$r, exception=$t"
        }
        f.completeExceptionally(Exception("test"))
        println(result.get())
    }

    @Test
    fun testCompleted(){
        val future = CompletableFuture.completedFuture(1)
        future.whenComplete{ r, ex->
            println("结果=" + r + "，异常=" + ex)
        }
        future.thenRun {
            println("run1")
        }.thenRun {
            println("run2")
        }
    }

    @Test
    fun testSynAsyn(){
        val f = CompletableFuture<Unit>()
        f.thenRun{
            val name = Thread.currentThread().name
            println("同步then: $name")
        }
        /*f.thenRunAsync{
            val name = Thread.currentThread().name
            println("异步then: $name")
        }*/

        makeThreads(1){
            val name = Thread.currentThread().name
            println("complete: $name")
            f.complete(null)
        }

        Thread.sleep(3000)
    }

    /**
     * 测试多次完成, 看看then回调会调用几次?  => 1次
     */
    @Test
    fun testMultipleComplete(){
        val f = CompletableFuture<Int>()
        val thenCounter = AtomicInteger(0)
        // 结果: 回调1次
        f.thenRun {
            println("回调" + thenCounter.incrementAndGet() + "次, 结果: " + f.get())
        }

        val completeCounter = AtomicInteger(0)
        for(i in 0..30) {
            f.complete(i)
            println("完成" + completeCounter.incrementAndGet() + "次")
        }

        f.get()
        Thread.sleep(3000)
    }
}