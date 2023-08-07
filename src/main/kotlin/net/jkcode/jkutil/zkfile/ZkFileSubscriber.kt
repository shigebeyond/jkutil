package net.jkcode.jkutil.zkfile

import net.jkcode.jksoa.rpc.registry.zk.listener.ZkChildListener
import net.jkcode.jkutil.common.commonLogger
import net.jkcode.jkutil.common.getOrPutOnce
import net.jkcode.jkutil.scope.ClosingOnShutdown
import net.jkcode.jkutil.zk.ZkClientFactory
import org.I0Itec.zkclient.ZkClient
import java.util.concurrent.ConcurrentHashMap

/**
 * zookeeper文件订阅器
 *
 * 设计目标：
 *   只监控(当前k8s命名空间+应用)目录下的配置文件
 *   配合 jkcfig 在zk上生成的目录结构
 *
 * zk目录结构如下:
 * ```
 * jkcfig
 *  	default # k8s命名空间
 *  		app1 # 应用
 *  			redis.yaml # 配置文件
 *              log4j.properties
 *  		app2 # 应用
 *  			redis.yaml # 配置文件
 *              log4j.properties
 * ```
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2023-7-12 11:22 AM
 */
object ZkFileSubscriber : ClosingOnShutdown() {

    /**
     * zk客户端
     */
    public val zkClient: ZkClient = ZkClientFactory.instance()

    /**
     * zk子节点监听器: <父目录 to zk子节点监听器>
     *
     * 对于 ZkChildListener
     *    实现了IZkChildListener, 处理zk子节点变化
     *    自发监听zk
     */
    private val childListeners = ConcurrentHashMap<String, ZkChildListener>()

    /**
     * 监听文件变化
     *
     * @param parentPath 父目录
     * @param listener 监听器
     */
    public fun subscribe(parentPath: String, listener: IFileListener) {
        commonLogger.info("ZkChildListener监听[{}]子节点变化", parentPath)
        // 1 获得zk子节点监听器
        childListeners.getOrPutOnce(parentPath) { // 记录监听器，以便取消监听时使用
            ZkChildListener(zkClient, parentPath, listener)
        }

        // 2 刷新文件: 通知监听器更新缓存的文件
        listFiles(parentPath)
    }

    /**
     * 取消监听文件变化
     *
     * @param parentPath 父目录
     * @param listener 监听器
     */
    public fun unsubscribe(parentPath: String) {
        commonLogger.info("ZkChildListener取消监听[{}]子节点变化", parentPath)
        // 获得zk子节点监听器
        val childListener = childListeners[parentPath]!!
        if (childListener != null) {
            // 关闭: 清理监听器
            childListener.close()
            // 删除zk子节点监听器
            childListeners.remove(parentPath)
        }
    }

    /**
     * 列出文件
     * @param parentPath 父目录
     * @return 文件
     */
    public fun listFiles(parentPath: String): List<String> {
        // 获得子节点
        var children: List<String> = emptyList()
        if (zkClient.exists(parentPath))
            children = zkClient.getChildren(parentPath)

        // 处理文件变化, 从而触发 IDiscoveryListener
        childListeners[parentPath]!!.handleChildChange(parentPath, children)
        return children
    }

    override fun close() {
        for (parentPath in childListeners.keys())
            unsubscribe(parentPath)
    }
}