package net.jkcode.jksoa.rpc.registry.zk.listener

import net.jkcode.jkutil.zkfile.IFileListener
import net.jkcode.jkutil.common.commonLogger
import org.I0Itec.zkclient.IZkChildListener
import org.I0Itec.zkclient.ZkClient
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap

/**
 * zk中子节点变化监听器： 适配器模式, 将zk监听器接口转为代理调用fileListener接口, 就是将zk节点/数据变化事件转为文件增删改事件, 好实现zk配置(文件)动态刷新
 *    1 实现了IZkChildListener, 处理zk子节点变化
 *    2 维护子节点的zk数据监听器
 *    3 实现与代理调用 fileListener, 多加了增删zk数据监听器
 *
 * @author shijianhang
 * @create 2023-7-13 下午10:56
 **/
class ZkChildListener(
    public val zkClient: ZkClient,
    public val parentPath: String, // 父路径
    public val fileListener: IFileListener
) : IZkChildListener, Closeable, IFileListener {

    /**
     * zk节点数据监听器: <文件子路径 to zk数据监听器>
     */
    protected val dataListeners: ConcurrentHashMap<String, ZkDataListener> = ConcurrentHashMap()

    /**
     * 子文件
     */
    @Volatile
    protected var files: List<String> = ArrayList()

    /************************* 监听子节点变化 *************************/
    init {
        // 添加zk子节点监听
        zkClient.subscribeChildChanges(parentPath, this)
    }

    /**
     * 处理zk中子节点(文件)变化事件
     *     对比旧的文件, 从而识别文件的增删, 从而触发 IFileListener 的增删方法
     *
     * @param parentPath
     * @param currentChilds
     */
    @Synchronized
    public override fun handleChildChange(parentPath: String, newChilds: List<String>?) {
        val newFiles = newChilds ?: emptyList()
        // 处理配置文件变化, 从而触发 IDiscoveryListener
        commonLogger.info("处理zk[{}]子节点变化事件, 子节点为: {}", parentPath, newChilds)
        // 1 新加文件
        val addFiles = newFiles - files
        for (file in addFiles) {
            val path = parentPath + '/' + file
            val content = zkClient.readData<String>(path) // 节点值
            handleFileAdd(path, content)
        }

        // 2 删除文件
        val removeFiles = files - newFiles
        for (file in removeFiles) {
            val path = parentPath + '/' + file
            handleFileRemove(path)
        }

        files = newFiles
    }

    /************************* 监听子节点数据变化 *************************/
    /**
     * 对文件子节点添加数据监听器
     * @param path
     */
    protected fun addDataListener(path: String) {
        commonLogger.info("ZkDataListener监听[{}]数据变化", path)
        val dataListener = ZkDataListener(fileListener)
        zkClient.subscribeDataChanges(path, dataListener);
        dataListeners[path] = dataListener
    }

    /**
     * 对文件子节点删除数据监听器
     * @param path
     */
    protected fun removeDataListener(path: String) {
        commonLogger.info("ZkDataListener取消监听[{}]数据变化", path)
        val dataListener = dataListeners.remove(path)!!
        zkClient.unsubscribeDataChanges(path, dataListener)
    }

    /**
     * 关闭: 清理监听器
     */
    public override fun close() {
        // 取消zk子节点监听
        zkClient.unsubscribeChildChanges(parentPath, this)

        // 清理数据监听器
        // ConcurrentHashMap支持边遍历边删除, HashMap不支持
        for (key in dataListeners.keys)
            removeDataListener(key)
    }

    /************************* 实现与代理调用 fileListener, 多加了增删zk数据监听器 *************************/
    /**
     * 处理配置文件新增
     * @param path
     * @param allPath
     */
    public override fun handleFileAdd(path: String, content: String) {
        fileListener.handleFileAdd(path, content)

        //监听子节点的数据变化
        addDataListener(path)
    }

    /**
     * 处理配置文件删除
     * @param path
     * @param allPath
     */
    public override fun handleFileRemove(path: String) {
        fileListener.handleFileRemove(path)

        // 取消监听子节点的数据变化
        removeDataListener(path)
    }

    /**
     * 处理文件内容变化
     * @param path
     * @param content
     */
    override fun handleContentChange(path: String, content: String) {
        fileListener.handleContentChange(path, content)
    }

}