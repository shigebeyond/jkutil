package net.jkcode.jkutil.scope

import net.jkcode.jkutil.common.JkApp
import net.jkcode.jkutil.ttl.ScopedTransferableThreadLocal
import net.jkcode.jkutil.ttl.SttlInterceptor
import java.io.Closeable
import java.util.concurrent.CompletableFuture

// 针对所有请求的请求作用域
object GlobalAllRequestScope : IRequestScope() {}

// 针对rpc请求的请求作用域: 包含所有请求的作用域
object GlobalRpcRequestScope : IRequestScope() {
    init {
        addChildScope(GlobalAllRequestScope as IScope)
    }
}

// 针对http请求的请求作用域: 包含所有请求的作用域
object GlobalHttpRequestScope : IRequestScope() {
    init {
        addChildScope(GlobalAllRequestScope as IScope)
    }
}

/**
 * 请求作用域
 *    1. 对应的请求处理器, 承诺在请求处理前后调用其  beginScope()/endScope()
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-16 3:48 PM
 */
open class IRequestScope : HierarchicalScope(), Closeable {

    init {
        // 关机时要关闭
        ClosingOnShutdown.addClosing(this)
    }

    /**
     * 可能没有开始请求作用域, 则需要关机时主动结束作用域(释放资源)
     *    如cli环境中调用Db
     */
    public override fun close() {
        endScope()
    }

    /**
     * 包装请求处理, 添加作用域处理
     *    1 作用域的开始与结束
     *    2 ScopedTransferableThreadLocal 的传递
     * @param reqAction
     * @return
     */
    public inline fun <T> sttlWrap(reqAction: () -> CompletableFuture<T>): CompletableFuture<T> {
        // 不应用sttl时, 不包装
        if(!JkApp.useSttl)
            return reqAction.invoke()

        // 1 请求处理前，开始作用域
        // 必须在拦截器之前调用, 因为拦截器可能引用请求域的资源
        this.beginScope()

        // 2 调用请求处理, 并包装 ScopedTransferableThreadLocal 的传递
        return SttlInterceptor.wrap(action = reqAction)
                .whenComplete { r, ex ->
                    // 3 请求处理后，结束作用域(关闭资源)
                    this.endScope()
                }
    }

}