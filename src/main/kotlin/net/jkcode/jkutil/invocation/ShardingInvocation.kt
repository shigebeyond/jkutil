package net.jkcode.jkutil.invocation

import net.jkcode.jkutil.common.getSignature
import net.jkcode.jkutil.common.join
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * 分片的本地方法调用
 *    本地方法调用的描述: 方法 + 参数
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:03 AM
 */
class ShardingInvocation(clazz: String, //服务接口类全名
                         methodSignature: String, //要调用的方法签名：包含方法名+参数类型
                         args: Array<Any?>, //实参
                         public override val argsPerSharding: Int // 每个分片的参数个数
) : IShardingInvocation, Invocation(clazz, methodSignature, args) {

    /**
     * 构造函数
     *
     * @param method 方法
     * @param args 实参
     */
    public constructor(method: Method, args: Array<Any?>, argsPerSharding: Int) : this(method.declaringClass.name, method.getSignature(), args, argsPerSharding)

    /**
     * 构造函数
     *
     * @param func 方法
     * @param args 实参
     */
    public constructor(func: KFunction<*>, args: Array<Any?>, argsPerSharding: Int) : this(func.javaMethod!!, args, argsPerSharding)

    /**
     * 调用
     * @return
     */
    public override fun invoke(): Any? {
        val futures = arrayOfNulls<Any?>(shardingSize)
        for(i in 0 until shardingSize){
            // 异步调用
            futures[i] = CompletableFuture.supplyAsync{
                method.invoke(bean, *getShardingArgs(i)) // 调用bean方法
            }
        }
        return (futures as Array<CompletableFuture<Any?>>).join()
    }
}