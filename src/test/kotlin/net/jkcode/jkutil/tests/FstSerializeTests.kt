package net.jkcode.jkutil.tests

import net.jkcode.jkutil.serialize.FstSerializer
import org.junit.Test
import org.nustaq.serialization.FSTConfiguration
import java.io.File
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
        runFst(conf)
    }

    /**
     * 线程安全
     */
    @Test
    fun testThreadsafe(){
        runFst(confs.get())
    }

    /**
     * 线程安全 + 不共享(不检查循环引用)
     */
    @Test
    fun testThreadsafeUnshared(){
        isShareReferences = false
        runFst(confs.get())
    }

    fun runFst(conf: FSTConfiguration){
        val obj = Man("shi", 12)
        val bytes = conf.asByteArray(obj)
        val obj2 = conf.asObject(bytes) as Man
        assertEquals(obj, obj2)
    }

    @Test
    fun testOut(){
        val serializer = FstSerializer()
        val obj = Man("shi", 12)
        val bytes = serializer.serialize(obj)!!
        File("man.data").writeBytes(bytes)
    }

    @Test
    fun testIn(){
        val serializer = FstSerializer()
        val bytes = File("man.data").readBytes()
        val obj = serializer.unserialize(bytes)
        println(obj)
    }

}