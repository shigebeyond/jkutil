package net.jkcode.jkutil.zkfile

import com.alibaba.fastjson.JSONObject
import net.jkcode.jkutil.common.JkApp
import net.jkcode.jkutil.common.getOrPutOnce
import org.yaml.snakeyaml.Yaml
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.HashMap

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
 * 属性fileProps中的key其实用的是第4层的节点名
 *
 * @author shijianhang
 * @date 2023-7-8 下午8:02:47
 */
class ZkConfigFiles(public val appPath: String): IFileListener {

    /**
     * 所有配置文件的数据: <文件路径 to 配置数据>
     */
    private val fileProps: ConcurrentHashMap<String, Map<String, Any?>> = ConcurrentHashMap()

    /**
     * 获得所有配置文件
     */
    public val files: Collection<String>
        get() = fileProps.keys

    /**
     * 配置变化监听器: <配置文件名 to 配置变化监听器>
     */
    protected val configListeners: ConcurrentHashMap<String, CopyOnWriteArrayList<IConfigListener>> = ConcurrentHashMap()

    init {
        // 监听应用下配置文件变化
        ZkFileSubscriber.subscribe(appPath, this)
    }

    companion object{

        /**
         * 当前应用路径
         */
        private val currAppPath = "/jkcfg/${JkApp.namespace}/${JkApp.name}"

        /**
         * ZkConfigFiles池
         */
        private val insts: ConcurrentHashMap<String, ZkConfigFiles> = ConcurrentHashMap();

        /**
         * 获得ZkConfigFiles实例
         * @param appPath
         * @return
         */
        public fun instance(appPath: String? = null): ZkConfigFiles {
            val appPath = appPath ?: currAppPath // 默认取当前app路径
            return insts.getOrPut(appPath){
                ZkConfigFiles(appPath)
            }
        }
    }

    /************************ 实现 IFileListener zk文件变化的监听器 ***************************/
    /**
     * 处理配置文件新增
     * @param url
     */
    override fun handleFileAdd(path: String, content: String) {
        handleContentChange(path, content)
    }

    /**
     * 处理配置文件删除
     * @param url
     */
    override fun handleFileRemove(path: String) {
        val path = getFilename(path)
        fileProps.remove(path)
        triggerConfigListeners(path, null)
    }

    /**
     * 处理文件内容变化
     * @param path
     * @param content
     */
    override fun handleContentChange(path: String, content: String) {
        val path = getFilename(path)
        val type = path.substringAfterLast('.') // 扩展名即为类型
        val data = buildProperties(content, type)
        fileProps[path] = data
        triggerConfigListeners(path, data)
    }

    /**
     * 从路径中获得文件名(干掉appPath)
     *   如 /jkcfg/default/rpcserver/redis.yml 转为 redis.yml
     */
    private fun getFilename(path: String): String {
        //return path.replace(appPath, "") // wrong: /redis.yml
        return path.substring(appPath.length + 1) // right: redis.yml
    }

    /**
     * 构建配置项
     * @param content　配置文件内容
     * @param type properties | yaml | json
     * @return
     */
    public fun buildProperties(content: String, type: String): Map<String, Any?> {
        if(content.isNullOrEmpty())
            return emptyMap()

        // 解析内容
        val result = when(type){
                "properties" -> Properties().apply { load(content.reader()) } // 加载 properties 文件
                "yaml", "yml" -> Yaml().loadAs(content, HashMap::class.java) // 加载 yaml 文件
                "json" -> JSONObject.parseObject(content.reader().readText()) // 加载 json 文件
                else -> throw IllegalArgumentException("未知配置文件类型: " + type)
            }
        if(result == null)
            return emptyMap()

        return result as Map<String, *>
    }

    /**
     * 获得配置文件的配置数据
     */
    public fun getFileProps(file: String): Map<String, Any?> {
        return fileProps[file] ?: throw Exception("找到不zk配置文件[$file], 其在zk路径为 $appPath/$file}")
    }

    /**
     * 获得zk配置文件
     */
    public fun getZkConfig(file: String): ZkConfig {
        return ZkConfig(file, this)
    }

    /************************ 配置变化监听器 ***************************/
    /**
     * 添加配置变化监听器
     */
    public fun getConfigListener(file: String): CopyOnWriteArrayList<IConfigListener> {
        return configListeners.getOrPutOnce(file){
            CopyOnWriteArrayList()
        }
    }

    /**
     * 触发配置变化监听器
     */
    private fun triggerConfigListeners(file: String, data: Map<String, Any?>?){
        for (l in getConfigListener(file))
            l.handleConfigChange(data)
    }

    /**
     * 添加配置变化监听器
     */
    public fun addConfigListener(file: String, listener: IConfigListener){
        getConfigListener(file).add(listener)
    }

    /**
     * 添加配置变化监听器
     */
    public fun addConfigListener(file: String, callback: (data: Map<String, Any?>?)->Unit){
        val listener = object: IConfigListener{
            override fun handleConfigChange(data: Map<String, Any?>?){
                callback.invoke(data)
            }
        }
        getConfigListener(file).add(listener)
    }

    /**
     * 删除配置变化监听器
     */
    public fun removeConfigListener(file: String, listener: IConfigListener){
        configListeners[file]?.remove(listener)
    }

}
