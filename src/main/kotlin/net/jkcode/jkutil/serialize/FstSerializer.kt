package net.jkcode.jkutil.serialize

import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jkutil.common.isSubClass
import org.nustaq.serialization.FSTConfiguration
import org.nustaq.serialization.FSTObjectSerializer
import java.io.InputStream

/**
 * 基于fast-serialization的序列化
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-11-10 4:18 PM
 */
class FstSerializer: ISerializer {

    /**
     * fst内部配置
     * 线程安全 + 不共享(不检查循环引用)
     */
    protected val confs: ThreadLocal<FSTConfiguration> = ThreadLocal.withInitial {
        val conf = FSTConfiguration.createDefaultConfiguration()
        // share模型, 性能最高
        conf.isShareReferences = false

        try{
            // 1 自定义的序列器
            val reg = conf.getCLInfoRegistry().getSerializerRegistry()
            val config: IConfig = Config.instance("fst-serializer", "yaml", true)
            // key是被序列化的类名, value是序列器的类名
            for((key, value) in config.props){
                if(value == null)
                    throw RuntimeException("配置文件 fst-serializer.yaml 的配置项[$key]的值为空");
                // 被序列化的类名
                val targetClass = Class.forName(key)
                // 序列器的类名
                val serializerClass = Class.forName(value as String)
                if(!serializerClass.isSubClass(FSTObjectSerializer::class.java))
                    throw RuntimeException("配置文件 fst-serializer.yaml 中的值[$value]对应的类型没有实现 FSTObjectSerializer");
                val serializer = serializerClass.newInstance() as FSTObjectSerializer
                // 给特定类指定序列器
                reg.putSerializer(targetClass, serializer, true)
            }
        }catch (e: ClassNotFoundException){
            throw RuntimeException("配置文件 fst-serializer.yaml 错误: ${e.message}", e);
        }

        try{
            // 2 注册类
            val config: IConfig = Config.instance("fst-class", "yaml", true)
            // key是类名, value是空
            // 类名必须排序, 这样保证rpc client/server两端的类名注册顺序一样, 这样保证类名依次映射的简写code也一样
            val names = config.props.keys.sorted()
            for(name in names) {
                val clazz = Class.forName(name)
                conf.registerClass(clazz)
            }
        }catch (e: ClassNotFoundException){
            throw RuntimeException("配置文件 fst-class.yaml 错误: ${e.message}", e);
        }

        conf
    }

    /**
     * 获得fst当前配置
     */
    public val conf: FSTConfiguration
        get() = confs.get()


    /**
     * 注册类名, 相当于缩写, 加快序列化
     * @param c
     */
    public fun registerClass(vararg c: Class<*>) {
        confs.get().registerClass(*c)
    }

    /**
     * 序列化
     *
     * @param obj
     * @return
     */
    public override fun serialize(obj: Any): ByteArray? {
        return confs.get().asByteArray(obj)
    }

    /**
     * 反序列化
     *
     * @param bytes
     * @return
     */
    public override fun unserialize(bytes: ByteArray): Any? {
        // 两个语句是一样的
        //return confs.get().asObject(bytes)
        return confs.get().getObjectInput(bytes).readObject()
    }

    /**
     * 反序列化
     *
     * @param input
     * @return
     */
    public override fun unserialize(input: InputStream): Any? {
        return confs.get().getObjectInput(input).readObject()
    }

}