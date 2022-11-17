package net.jkcode.jkutil.tests

import net.jkcode.jkutil.http.ContentType
import net.jkcode.jkutil.http.HttpClient
import org.junit.Test

class HttpClientTests {

    @Test
    fun http(){
        val client = HttpClient()
        println("第一次请求")
        val res1 = client.get("http://platinum.shikee.com/data/34254381", null, ContentType.APPLICATION_JSON, emptyMap()).get() // 阻塞同步
        println(res1.responseBody)
        println("第二次请求")
        val res2 = client.get("http://platinum.shikee.com/data/34254381", null, ContentType.APPLICATION_JSON, emptyMap()).get() // 阻塞同步
        println(res2.responseBody)
    }
}