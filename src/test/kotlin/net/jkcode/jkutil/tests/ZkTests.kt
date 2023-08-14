package net.jkcode.jkutil.tests

import org.junit.Test
import net.jkcode.jkutil.common.JkApp
import net.jkcode.jkutil.zkfile.IFileListener
import net.jkcode.jkutil.zkfile.ZkConfig
import net.jkcode.jkutil.zkfile.ZkConfigFiles
import net.jkcode.jkutil.zkfile.ZkFileSubscriber


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
     * 同步使用 jkcfg/zkCli.sh 来更新zk配置文件，来观察IFileListener的监听处理
     */
    @Test
    fun testZkSubscribe(){
        val appPath = "/jkcfg/zkCli.sh/${JkApp.namespace}/${JkApp.name}"
        ZkFileSubscriber.subscribe(appPath, l)

        Thread.sleep(1000000)
    }

    /**
     * 同步使用 jkcfg/zkCli.sh 来更新zk配置文件，来观察有哪些zk配置文件+内容
     */
    @Test
    fun testZkConfigFiles(){
        var i = 1
        while (true) {
            println("---- 第${i++}次 ----")
            val zkfiles = ZkConfigFiles.instance()
            for (f in zkfiles.files) {
                val props = zkfiles.getFileProps(f)
                println(">>> 配置文件 $f 内容:")
                println(props)
            }

            Thread.sleep(10000)
        }
    }

    /**
     * 同步使用 jkcfg/zkCli.sh 来更新zk配置文件，来观察单个配置文件内容变化
     */
    @Test
    fun testZkConfig(){
        //val redisConfig = ZkConfig("redis.yml")
        val redisConfig = ZkConfigFiles.instance().getZkConfig("redis.yml")
        val rpcserverConfig = ZkConfig("rpcserver.yml")
        var i = 1
        while (true) {
            println("---- 第${i++}次 ----")
            println("redis.yml的host配置为: " + redisConfig.getString("host"))
            println("rpcserver.yml的port配置为: " + rpcserverConfig.getString("port"))
            Thread.sleep(10000)
        }
    }

    /**
     * 同步使用 jkcfg/zkCli.sh 来更新zk配置文件，来观察单个配置文件内容变化
     */
    @Test
    fun testZkConfigListener(){
        val file = "redis.yml"
        val configFiles = ZkConfigFiles.instance()
        configFiles.addConfigListener(file){ data ->
            println("监听到配置[$file]变更: $data")
        }
        Thread.sleep(1000000)
    }

    /**
     * 同步使用 jkcfg/zkCli.sh 来更新zk配置文件，来观察单个配置文件内容变化
     */
    @Test
    fun testZkConfigListener2(){
        val file = "redis.yml"
        val config = object: ZkConfig(file){
            override fun handleConfigChange(data: Map<String, Any?>?) {
                println("监听到配置[$file]变更: $data")
                if(data == null){
                    println("redis.yml配置文件被删除")
                    return
                }
                //println("redis.yml的host配置为: " + data?.get("host"))
                println("redis.yml的host配置为: " + this.getString("host"))
            }
        }
        println("redis.yml的host配置为: " + config.getString("host"))
        Thread.sleep(1000000)
    }
}