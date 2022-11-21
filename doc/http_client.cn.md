# http客户端

## 概述
对 [async-http-client](https://github.com/AsyncHttpClient/async-http-client/) 进行封装, 简化http请求处理；

async-http-client 内部实现了对每个server(`协议://ip:端口`)建立连接池以便复用连接, 加上异步请求, 性能很好

## 使用
```kotlin
val client = HttpClient()
// 异步请求, 支持http方法: get/head/post/put/delete/trace/options/patch
val future = client.get("http://t.weather.sojson.com/api/weather/city/101030100", null, ContentType.APPLICATION_JSON, emptyMap())
//val future = client.post("http://t.weather.sojson.com/api/weather/city/101030100", "a=1&b=2", ContentType.APPLICATION_JSON, emptyMap())

// 同步获得结果
val res = future.get()
println(res.responseBody)

// 异步获得结果
future.thenAccept { res ->
    println(res.responseBody)
}
```