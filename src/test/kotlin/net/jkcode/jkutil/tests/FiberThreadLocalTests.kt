package net.jkcode.jkutil.tests

import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.SuspendExecution
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.fiber
import co.paralleluniverse.strands.Strand
import co.paralleluniverse.strands.SuspendableRunnable
import org.junit.Test
import org.junit.Assert.*
import org.hamcrest.CoreMatchers.*

/**
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2020-08-04 9:14 PM
 */
class FiberThreadLocalTests {

    @Test
    @Throws(Exception::class)
    fun testThreadLocals() {
        val tl1 = ThreadLocal<String>()
        val tl2 = InheritableThreadLocal<String>()
        tl1.set("foo")
        tl2.set("bar")

        val fiber = fiber @Suspendable {
            assertThat<String>(tl1.get(), `is`<Any>(nullValue()))
            assertThat<String>(tl2.get(), `is`<String>("bar"))

            tl1.set("koko")
            tl2.set("bubu")

            assertThat<String>(tl1.get(), `is`<String>("koko"))
            assertThat<String>(tl2.get(), `is`<String>("bubu"))

            Fiber.sleep(100)

            assertThat<String>(tl1.get(), `is`<String>("koko"))
            assertThat<String>(tl2.get(), `is`<String>("bubu"))
        }
        fiber.join()

        assertThat<String>(tl1.get(), `is`<String>("foo"))
        assertThat<String>(tl2.get(), `is`<String>("bar"))
    }

    @Test
    @Throws(Exception::class)
    fun testNoLocals() { // shitty test
        val tl1 = ThreadLocal<String>()
        val tl2 = InheritableThreadLocal<String>()
        tl1.set("foo")
        tl2.set("bar")

        val fiber = fiber @Suspendable {
            assertThat<String>(tl1.get(), `is`<Any>(nullValue()))
            assertThat<String>(tl2.get(), `is`<Any>(nullValue()))

            tl1.set("koko")
            tl2.set("bubu")

            assertThat<String>(tl1.get(), `is`<String>("koko"))
            assertThat<String>(tl2.get(), `is`<String>("bubu"))
        }.setNoLocals(true)
        fiber.join()

        assertThat<String>(tl1.get(), `is`<String>("foo"))
        assertThat<String>(tl2.get(), `is`<String>("bar"))
    }

    @Test
    @Throws(Exception::class)
    fun testInheritThreadLocals() {
        val tl1 = ThreadLocal<String>()
        tl1.set("foo")

        val fiber = fiber @Suspendable {
            assertThat<String>(tl1.get(), `is`<String>("foo"))

            Fiber.sleep(100)

            assertThat<String>(tl1.get(), `is`<String>("foo"))

            tl1.set("koko")

            assertThat<String>(tl1.get(), `is`<String>("koko"))

            Fiber.sleep(100)

            assertThat<String>(tl1.get(), `is`<String>("koko"))
        }
        fiber.inheritThreadLocals()
        fiber.join()

        assertThat<String>(tl1.get(), `is`<String>("foo"))
    }


    @Test
    @Throws(Exception::class)
    fun testThreadLocalsParallel() {
        val tl = ThreadLocal<String>()

        val n = 100
        val loops = 100
        val fibers = (0 until n).map { i ->
            fiber @Suspendable {
                for (j in 0 until loops) {
                    val tlValue = "tl-$i-$j"
                    tl.set(tlValue)
                    assertThat<String>(tl.get(), equalTo<String>(tlValue))
                    Strand.sleep(10)
                    assertThat<String>(tl.get(), equalTo<String>(tlValue))
                }
            }
        }

        for (fiber in fibers)
            fiber.join()
    }

    @Test
    @Throws(Exception::class)
    fun testInheritThreadLocalsParallel() {
        val tl = ThreadLocal<String>()
        tl.set("foo")

        val n = 100
        val loops = 100
        val fibers = (0 until n).map { i ->
            fiber @Suspendable {
                for (j in 0 until loops) {
                    val tlValue = "tl-$i-$j"
                    tl.set(tlValue)
                    assertThat<String>(tl.get(), equalTo<String>(tlValue))
                    Strand.sleep(10)
                    assertThat<String>(tl.get(), equalTo<String>(tlValue))
                }
            }.inheritThreadLocals()
        }

        for (fiber in fibers)
            fiber.join()
    }

}