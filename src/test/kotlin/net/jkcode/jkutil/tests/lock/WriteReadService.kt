package net.jkcode.jkutil.tests.lock

import java.util.concurrent.locks.ReentrantReadWriteLock

object WriteReadService {
    private val lock: ReentrantReadWriteLock = ReentrantReadWriteLock()
    fun read() {
        try {
            try {
                val name = Thread.currentThread().name
                lock.readLock().lock()
                println("[$name]获得读锁" + " " + System.currentTimeMillis())
                Thread.sleep((1000 * 10).toLong())
            } finally {
                lock.readLock().unlock()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun write() {
        try {
            try {
                val name = Thread.currentThread().name
                lock.writeLock().lock()
                println("[$name]获得写锁" + " " + System.currentTimeMillis())
                Thread.sleep((1000 * 10).toLong())
            } finally {
                lock.writeLock().unlock()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    // 读写互斥
    @JvmStatic
    fun main(args: Array<String>) {
        // 读锁可并行
        for(i in 0..2) {
            val a = Thread { read() }
            a.name = "ReadThread_" + i
            a.start()
        }
        Thread.sleep(1000)

        // 写锁与读锁互斥
        val b = Thread { write() }
        b.name = "WriteThread"
        b.start()

        Thread.sleep((1000 * 30).toLong())
    }
}