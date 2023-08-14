package net.jkcode.jkutil.zkfile

import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jkutil.common.getOrPutOnce
import java.util.concurrent.ConcurrentHashMap

/**
 * zookeeper上的配置文件数据, 支持从远端(zookeeper)加载配置
 *
 * 设计目标：
 *   从zk中获得(当前k8s命名空间+应用)目录下的配置文件
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
 * 属性file其实用的是第4层的节点名
 *
 * @author shijianhang
 * @date 2023-7-8 下午8:02:47
 */
open class ZkConfig(
    public override val file: String, // 配置文件
    private val files: ZkConfigFiles = ZkConfigFiles.instance() // 配置文件
) : IConfig(), IConfigListener {

    /**
     * 配置项
     */
    public override val props: Map<String, *>
        get() {
            return files.getFileProps(file)
        }

    init {
        files.addConfigListener(file, this)
    }

}
