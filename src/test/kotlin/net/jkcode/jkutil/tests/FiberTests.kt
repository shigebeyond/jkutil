package net.jkcode.jkutil.tests

import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.FiberExecutorScheduler
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.fiber
import co.paralleluniverse.strands.Strand
import io.netty.channel.DefaultEventLoop
import org.junit.Test
import co.paralleluniverse.fibers.FiberAsync
import co.paralleluniverse.kotlin.Receive
import co.paralleluniverse.kotlin.Send
import co.paralleluniverse.kotlin.select
import co.paralleluniverse.strands.channels.Channels
import co.paralleluniverse.strands.dataflow.Val
import co.paralleluniverse.strands.dataflow.Var
import net.jkcode.jkutil.common.randomBoolean
import org.junit.Assert.assertTrue

object Foo{

    /**
     * foo业务异步处理, 并设置回调
     */
    fun asyncOp(c: FooCompletion){
        // 异常: java.util.concurrent.ExecutionException: java.lang.IllegalStateException: Not called on a fiber (current strand: null)
        // 原因: FiberAsync.run() 是先调用 Fiber.park() (协程被打包了暂停了, 当前线程已无协程), 然后才调用 requestAsync(), 因此你在 requestAsync() 调用 Strand.sleep(1000) 当然就报无协程的异常
        // Strand.sleep(1000)
        Thread.sleep(1000)
        if(randomBoolean())
            c.success("foo success result")
        else
            c.failure(Exception("foo exception result"))
    }
}

/**
 * foo业务异步处理的异步回调
 */
interface FooCompletion {
    fun success(result: String)
    fun failure(exception: Exception)
}

/**
 * 包装foo业务的回调, 为 FiberAsync
 *   其中 requestAsync() 主要是触发 foo业务异步处理, 并设置回调
 *   FiberAsync 必须运行在fiber中
 */
class FooAsync : FiberAsync<String, Exception>(), FooCompletion {
    /**
     * 触发 foo业务异步处理, 并设置回调
     */
    override fun requestAsync() {
        Foo.asyncOp(this);
    }

    // 异步成功回调
    override fun success(result: String) {
        asyncCompleted(result)
    }

    // 异步失败回调
    override fun failure(exception: Exception) {
        asyncFailed(exception)
    }
}

/**
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2020-07-30 9:23 PM
 */
class FiberTests {

    @Test
    fun testFiber(){
        val f = fiber @Suspendable {
            // The fiber will be created and will start executing this body

        }
        println(f.get())
    }

    @Test
    fun testFooAsync(){
        // FooAsync().run() // 异常: java.lang.IllegalThreadStateException: Method called not from within a fiber

        //FiberAsync 必须运行在fiber中
        val f = fiber @Suspendable{
            FooAsync().run()
        }
        println(f.get())
    }

    /**
     * 测试线程的轮换
     *    fiber睡1s时, 线程是否轮换执行其他任务
     */
    @Test
    fun testThreadRoate(){
        val singleThread = DefaultEventLoop()
        val scheduler = FiberExecutorScheduler("test", singleThread)

        val f = fiber(true, scheduler = scheduler) @Suspendable {
            println("睡之前")
            Strand.sleep(1000)
            println("睡之后")
            "done"
        }

        singleThread.execute {
            println("另外的操作")
        }

        println(f.get())
    }

    @Test
    fun testChannel() {
        val ch1 = Channels.newChannel<Int>(1)
        val ch2 = Channels.newChannel<Int>(1)

        assertTrue (
                fiber @Suspendable {
                    select(Receive(ch1), Send(ch2, 2)) { //  Send(ch2, 2) 先触发
                        it
                    }
                }.get() is Send
        )

        ch1.send(1)

        assertTrue (
                fiber @Suspendable {
                    select(Receive(ch1), Send(ch2, 2)) { // ch1.send(1) 先触发
                        when (it) {
                            is Receive -> it.msg
                            is Send -> 0
                            else -> -1
                        }
                    }
                }.get() == 1
        )

        Strand.sleep(2000); //
    }

    @Test
    fun testDataFlow(){
        val a = Val<Int>()
        val x = Var<Int>()
        val y = Var { a.get() + x.get() * 10 }
        val z = Var {
            val res = y.get()
            println("res: $res") // 从 193 开始, 因为主线程中 Strand.sleep(2000), 导致fiber过了20个循环, 每个循环睡100ms
            res
        }

        val f = fiber @Suspendable {
            for (i in 0..100) {
                x.set(i)
                Strand.sleep(100)
            }
        }

        Strand.sleep(2000); //
        a.set(3); // this will trigger everything
        f.join();
    }
}