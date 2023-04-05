package net.jkcode.jkutil.tests.lock

import java.util.concurrent.locks.ReentrantReadWriteLock

object WriteWriteService {
    private val lock: ReentrantReadWriteLock = ReentrantReadWriteLock()
    fun write() {
        try {
            try {
                val name = Thread.currentThread().name
                lock.writeLock().lock()
                println("[$name]获得写锁" +
                        " " + System.currentTimeMillis())
                Thread.sleep((1000 * 10).toLong())
            } finally {
                lock.writeLock().unlock()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    // 每刻只有一个线程能获得锁
    @JvmStatic
    fun main(args: Array<String>) {
        for (i in 0..5) {
            Thread { write() }.start()
        }
        Thread.sleep((1000 * 30).toLong())
    }
}