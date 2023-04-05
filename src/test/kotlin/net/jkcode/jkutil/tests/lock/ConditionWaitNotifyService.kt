package net.jkcode.jkutil.tests.lock

import java.util.concurrent.locks.*

object ConditionWaitNotifyService {
    private val lock: Lock = ReentrantLock()
    var condition: Condition = lock.newCondition()

    fun await() {
        val name = Thread.currentThread().name
        try {
            lock.lock()
            println("lock: $name")
            println("[$name] await开始时间为 " + System.currentTimeMillis())
            condition.await()
            println("[$name] await结束时间" + System.currentTimeMillis())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            println("unlock: $name")
            lock.unlock()
        }
    }

    fun signal() {
        val name = Thread.currentThread().name
        try {
            lock.lock()
            println("lock: $name")
            println("[$name] sign的时间为" + System.currentTimeMillis())
            condition.signal()
        } finally {
            println("unlock: $name")
            lock.unlock()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Thread { await() }.start()
        Thread.sleep((1000 * 3).toLong())
        signal()
        Thread.sleep(1000)
    }

}