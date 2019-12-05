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
            if (!(executor.queue as TaskQueue).force(command)) throw RejectedExecutionException()
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

    override fun offer(o: Runnable): Boolean {
        //we are maxed out on threads, simply queue the object
        if (executor.poolSize == executor.maximumPoolSize) return super.offer(o)
        //we have idle threads, just add it to the queue
        //this is an approximation, so it could use some tuning
        if (executor.activeCount < executor.poolSize) return super.offer(o)
        //if we have less threads than maximum force creation of a new thread
        return if (executor.poolSize < executor.maximumPoolSize) false else super.offer(o)
        //if we reached here, we need to add it to the queue
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