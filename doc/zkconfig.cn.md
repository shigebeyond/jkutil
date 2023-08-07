# 基于zookeeper实现的远程配置

## 设计理念
1. 为了更好的融入k8s架构，远端配置文件的目录结构必须遵循k8s的层次结构，即两层：1 命名空间 2 应用

2. 应用加载远端配置，即是从zookeeper(远端)中获得(当前k8s命名空间+应用)目录下的配置文件

3. 结合 [jkcfg](https://github.com/shigebeyond/jkcfg) 在zookeeper上做配置管理，生成对应的目录结构

3. zookeeper上目录结构如下:
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
// 获得远程配置对象
// 只有一个参数：配置文件名
val cfg: ZkConfig = ZkConfig.instance("xxx.yaml")
// 获得单个配置项
val name: String? = config["name"]
```

## 结合 jkcfg 做配置管理
[jkcfg](https://github.com/shigebeyond/jkcfg), 使用非常简单的方式，来实现对远程zookeeper配置的管理(存储与分发)
1. `jkcfg diff` 对比差异文件
2. `jkcfg sync` 将差异文件同步到zookeeper上

详细用法请参考官网
