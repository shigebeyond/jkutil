package net.jkcode.jkutil.flusher

import net.jkcode.jkutil.common.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 请求队列
 *    单个请求 = 请求参数 + 异步响应
 */
typealias RequestQueue<RequestType, ResponseType> = ConcurrentLinkedQueue<Pair<RequestType, CompletableFuture<ResponseType>>>

/**
 * 请求队列刷盘器
 *    定时刷盘 + 定量刷盘
 *    注意: 1 使用 ConcurrentLinkedQueue 来做队列, 其 size() 是遍历性能慢, 尽量使用 isEmpty()
 *         2 doFlush()直接换新的队列, 而处理旧的队列
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-12 5:52 PM
 */
abstract class RequestQueueFlusher<RequestType /* 请求类型 */, ResponseType /* 响应值类型 */> (
        flushQuota: Int, // 触发刷盘的计数大小
        flushTimeoutMillis: Long // 触发刷盘的定时时间
): ITimeFlusher<RequestType, ResponseType>(flushQuota, flushTimeoutMillis) {

    /**
     * 队列池
     */
    protected val queuePool = SimpleObjectPool(){
        RequestQueue<RequestType, ResponseType>()
    }

    /**
     * 列表池, 用于在doFlush()将队列请求转为List, 方便用户处理
     */
    protected val listPool = SimpleObjectPool(){
        ArrayList<RequestType>()
    }

    /**
     * 2个请求队列来轮换
     *    单个请求 = 请求参数 + 异步响应
     */
    protected val queues: Array<RequestQueue<RequestType, ResponseType>> = Array(2){
        queuePool.borrowObject()
    }

    /**
     * 获得请求计数
     * @return
     */
    public override fun requestCount(): Int {
        val index = currIndex()
        return queues[index].size
    }

    /**
     * 单个请求入队
     * @param req
     * @return
     */
    public override fun add(req: RequestType): CompletableFuture<ResponseType> {
        val index = currIndex()
        val queue = queues[index]

        // 添加请求
        val resFuture = CompletableFuture<ResponseType>()
        queue.offer(req to resFuture) // 返回都是true

        // 尝试定量刷盘
        tryFlushWhenAdd(queue.size)
        return resFuture
    }


    /**
     * 处理旧索引的请求
     * @param oldIndex 旧的索引, 因为新的索引已切换, 现在要处理旧的索引的数据
     */
    protected override fun doFlush(oldIndex: Int){
        val oldQueue = queues[oldIndex]
        queues[oldIndex] = queuePool.borrowObject() // 换一个新的请求队列

        // 无请求要处理
        if(oldQueue.isEmpty()) {
            // 归还队列
            queuePool.returnObject(oldQueue)
            return
        }

        // 收集请求, 转为List, 方便用户处理
        //val reqs = decorateCollection(oldQueue){ it.first } // 批量操作可能会涉及到序列化存库, 因此不要用 CollectionDecorator
        val reqs = oldQueue.mapTo(listPool.borrowObject()) { it.first }

        // 处理刷盘请求
        trySupplierFuture {
            handleRequests(reqs, oldQueue)
        }.whenComplete { r, ex ->
            // 无响应值: 响应值值类型为 Void / Unit, 则框架帮设置异步响应
            if (noResponse)
                oldQueue.forEach { (req, resFuture) ->
                    if(ex == null)
                        resFuture.complete(null)
                    else
                        resFuture.completeExceptionally(ex)
                }

            // 清空+归还队列
            oldQueue.clear()
            queuePool.returnObject(oldQueue)

            // 清空+归还列表
            reqs.clear()
            listPool.returnObject(reqs)
        }
    }

    /**
     * 是否无响应值
     *    即响应值值类型为 Void / Unit, 则框架帮设置异步响应
     */
    protected open val noResponse: Boolean = false
        /*get() {
            // wrong: 由于后代类继承太深, 其中子类UnitRequestQueueFlusher抹掉了第二个泛型, 导致UnitRequestQueueFlusher子类获得第二个泛型出错
            val responseType = this.javaClass.getSuperClassGenricType(1)
            return responseType == Void::class.java || responseType == Unit::class.java

            // right 直接在子类改写
        }*/

    /**
     * 处理刷盘的请求
     *     如果 ResponseType != Void/Unit, 则需要你主动设置异步响应
     * @param reqs
     * @param req2ResFuture
     * @return
     */
    protected abstract fun handleRequests(reqs: List<RequestType>, req2ResFuture: Collection<Pair<RequestType, CompletableFuture<ResponseType>>>): CompletableFuture<*>

}