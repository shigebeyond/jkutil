package net.jkcode.jkutil.common


import com.alibaba.fastjson.JSONObject
import net.jkcode.jkutil.singleton.BeanSingletons
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.net.URL
import java.nio.file.FileSystems
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

/**
 * 配置数据，用于加载配置文件，并读取配置数据
 * Config data, can load properties file from CLASSPATH or File object.
 *
 * @author shijianhang
 * @date 2016-10-8 下午8:02:47
 */
open class Config(
        override val props: Map<String, *>, // 配置项
        public override val file: String = "", // 配置文件
        public val merging: Boolean = false // 是否合并
): IConfig() {

    companion object{
        /**
         * 缓存配置数据
         *   key 文件名
         *   value 配置数据
         */
        private val configs:ConcurrentHashMap<String, Config> = ConcurrentHashMap()

        /**
         * 获得配置数据，如果没有数据，则加载配置文件来读取数据
         * Get the config data. It will loading the properties file if not loading.
         *
         * 例子：
         * <code>
         *      val config = Config.instance("config.yml", "UTF-8");
         *      String username = config.get("username");
         *      String password = config.get("password");
         *
         *      username = Config.instance("other_config.yml").get("username");
         *      password = Config.instance("other_config.yml").get("password");
         *
         *      Config.instance("com/jfinal/config_in_sub_directory_of_classpath.yml");
         * <code>
         *
         * @param file the properties file's name in classpath or the sub directory of classpath
         * @param type properties | yaml
         * @param merging
         */
        @JvmStatic
        @JvmOverloads
        public fun instance(file: String, type: String = "properties", merging: Boolean = false): Config {
            // 解析出文件名 + 子项路径
            var filename:String = file
            var path:String? = null
            val i = file.indexOf('.')
            if(i > -1){
                filename = file.substring(0, i)
                path = file.substring(i + 1)
            }
            // 获得文件的配置项
            val config = configs.getOrPutOnce(filename){
                Config("$filename.$type", type, merging)
            }!!
            if(config.merging != merging)
                throw IllegalArgumentException("配置文件[$file]已加载过, 但本地加载与之前加载的 merging 参数不一样")

            // 无子项
            if(path == null)
                return config
            // 有子项
            return config.getPathConfig(path)
        }

        /**
         * 构建配置项
         *
         * @param file the properties file's name in classpath or the sub directory of classpath
         * @param type properties | yaml | json
         * @param merging
         * @return
         */
        public fun buildProperties(file:String, type: String = "properties", merging: Boolean = false): Map<String, *> {
            val path = FileSystems.getDefault().getPath(file)
            val urls = if(path.isAbsolute) {
                            //val url = URL("file://$file")
                            val url = File(file).toURL()
                            listOf(url).enumeration()
                        }else
                            Thread.currentThread().contextClassLoader.getResources(file)
            if(!urls.hasMoreElements())
                throw IllegalArgumentException("配置文件[$file]不存在")
            // 1 不合并: 取第一个
            if (!merging)
                return buildProperties(urls.nextElement(), type)

            // 2 合并: 先来先写, 后来不覆盖
            val result = HashMap<String, Any?>()
            for (url in urls){
                val props = buildProperties(url, type)
                // 合并, 但不覆盖先前的
                result.putAllIfAbsent(props)
            }
            return result
        }

        /**
         * 构建配置项
         *
         * @param url　配置文件
         * @param type properties | yaml | json
         * @return
         */
        public fun buildProperties(url: URL, type: String = "properties"): Map<String, *> {
            val `is` = url.openStream()
            // 无内容则返回空map
            if(`is`.available() == 0)
                return emptyMap<String, Any?>()

            // 解析内容
            val result = `is`.use {
                when(type){
                    "properties" -> Properties().apply { load(`is`.reader()) } // 加载 properties 文件
                    "yaml", "yml" -> Yaml().loadAs(`is`, HashMap::class.java) // 加载 yaml 文件
                    "json" -> JSONObject.parseObject(`is`.reader().readText()) // 加载 json 文件
                    else -> throw IllegalArgumentException("未知配置文件类型: " + type)
                }
            }
            if(result == null)
                return emptyMap<String, Any?>()

            return result as Map<String, *>
        }
    }

    /**
     * 例子：
     * <code>
     *      val config = Config("my_config");
     *      val config = Config("my_config", "properties");
     *      val config = Config("my_config", "yaml");
     *      val username = config.get("username");
     * <code>
     *
     * @param file the properties file's name in classpath or the sub directory of classpath
     * @param type properties | yaml
     * @param　merging
     */
    public constructor(file: String, type: String = "properties", merging: Boolean = false):this(buildProperties(file, type, merging), file, merging){
    }

    public override fun toString(): String {
        return "${this::class.simpleName}[$props]"
    }

    override fun equals(other: Any?): Boolean {
        return other is Config && props == other.props
    }

    override fun hashCode(): Int {
        return props.hashCode()
    }
}
