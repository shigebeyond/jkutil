package net.jkcode.jkutil.zk

import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.getOrPutOnce
import org.I0Itec.zkclient.ZkClient
import java.util.concurrent.ConcurrentHashMap

/**
 * zk连接工厂
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 11:22 AM
 */
object ZkClientFactory{

    /**
     * 缓存zk客户端
     */
    private val clients: ConcurrentHashMap<String, ZkClient> = ConcurrentHashMap()

    /**
     * 构建zk客户端
     *
     * @param name
     * @return
     */
    private fun buildClient(name: String): ZkClient {
        // 获得zk配置
        val config = Config.instance("zk.${name}", "yaml")

        // 创建zk客户端：查看源码得知，默认是不断重连，直到建立连接，因此不用手动处理重连
        val client = ZkClient(config.getString("address")!!, config.getInt("sessionTimeout", 5000)!!, config.getInt("connectionTimeout", 5000)!!)
        // 需指定序列化类类, 否则readData()报StreamCorruptedException错: https://blog.csdn.net/y277an/article/details/90726074
        client.setZkSerializer(ZkStringSerializer())
        return client
    }
    /**
     * 获得zk连接
     *
     * @param name
     * @return
     */
    public fun instance(name: String = "default"): ZkClient {
        return clients.getOrPutOnce(name){
            buildClient(name)
        }
    }
}