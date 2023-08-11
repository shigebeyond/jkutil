# k8s命名空间与应用名
jksoa/jkmvc框架从设计层面就适应k8s架构，以便更好的融入k8s技术体系。

## k8s命名空间(Namespace)
k8s中的Namespace是一种用于在集群内部组织和隔离资源的机制。一个Namespace可以看作是一个虚拟的集群，它将物理集群划分为多个逻辑部分，每个部分都有自己的一组资源（如Pod、Service、ConfigMap等），用于对集群中的资源进行分类、筛选和管理。

## k8s命名空间结合应用的管理需求
可以给不同的用户、租户、环境或项目创建对应的命名空间，以实现以下的管理需求：
1. 应用隔离：将不同业务或不同团队的应用隔离开来，可独立部署与管理，避免命名冲突与资源竞争；
2. 环境隔离：为应用提供不同的环境，如开发环境dev、测试环境test、生产环境pro；
3. 安全隔离：控制资源配额，限制cpu、内存等资源的使用；控制访问权限，确保应用之间的安全隔离。

## 在k8s集群中引入应用概念
结合 [K8sBoot](https://github.com/shigebeyond/K8sBoot) 框架来实现，它对k8s资源定义做了简化，并抽取了`应用`的概念
```yaml
- ns: dev # 定义命名空间
- app(xxx): # 定义应用，应用名为xxx
    - containers:
        xxx: # 定义多个容器, dict形式, 键是容器名, 值是容器配置
          image: nginx # 镜像
          env: # 以dict方式设置环境变量
            APP_NAME: $app # 引用app名
            POD_NAMESPACE: ${ref_pod_field(metadata.namespace)} # 引用k8s命名空间
    - deploy: 1
```

## 远程(zookeeper)配置在k8s架构中的实现
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

2. 数据管理层面：结合 [jkcfg](https://github.com/shigebeyond/jkcfg) 在zookeeper上做配置管理，生成对应的目录结构

3. 应用端层面：有以下实现，用于在应用中实时加载zookeeper上的配置文件:

3.1 java/kotlin实现: [jkutil库的ZkConfig类](https://github.com/shigebeyond/jkutil/blob/master/doc/zkconfig.cn.md)

3.2 python实现: [pyutilb库的zkconfigfiles类](https://github.com/shigebeyond/pyutilb)


## 应用中如何获得命名空间与应用名?

```
JkApp.namespace // 命名空间
JkApp.name // 应用名
```

## 应用中命名空间与应用名从哪里来?
根据部署方式的不同，有不同的取值来源

1. 本地部署: 命名空间与应用名读jkapp.yaml配置文件
jkapp.yaml
```yaml
# k8s命名空间, 可能包含环境(pro/dev/test)+版本, 仅本地部署时有效, 当k8s部署并设置了环境变量POD_NAMESPACE, 则此配置项无效
namespace: dev
# k8s应用名, 仅本地部署时有效, 当k8s部署并设置了环境变量APP_NAME, 则此配置项无效
name: jkapp
# ......
```

2. k8s集群部署: 命名空间与应用名优先读环境变量
参考 [K8sBoot](https://github.com/shigebeyond/K8sBoot) 框架的k8s资源定义
```yaml
- ns: dev # 定义命名空间
- app(xxx): # 定义应用，应用名为xxx
    - containers:
        xxx: # 定义多个容器, dict形式, 键是容器名, 值是容器配置
          image: nginx # 镜像
          env: # 以dict方式设置环境变量
            APP_NAME: $app # 引用app名
            POD_NAMESPACE: ${ref_pod_field(metadata.namespace)} # 引用k8s命名空间
    - deploy: 1
```