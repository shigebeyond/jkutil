# 基于zookeeper实现的远程配置

## 设计理念
1. 数据层面：为了更好的融入k8s架构，远端配置文件的目录结构必须遵循k8s的层次结构，必包含两层：1 命名空间 2 应用；因此zookeeper上目录结构大致如下:
```
jkcfig
  default # k8s命名空间
    app1 # 应用
      redis.yaml # 配置文件
      log4j.properties
    app2 # 应用
      redis.yaml # 配置文件
      log4j.properties
```
注：仅支持 properties/yaml/yml/json 4种后缀的配置文件

2. 数据管理层面：结合 [jkcfg](https://github.com/shigebeyond/jkcfg) 在zookeeper上做配置管理，生成对应的目录结构

3. 应用端层面：就是本文`ZkConfig`的实现，用来实时加载远端配置，即是从zookeeper(远端)中获得(当前k8s命名空间+应用)目录下的配置文件

## 使用
1. 配置 zk.yaml
```yaml
default:
  # zk服务器地址，可填多个地址，以逗号为分割
  address : 172.16.0.229:2181
  #address : 192.168.0.17:2181
  sessionTimeout : 5000 # 会话超时
  connectionTimeout : 5000 # 连接超时
```

2. 使用远程配置
```kotlin
// 获得远程配置文件: 假定当前k8s命名空间为default, 应用名为rpcserver, 而配置文件名为redis.yml，则对应zookeeper上的配置文件路径为/jkcfg/default/rpcserver/redis.yml
val cfg: ZkConfig = ZkConfig("redis.yml") // 配置文件名为redis.yml
// 或
//val cfg: ZkConfig = ZkConfigFiles.instance().getZkConfig("redis.yml")
// 读配置文件中host配置项的值
val host: String? = config["host"]
```

3. 使用监听器监听配置变化，有2种监听方式:

3.1 使用`ZkConfigFiles.addConfigListener()`来添加监听器
```kotlin
val file = "redis.yml"
val configFiles = ZkConfigFiles.instance()
configFiles.addConfigListener(file){ data ->
    println("监听到配置[$file]变更: $data")
}
```

3.2 改写`ZkConfig.handleConfigChange()`来处理监听
```kotlin
val file = "redis.yml"
val config = object: ZkConfig(file){
    override fun handleConfigChange(data: Map<String, Any?>?) {
        println("监听到配置[$file]变更: $data")
        if(data == null){
            println("redis.yml配置文件被删除")
            return
        }
        println("redis.yml的host配置为: " + this.getString("host"))
    }
}
println("redis.yml的host配置为: " + config.getString("host"))
```

## 结合 jkcfg 做配置管理
[jkcfg](https://github.com/shigebeyond/jkcfg), 使用非常简单的方式，来实现对远程zookeeper配置的管理(存储与分发)
1. `jkcfg diff` 对比差异文件
2. `jkcfg sync` 将差异文件同步到zookeeper上

详细用法请参考官网
