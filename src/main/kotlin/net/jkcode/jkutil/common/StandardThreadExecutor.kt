/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.jkcode.jkutil.common

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * source from Tomcat's `org.apache.catalina.core.StandardThreadExecutor`
 * http://www.docjar.com/html/api/org/apache/catalina/core/StandardThreadExecutor.java.html
 *
 * TODO: 将 SynchronousQueue 换为性能更优的 LinkedTransferQueue
 */
class StandardThreadExecutor(protected val executor: ThreadPoolExecutor): ExecutorService by executor{

    public constructor(corePoolSize: Int, maximumPoolSize: Int, queueSize: Int = Integer.MAX_VALUE):
            this(ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60, TimeUnit.SECONDS, TaskQueue(queueSize), TaskThreadFactory("jk-exec-"))){
        (executor.queue as TaskQueue).executor = executor
    }

    public override fun execute(command: Runnable) {
        try {
            executor.execute(command)
        } catch (rx: RejectedExecutionException) {
            //there could have been contention around the queue
            if (!(executor.queue as TaskQueue).force(command))
                throw RejectedExecutionException()
        }
    }

}

// queue
internal class TaskQueue(queueSize: Int) : LinkedBlockingQueue<Runnable>(queueSize) {

    public lateinit var executor: ThreadPoolExecutor

    fun force(o: Runnable): Boolean {
        if (executor.isShutdown) throw RejectedExecutionException("Executor not running, can't force a command into the queue")
        return super.offer(o) //forces the item onto the queue, to be used if the task is rejected
    }

    /**
     * 问题: 对于 ThreadPoolExecutor 的方法 getPoolSize()/getActiveCount() 的实现是加锁的, 特别是getActiveCount()要遍历worker
     * 优化: 限制 ThreadPoolExecutor.getPoolSize() 只调用一次, 并缓存值
     *      弃用 ThreadPoolExecutor.getActiveCount(), 改用 TaskQueue.size()
     */
    override fun offer(o: Runnable): Boolean {
        //we are maxed out on threads, simply queue the object
        val poolSize = executor.poolSize
        if (poolSize == executor.maximumPoolSize)
            return super.offer(o)
        //we have idle threads, just add it to the queue
        //this is an approximation, so it could use some tuning
        //if (executor.activeCount < poolSize)
        if (this.size < poolSize)
            return super.offer(o)
        //if we have less threads than maximum force creation of a new thread
         if (poolSize < executor.maximumPoolSize)
             return false
        //if we reached here, we need to add it to the queue
        return super.offer(o)
    }
}

// thread factory
internal class TaskThreadFactory(val namePrefix: String) : ThreadFactory {
    val group: ThreadGroup
    val threadNumber = AtomicInteger(1)

    init {
        val s = System.getSecurityManager()
        group = if (s != null) s.threadGroup else Thread.currentThread().threadGroup
    }

    override fun newThread(r: Runnable): Thread {
        val t = Thread(group, r, namePrefix + threadNumber.getAndIncrement())
        t.isDaemon = true
        t.priority = Thread.NORM_PRIORITY
        return t
    }
}