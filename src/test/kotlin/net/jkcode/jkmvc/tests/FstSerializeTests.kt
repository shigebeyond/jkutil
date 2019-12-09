package net.jkcode.jkmvc.tests

import org.junit.Test
import org.nustaq.serialization.FSTConfiguration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.test.assertEquals

/**
 * 测试序列化
 * @author shijianhang<772910474@qq.com>
 * @date 2019-10-17 8:43 PM
 */
class FstSerializeTests {

    val requests:Int = 10000000

    val concurrents: Int = 5


    val conf: FSTConfiguration = FSTConfiguration.createDefaultConfiguration()

    // 默认是true
    var isShareReferences = true

    val confs: ThreadLocal<FSTConfiguration> = ThreadLocal.withInitial {
        val c = FSTConfiguration.createDefaultConfiguration()
        // 默认是true
        c.isShareReferences = isShareReferences
        c
    }

    /**
     * 单例
     * 18109ms
     *
     */
    @Test
    fun testSingleton(){
        runNFst(conf)
    }

    /**
     * 线程安全
     */
    @Test
    fun testThreadsafe(){
        runNFst(confs.get())
    }

    /**
     * 线程安全 + 不共享(不检查循环引用)
     */
    @Test
    fun testThreadsafeUnshared(){
        isShareReferences = false
        runNFst(confs.get())
    }

    fun run1Fst(conf: FSTConfiguration){
        val obj = Man("shi", 12)
        val bytes = conf.asByteArray(obj)
        val obj2 = conf.getObjectInput(bytes).readObject() as Man
        assertEquals(obj, obj2)
    }

    fun runNFst(conf: FSTConfiguration){
        val pool = Executors.newFixedThreadPool(concurrents)
        val results = (1..5).map{
            val latch = CountDownLatch(requests)
            val start = System.currentTimeMillis()
            for (i in 0..requests) {
                pool.execute {
                    latch.countDown()
                }
            }
            latch.await()
            val runtime = System.currentTimeMillis() - start
            println("第 $it 轮耗时 $runtime ms")
            runtime
        }

        println("最小耗时 "+ results.min() + " ms")
    }

}