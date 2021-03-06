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
    private fun buildClient(name: String): org.I0Itec.zkclient.ZkClient {
        // 获得zk配置
        val config = Config.instance("zk.${name}", "yaml")

        // 创建zk客户端：查看源码得知，默认是不断重连，直到建立连接，因此不用手动处理重连
        return ZkClient(config.getString("address")!!, config.getInt("sessionTimeout", 5000)!!, config.getInt("connectionTimeout", 5000)!!)
    }
    /**
     * 获得zk连接
     *
     * @param name
     * @return
     */
    public fun instance(name: String = "default"): org.I0Itec.zkclient.ZkClient {
        return clients.getOrPutOnce(name){
            buildClient(name)
        }
    }
}