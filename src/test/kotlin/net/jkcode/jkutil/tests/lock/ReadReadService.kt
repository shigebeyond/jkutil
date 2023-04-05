package net.jkcode.jkutil.tests.lock

import java.util.concurrent.locks.ReentrantReadWriteLock

object ReadReadService {
    private val lock: ReentrantReadWriteLock = ReentrantReadWriteLock()

    fun read() {
        try {
            try {
                val name = Thread.currentThread().name
                lock.readLock().lock()
                println("[$name]获得读锁" +
                        " " + System.currentTimeMillis())
                Thread.sleep((1000 * 10).toLong())
            } finally {
                lock.readLock().unlock()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    //5个线程几乎同时执行同步代码
    //读锁所有线程都能获得
    @JvmStatic
    fun main(args: Array<String>) {
        for (i in 0..5) {
            Thread { read() }.start()
        }
        Thread.sleep((1000 * 5).toLong())
    }
}