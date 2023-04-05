package net.jkcode.jkutil.tests.lock

import java.util.concurrent.locks.*

object LockService {
    private val lock: Lock = ReentrantLock()
    fun testMethod() {
        lock.lock()
        val name = Thread.currentThread().name
        for (i in 0..4) {
            println("ThreadName = " + name + (" " + (i + 1)))
        }
        lock.unlock()
    }

    // 每个线程的打印1-5都是同步进行，顺序没有乱。
    @JvmStatic
    fun main(args: Array<String>) {
        for (i in 0..5) {
            Thread { testMethod() }.start()
        }
        Thread.sleep((1000 * 5).toLong())
    }
}