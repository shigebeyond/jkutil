package net.jkcode.jkutil.tests

import net.jkcode.jkutil.http.ContentType
import net.jkcode.jkutil.http.HttpClient
import org.junit.Test

class HttpClientTests {

    val client = HttpClient()

    @Test
    fun testHttpRequest(){
        for(i in 1..3) {
            println("第${i}次请求")
            httpRequest()
        }
        Thread.sleep(100000)
    }

    private fun httpRequest() {
        val future = client.get("http://platinum.shikee.com/data/34254381", null, ContentType.APPLICATION_JSON, emptyMap())
//        val res = future.get() // 阻塞同步
//        println(res.responseBody)
        future.thenAccept { res ->// 异步
            println(res.responseBody)
        }

    }
}