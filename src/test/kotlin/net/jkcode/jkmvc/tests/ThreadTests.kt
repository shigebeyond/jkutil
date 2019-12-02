package net.jkcode.jkmvc.tests

import net.jkcode.jkutil.common.currMillis
import net.jkcode.jkutil.common.getProperty
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ForkJoinPool
import kotlin.reflect.KMutableProperty1
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService



/**
 * 测试锁
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-18 5:38 PM
 */
class ThreadTests {

    @Test
    fun testCloneThreadLocalMap(){
        // ThreadLocalMap的类, 是ThreadLocal的内部私有类
        // 编码时使用内部类 "ThreadLocal.ThreadLocalMap", 但是Java在编译代码时为了区分内部类, 会将内部类名改为 "ThreadLocal$ThreadLocalMap"
        val mapClazz = Class.forName("java.lang.ThreadLocal\$ThreadLocalMap")
        println(mapClazz)
        val mapConstructor = mapClazz.getDeclaredConstructor(mapClazz)
        println(mapConstructor)
        mapConstructor.isAccessible = true
        println(mapConstructor)

        // 获得当前线程拥有的ThreadLocalMap实例
        val threadLocalProp = Thread::class.getProperty("threadLocals") as KMutableProperty1<Thread, Any?>
        val value = threadLocalProp.get(Thread.currentThread())

        // 克隆实例
        //val o = value.tryClone() // wrong: ThreadLocalMap没有实现Cloneable接口
        val o = mapConstructor.newInstance(value) // wrong: ThreadLocalMap(ThreadLocalMap parentMap) 构造函数只能用于对 InheritableThreadLocal 中的ThreadLocalMap进行复制
    }


    @Test
    fun testForkJoinPool() {
        val pool = ForkJoinPool(1)
        //val pool = Executors.newFixedThreadPool(1)
        val requests = 100000
        val latch = CountDownLatch(requests)
        val start = System.currentTimeMillis()
        for(i in 0..requests) {
            pool.execute {
                println(i)
                latch.countDown()
            }
        }
        latch.await()
        val runtime = System.currentTimeMillis() - start
        println("耗时 $runtime ms")
    }

}