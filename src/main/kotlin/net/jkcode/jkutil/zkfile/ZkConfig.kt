package net.jkcode.jkutil.zkfile

import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jkutil.common.getOrPutOnce
import java.util.concurrent.ConcurrentHashMap

/**
 * zookeeper上的配置文件数据, 支持从远端(zookeeper)加载配置
 *
 * 设计目标：
 *   从zk中获得(当前命名空间+当前应用)目录下的配置文件
 *   配合 jkcfig 在zk上生成的目录结构
 *
 * zk目录结构如下:
 * ```
 * jkcfig
 *  	default # 命名空间
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
class ZkConfig(public override val file: String /* 配置文件 */) : IConfig() {

    /**
     * 配置项
     */
    public override val props: Map<String, *>
        get() {
            return ZkConfigFiles.getFileProps(file)
        }

    companion object{
        /**
         * 缓存配置数据
         *   key 文件名
         *   value 配置数据
         */
        private val configs: ConcurrentHashMap<String, ZkConfig> = ConcurrentHashMap()

        /**
         * 获得配置数据
         * 例子：
         * <code>
         *      val config = ZkConfig.instance("config.yml", "UTF-8");
         *      String username = config.get("username");
         *      String password = config.get("password");
         *
         *      username = ZkConfig.instance("other_config.yml").get("username");
         *      password = ZkConfig.instance("other_config.yml").get("password");
         * <code>
         *
         * @param file zk的配置文件名
         */
        @JvmStatic
        @JvmOverloads
        public fun instance(file: String): ZkConfig {
            return configs.getOrPutOnce(file){
                ZkConfig(file)
            }
        }
    }

}
