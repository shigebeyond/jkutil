package net.jkcode.jkutil.tests

import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.fiber

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
}