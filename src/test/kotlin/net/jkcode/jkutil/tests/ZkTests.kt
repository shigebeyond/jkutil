package net.jkcode.jkutil.tests

import com.thoughtworks.xstream.XStream
import net.jkcode.jkutil.common.lcFirst
import org.junit.Test
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*
import com.thoughtworks.xstream.converters.SingleValueConverter
import com.thoughtworks.xstream.converters.Converter
import com.thoughtworks.xstream.converters.MarshallingContext
import com.thoughtworks.xstream.converters.UnmarshallingContext
import com.thoughtworks.xstream.io.HierarchicalStreamReader
import com.thoughtworks.xstream.io.HierarchicalStreamWriter
import net.jkcode.jkutil.common.JkApp
import net.jkcode.jkutil.xml.StringMapConverter
import net.jkcode.jkutil.zkfile.IFileListener
import net.jkcode.jkutil.zkfile.ZkConfig
import net.jkcode.jkutil.zkfile.ZkConfigFiles
import net.jkcode.jkutil.zkfile.ZkFileSubscriber
import org.simpleframework.xml.core.Persister


/**
 * zk测试
 */
class ZkTests {

    val l = object: IFileListener{
        override fun handleFileAdd(path: String, content: String) {
            println("监听到新增文件[$path], 内容为: $content")
        }

        override fun handleFileRemove(path: String) {
            println("监听到删除文件[$path]")
        }

        override fun handleContentChange(path: String, content: String) {
            println("监听到修改文件[$path], 内容为: $content")
        }

    }

    /**
     * 同步使用 jkcfg 来更新zk配置文件，来观察IFileListener的监听处理
     */
    @Test
    fun testZkSubscribe(){
        val appPath = "/jkcfg/${JkApp.namespace}/${JkApp.name}"
        ZkFileSubscriber.subscribe(appPath, l)

        Thread.sleep(1000000)
    }

    /**
     * 同步使用 jkcfg 来更新zk配置文件，来观察有哪些zk配置文件+内容
     */
    @Test
    fun testZkConfigFiles(){
        var i = 1
        while (true) {
            println("---- 第${i++}次 ----")
            for (f in ZkConfigFiles.files) {
                val props = ZkConfigFiles.getFileProps(f)
                println(">>> 配置文件 $f 内容:")
                println(props)
            }

            Thread.sleep(10000)
        }
    }

    /**
     * 同步使用 jkcfg 来更新zk配置文件，来观察单个配置文件内容变化
     */
    @Test
    fun testZkConfig(){
        var i = 1
        while (true) {
            println("---- 第${i++}次 ----")
            println("redis.yml的host配置为: " + ZkConfig.instance("redis.yml").getString("host"))
            println("rpcserver.yml的port配置为: " + ZkConfig.instance("rpcserver.yml").getString("port"))
            Thread.sleep(10000)
        }
    }
}