package net.jkcode.jksoa.rpc.registry.zk.listener

import net.jkcode.jkutil.zkfile.IFileListener
import net.jkcode.jkutil.common.commonLogger
import org.I0Itec.zkclient.IZkDataListener

/**
 * zk中节点数据变化监听器
 *
 * @author shijianhang
 * @create 2023-7-14 上午12:25
 **/
class ZkDataListener(public val fileListener: IFileListener): IZkDataListener {

    /**
     * 处理zk中节点数据变化事件
     */
    @Synchronized
    public override fun handleDataChange(path: String, content: Any) {
        try {
            commonLogger.info("处理zk节点[{}]数据变化事件，数据为: {}", path, content)
            // 处理更新文件内容
            fileListener.handleContentChange(path, content as String)
        }catch(e: Exception){
            commonLogger.error("处理zk节点[$path]数据变化事件失败", e)
            throw e
        }
    }

    /**
     * 处理zk中节点数据删除事件 -- 多余, ZkChildListener 已处理过节点删除事件
     */
    @Synchronized
    public override fun handleDataDeleted(path: String) {
        //fileListener.handleFileRemove(path)
    }
}