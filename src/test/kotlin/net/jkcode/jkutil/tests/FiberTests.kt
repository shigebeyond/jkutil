package net.jkcode.jkutil.tests

import co.paralleluniverse.fibers.FiberExecutorScheduler
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.fiber
import co.paralleluniverse.strands.Strand
import io.netty.channel.DefaultEventLoop
import org.junit.Test

/**
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2020-07-30 9:23 PM
 */
class FiberTests {

    fun testFiber(){
        fiber @Suspendable {
            // The fiber will be created and will start executing this body

        }
    }

    /**
     * 测试线程的轮换
     *    fiber睡1s时, 线程是否轮换执行其他任务
     */
    @Test
    fun testThreadRoate(){
        val singleThread = DefaultEventLoop()
        val scheduler = FiberExecutorScheduler("test", singleThread)

        fiber(true, scheduler = scheduler) @Suspendable {
            println("睡之前")
            Strand.sleep(1000)
            println("睡之后")
        }

        singleThread.execute {
            println("另外的操作")
        }

        Thread.sleep(10000)
    }
}