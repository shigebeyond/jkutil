package net.jkcode.jkutil.tests

import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.FiberForkJoinScheduler
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.fiber
import co.paralleluniverse.strands.SuspendableCallable
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.reflect.KProperty

/**
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2020-08-07 9:58 AM
 */
class FiberKotlinTests {

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        @Suspendable
        inline fun fiberSleepInline() {
            Fiber.sleep(1)
        }
    }

    val scheduler = FiberForkJoinScheduler("test", 4, null, false)

    class D {
        var v = true

        @Suspendable
        operator fun getValue(thisRef: Any?, prop: KProperty<*>): Boolean {
            fiberSleepInline()
            return v
        }

        @Suppress("UNUSED_PARAMETER")
        @Suspendable
        operator fun setValue(thisRef: Any?, prop: KProperty<*>, value: Boolean) {
            fiberSleepInline()
            v = value
        }
    }

    @Test
    fun testLocalValDelegProp() {
        val ip by D()
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            ip
        }).start().get())
    }

    @Test
    fun testLocalVarGetDelegProp() {
        @Suppress("CanBeVal")
        var mp by D()
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            mp
        }).start().get())
    }

    @Test
    fun testLocalVarSetDelegProp() {
        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
        var mp by D()
        assertFalse(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            mp = false
            mp
        }).start().get())
    }

    @Test
    fun testLocalValInlineDelegProp() {
        val ipInline by DInline()
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            ipInline
        }).start().get())
    }

    @Test
    fun testLocalVarInlineGetDelegProp() {
        @Suppress("CanBeVal")
        var mpInline by DInline()
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            mpInline
        }).start().get())
    }

    @Test
    fun testLocalVarInlineSetDelegProp() {
        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
        var mpInline by DInline()
        assertFalse(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            mpInline = false
            mpInline
        }).start().get())
    }

    val iv = true
        @Suspendable get() {
            fiberSleepInline()
            return field
        }

    @Test
    fun testOOValPropGet() {
        /*assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            iv
        }).start().get())*/

        assertTrue(fiber(scheduler = scheduler)@Suspendable {
            iv
        }.get())
    }

    @Test
    fun testOOValPropRefGet() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            this::iv.get()
        }).start().get())
    }

    var mv = true
        @Suspendable get() {
            fiberSleepInline()
            return field
        }
        @Suspendable set(v) {
            fiberSleepInline()
            field = v
        }

    @Test
    fun testOOVarPropGet() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            mv
        }).start().get())
    }

    @Test
    fun testOOVarPropSet() {
        assertFalse(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            mv = false
            mv
        }).start().get())
    }

    @Test
    fun testOOVarPropRefGet() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            this::mv.get()
        }).start().get())
    }

    @Test
    fun testOOVarPropRefSet() {
        assertFalse(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            this::mv.set(false)
            this::mv.get()
        }).start().get())
    }

    var md by D()
        @Suspendable get
        @Suspendable set

    @Test
    fun testOODelegVarPropGet() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            md
        }).start().get())
    }

    @Test
    fun testOODelegVarPropSet() {
        assertFalse(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            md = false
            md
        }).start().get())
    }

    @Test
    fun testOODelegVarPropRefGet() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            this::md.get()
        }).start().get())
    }

    @Test
    fun testOODelegVarPropRefSet() {
        assertFalse(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            this::md.set(false)
            this::md.get()
        }).start().get())
    }

    val ivInline: Boolean
        @Suspendable inline get() {
            fiberSleepInline()
            return true
        }

    @Test
    fun testOOValPropGetInline() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            ivInline
        }).start().get())
    }

    @Test
    fun testOOValPropRefGetInline() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            this::ivInline.get()
        }).start().get())
    }

    var mvInline: Boolean
        @Suspendable inline get() {
            fiberSleepInline()
            return true
        }
        @Suspendable inline set(_) {
            fiberSleepInline()
        }

    @Test
    fun testOOVarPropGetInline() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            mvInline
        }).start().get())
    }

    @Test
    fun testOOVarPropSetInline() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            mvInline = false
            mvInline
        }).start().get())
    }

    @Test
    fun testOOVarPropRefGetInline() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            this::mvInline.get()
        }).start().get())
    }

    @Test
    fun testOOVarPropRefSetInline() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            this::mvInline.set(false)
            this::mvInline.get()
        }).start().get())
    }

    class DInline {
        var v = true

        @Suppress("NOTHING_TO_INLINE")
        @Suspendable
        inline operator fun getValue(thisRef: Any?, prop: KProperty<*>): Boolean {
            fiberSleepInline()
            return v
        }

        @Suppress("UNUSED_PARAMETER", "NOTHING_TO_INLINE")
        @Suspendable
        inline operator fun setValue(thisRef: Any?, prop: KProperty<*>, value: Boolean) {
            fiberSleepInline()
            v = value
        }
    }

    var mdInline by DInline()
        @Suspendable get
        @Suspendable set

    @Test
    fun testOODelegVarPropGetInline() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            mdInline
        }).start().get())
    }

    @Test
    fun testOODelegVarPropSetInline() {
        assertFalse(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            mdInline = false
            mdInline
        }).start().get())
    }

    @Test
    fun testOODelegVarPropRefGetInline() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            this::mdInline.get()
        }).start().get())
    }

    @Test
    fun testOODelegVarPropRefSetInline() {
        assertFalse(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            this::mdInline.set(false)
            this::mdInline.get()
        }).start().get())
    }

    enum class E(val data: Int?) {
        V1(0),
        V2(1) {
            @Suspendable
            override fun enumFun() {
                fiberSleepInline()
            }
        };

        @Suspendable
        open fun enumFun() {
            data
            fiberSleepInline()
        }
    }

    @Test
    fun testOOEnum1() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            E.V1.enumFun()
            true
        }).start().get())
    }

    @Test
    fun testOOEnum2() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            E.V2.enumFun()
            true
        }).start().get())
    }

    @Test
    fun testOOEnumExt() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            doFiberSleep()
            true
        }).start().get())
    }

    @Suppress("unused")
    val E.ivE: Boolean
        @Suspendable get() {
            fiberSleepInline()
            return true
        }

    @Test
    fun testOOExtValPropGet() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            E.V2.ivE
        }).start().get())
    }

    @Suppress("unused")
    var E.mvE: Boolean
        @Suspendable get() {
            fiberSleepInline()
            return true
        }
        @Suspendable set(_) {
            fiberSleepInline()
        }

    @Test
    fun testOOExtVarPropGet() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            E.V2.mvE
        }).start().get())
    }

    @Test
    fun testOOExtVarPropSet() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            E.V2.mvE = false
            E.V2.mvE
        }).start().get())
    }

    @Suppress("unused")
    var E.mdE by D()
        @Suspendable get
        @Suspendable set

    @Test
    fun testOOExtDelegVarPropGet() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            E.V1.mdE
        }).start().get())
    }

    @Test
    fun testOOExtDelegVarPropSet() {
        assertFalse(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            E.V1.mdE = false
            E.V1.mdE
        }).start().get())
    }

    @Suppress("unused")
    val E.ivEInline: Boolean
        @Suspendable inline get() {
            fiberSleepInline()
            return true
        }

    @Test
    fun testOOExtValPropGetInline() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            E.V2.ivEInline
        }).start().get())
    }

    @Suppress("unused")
    var E.mvEInline: Boolean
        @Suspendable inline get() {
            fiberSleepInline()
            return true
        }
        @Suspendable inline set(_) {
            fiberSleepInline()
        }

    @Test
    fun testOOExtVarPropGetInline() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            E.V2.mvEInline
        }).start().get())
    }

    @Test
    fun testOOExtVarPropSetInline() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            E.V2.mvEInline = false
            E.V2.mvEInline
        }).start().get())
    }

    @Suppress("unused")
    var E.mdEInline by DInline()
        @Suspendable get
        @Suspendable set

    @Test
    fun testOOExtDelegVarPropGetInline() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            E.V1.mdEInline
        }).start().get())
    }

    @Test
    fun testOOExtDelegVarPropSetInline() {
        assertFalse(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            E.V1.mdEInline = false
            E.V1.mdEInline
        }).start().get())
    }

    @Suspendable
    fun outerDoSleep(): Boolean {
        fiberSleepInline()
        return true
    }

    open class Base(val data: Int = 0) {
        // NOT SUPPORTED: Kotlin's initializers are named <init> and we don't instrument those. Not an issue
        // because they're called by constructors which we don't instrument either (because it is most probably
        // impossible to unpark them).

        // @Suspendable { fiberSleepInline() }

        @Suspendable
        open fun doSleep(): Boolean {
            data
            fiberSleepInline()
            return true
        }
    }

    @Test
    fun testOOMethodRef() {
        val b = Base()
        assertTrue(Fiber(scheduler, b::doSleep).start().get())

    }

    interface BaseTrait1 {
        @Suspendable
        fun doSleep(): Boolean {
            fiberSleepInline()
            return true
        }
    }

    interface BaseTrait2 {
        @Suspendable
        fun doSleep(): Boolean {
            fiberSleepInline()
            return true
        }
    }

    open class Derived(data: Int) : Base(data) {
        final override @Suspendable
        fun doSleep(): Boolean {
            fiberSleepInline()
            return true
        }
    }

    abstract class DerivedAbstract1 : Base(1) {
        override abstract @Suspendable
        fun doSleep(): Boolean
    }

    class DerivedDerived1 : Base(1) {
        override @Suspendable
        fun doSleep(): Boolean {
            fiberSleepInline()
            return true
        }
    }

    abstract class DerivedDerived2 : BaseTrait1, DerivedAbstract1()

    class DerivedDerived3 : DerivedAbstract1(), BaseTrait1, BaseTrait2 {
        override @Suspendable
        fun doSleep(): Boolean {
            return super<BaseTrait2>.doSleep()
        }
    }

    open inner class InnerDerived : DerivedAbstract1(), BaseTrait2 {
        override @Suspendable
        fun doSleep(): Boolean {
            return outerDoSleep()
        }
    }

    // TODO: https://youtrack.jetbrains.com/issue/KT-10532, still open in 1.1.3
    /*
    companion object : DerivedDerived2(), BaseTrait1 {
        override @Suspendable fun doSleep() {
            super<DerivedDerived2>.doSleep()
        }
    }

    @Test public fun testOODefObject() {
        assertTrue(Fiber(scheduler, SuspendableCallable<kotlin.Boolean> @Suspendable {
            OOTest.Companion.doSleep()
            true
        }).start().get())
    }
    */

    class Data : DerivedDerived2(), BaseTrait2 {
        @Suspendable
        override fun doSleep(): Boolean {
            return super<BaseTrait2>.doSleep()
        }

        @Suspendable
        fun doSomething() {
            fiberSleepInline()
        }
    }

    class Delegating(bb2: BaseTrait2) : Base(), BaseTrait2 by bb2 {
        @Suspendable
        override fun doSleep(): Boolean {
            fiberSleepInline()
            return true
        }
    }

    @Suppress("unused")
    @Suspendable
    fun Any?.doFiberSleep() {
        fiberSleepInline()
    }

    @Test
    fun testOOExtFun() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            null.doFiberSleep()
            true
        }).start().get())
    }

    object O : DerivedDerived2()

    @Test
    fun testOOSimple() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            Base().doSleep()
            true
        }).start().get())
    }

    @Test
    fun testOOInheritingObjectLiteral() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            (object : BaseTrait1 {}).doSleep()
            true
        }).start().get())
    }

    @Test
    fun testDerived() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            Derived(1).doSleep()
            true
        }).start().get())
    }

    @Test
    fun testOOOverridingObjectLiteral() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            (object : DerivedAbstract1() {
                override @Suspendable
                fun doSleep(): Boolean {
                    fiberSleepInline()
                    return true
                }
            }).doSleep()
            true
        }).start().get())
    }

    @Test
    fun testOODerived1() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            DerivedDerived1().doSleep()
            true
        }).start().get())
    }

    @Test
    fun testOODerived2() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            (object : DerivedDerived2() {}).doSleep()
            true
        }).start().get())
    }

    @Test
    fun testOODerived3() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            DerivedDerived3().doSleep()
            true
        }).start().get())
    }

    @Test
    fun testOOInnerDerived() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            InnerDerived().doSleep()
            true
        }).start().get())
    }

    @Test
    fun testOOOuter() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            outerDoSleep()
            true
        }).start().get())
    }

    @Test
    fun testOOData() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            Data().doSomething()
            true
        }).start().get())
    }

    @Test
    fun testOODataInherited() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            Data().doSleep()
            true
        }).start().get())
    }

    @Test
    fun testOOObjectDecl() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            O.doSleep()
            true
        }).start().get())
    }

    @Test
    fun testOODeleg() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            Delegating(DerivedDerived3()).doSleep()
            true
        }).start().get())
    }

    val lp: Boolean by lazy{
        true
    }

    @Test
    fun testLazyPropGet() {
        assertTrue(Fiber(scheduler, SuspendableCallable<Boolean> @Suspendable {
            fiberSleepInline()
            true
        }).start().get())
    }
}
