package net.jkcode.jkutil.ttl.reflect

import net.jkcode.jkutil.common.getAccessibleField

/**
 * Thread类
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-17 10:29 AM
 */
object ThreadClass {

    /**
     * Thread 类
     */
    public val clazz = Thread::class.java

    /**
     * Thread.threadLocals 字段
     */
    public val threadLocalField = clazz.getAccessibleField("threadLocals")!!

    /**
     * Thread.inheritableThreadLocals 字段
     */
    public val inheritableThreadLocalField = clazz.getAccessibleField("inheritableThreadLocals")!!

}