package net.jkcode.jkutil.http

import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.ssl.SslContextBuilder
import net.jkcode.jkutil.common.getAccessibleMethod
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClientConfig.Builder
import org.asynchttpclient.Response
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.HashMap
import io.netty.handler.ssl.util.InsecureTrustManagerFactory.INSTANCE as InsecureTrustManager

/**
 * 使用 asynchttpclient 实现用http通讯的rpc客户端
 *    内部使用netty, 会对每个server建立连接池以便复用连接, 加上异步请求, 性能很好
 *    如果你要对某个域名指定header或cookie, 则要对该域名要创建单独的 HttpClient 实例
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-10-19 12:48 PM
 */
class HttpClient {
    companion object{

        /**
         * DefaultAsyncHttpClient::requestBuilder()方法
         */
        protected val requestBuilderMethod = DefaultAsyncHttpClient::class.java.getAccessibleMethod("requestBuilder", String::class.java, String::class.java)

        /**
         * 安全的http client
         */
        protected val secureClient: DefaultAsyncHttpClient by lazy {
            buildClient(true)
        }

        /**
         * 不安全的http client
         */
        protected val insecureClient: DefaultAsyncHttpClient by lazy {
            buildClient(false)
        }

        /**
         * 构建http client
         */
        protected fun buildClient(insecure: Boolean): DefaultAsyncHttpClient {
            val sslContext = if (insecure)
                SslContextBuilder.forClient().trustManager(InsecureTrustManager).build()
            else
                SslContextBuilder.forClient().build()
            return DefaultAsyncHttpClient(Builder()
                .setConnectTimeout(5000) // 连接超时
                .setSslContext(sslContext)
                .build()
            )
        }
    }

    /**
     * 请求头
     */
    protected val headers: MutableMap<String, String?> = HashMap()

    /**
     * 记录cookie
     */
    protected val cookies: MutableMap<String, Cookie> = HashMap()

    /**
     * 添加请求头
     */
    public fun addHeader(name: String, value: String): HttpClient {
        headers[name] = value
        return this
    }

    /**
     * 添加请求头
     */
    public fun addHeaders(heads: Map<String, String?>): HttpClient {
        this.headers.putAll(heads)
        return this
    }

    /**
     * 添加 Authorization Basic 认证的请求头
     * @param user 认证的用户名
     * @param password 认证的密码
     * @return
     */
    public fun authBasic(user: String, password: String): HttpClient {
        val auth = Base64.getEncoder().encodeToString("$user:$password".toByteArray(UTF_8)) // base编码用户名密码
        addHeader("Authorization", "Basic $auth")
        return this
    }

    /**
     * 添加cookie
     */
    public fun addCookie(cookie: Cookie): HttpClient {
        cookies[cookie.name()] = cookie
        return this
    }

    /**
     * 添加cookie
     */
    public fun addCookies(cookies: Array<Cookie>): HttpClient {
        for (cookie in cookies)
            addCookie(cookie)
        return this
    }

    /**
     * 发送请求
     *
     * @param method
     * @param url
     * @param body
     * @param contentType
     * @param requestTimeout 请求超时
     * @return
     */
    public fun send(method: String, url: String, body: Any? = null, contentType: ContentType = ContentType.APPLICATION_FORM_URLENCODED, headers: Map<String, String> = emptyMap(), requestTimeout: Int = 5000): CompletableFuture<Response> {
        // 1 获得client
        val secure = url.startsWith("https://")
        val client = if(secure) secureClient else insecureClient

        // 2 准备http请求
        //val req = client.preparePost(url)
        val req = requestBuilderMethod.invoke(client, method, url) as BoundRequestBuilder
        req.setCharset(Charset.defaultCharset())

        // 3 设置header
        if(contentType != null)
            req.addHeader("Content-Type", contentType)

        this.headers.forEach { // 全局的header
            req.addHeader(it.key, it.value)
        }
        headers.forEach { // 当前请求的header
            req.addHeader(it.key, it.value)
        }
        // 4 设置cookie
        cookies.forEach {
            req.addCookie(it.value)
        }

        // 5 设置body
        contentType.setRequestBody(req, body)

        // 6 设置请求超时
        req.setRequestTimeout(requestTimeout)

        // 7 发送请求, 并返回异步响应
        return req.execute()
                .toCompletableFuture()
                .thenApply { response: Response ->
                    // 8 写cookie
                    response.cookies.forEach {
                        if (it.value() == "")
                            cookies.remove(it.name())
                        else
                            cookies[it.name()] = it
                    }

                    response
                }
    }

    /**
     * 发送get请求
     *
     * @param url
     * @param body
     * @param contentType
     * @param requestTimeout 请求超时
     * @return
     */
    public fun get(url: String, body: Any? = null, contentType: ContentType = ContentType.APPLICATION_FORM_URLENCODED, headers: Map<String, String> = emptyMap(), requestTimeout: Int = 5000): CompletableFuture<Response> {
        return send("GET", url, body, contentType, headers, requestTimeout)
    }

    /**
     * 发送get请求
     *
     * @param url
     * @param body
     * @param contentType
     * @param requestTimeout 请求超时
     * @return
     */
    public fun head(url: String, body: Any? = null, contentType: ContentType = ContentType.APPLICATION_FORM_URLENCODED, headers: Map<String, String> = emptyMap(), requestTimeout: Int = 5000): CompletableFuture<Response> {
        return send("HEAD", url, body, contentType, headers, requestTimeout)
    }
    /**
     * 发送get请求
     *
     * @param url
     * @param body
     * @param contentType
     * @param requestTimeout 请求超时
     * @return
     */
    public fun post(url: String, body: Any? = null, contentType: ContentType = ContentType.APPLICATION_FORM_URLENCODED, headers: Map<String, String> = emptyMap(), requestTimeout: Int = 5000): CompletableFuture<Response> {
        return send("POST", url, body, contentType, headers, requestTimeout)
    }
    /**
     * 发送get请求
     *
     * @param url
     * @param body
     * @param contentType
     * @param requestTimeout 请求超时
     * @return
     */
    public fun put(url: String, body: Any? = null, contentType: ContentType = ContentType.APPLICATION_FORM_URLENCODED, headers: Map<String, String> = emptyMap(), requestTimeout: Int = 5000): CompletableFuture<Response> {
        return send("PUT", url, body, contentType, headers, requestTimeout)
    }
    /**
     * 发送get请求
     *
     * @param url
     * @param body
     * @param contentType
     * @param requestTimeout 请求超时
     * @return
     */
    public fun delete(url: String, body: Any? = null, contentType: ContentType = ContentType.APPLICATION_FORM_URLENCODED, headers: Map<String, String> = emptyMap(), requestTimeout: Int = 5000): CompletableFuture<Response> {
        return send("DELETE", url, body, contentType, headers, requestTimeout)
    }
    /**
     * 发送get请求
     *
     * @param url
     * @param body
     * @param contentType
     * @param requestTimeout 请求超时
     * @return
     */
    public fun trace(url: String, body: Any? = null, contentType: ContentType = ContentType.APPLICATION_FORM_URLENCODED, headers: Map<String, String> = emptyMap(), requestTimeout: Int = 5000): CompletableFuture<Response> {
        return send("TRACE", url, body, contentType, headers, requestTimeout)
    }
    /**
     * 发送get请求
     *
     * @param url
     * @param body
     * @param contentType
     * @param requestTimeout 请求超时
     * @return
     */
    public fun options(url: String, body: Any? = null, contentType: ContentType = ContentType.APPLICATION_FORM_URLENCODED, headers: Map<String, String> = emptyMap(), requestTimeout: Int = 5000): CompletableFuture<Response> {
        return send("OPTIONS", url, body, contentType, headers, requestTimeout)
    }
    /**
     * 发送get请求
     *
     * @param url
     * @param body
     * @param contentType
     * @param requestTimeout 请求超时
     * @return
     */
    public fun patch(url: String, body: Any? = null, contentType: ContentType = ContentType.APPLICATION_FORM_URLENCODED, headers: Map<String, String> = emptyMap(), requestTimeout: Int = 5000): CompletableFuture<Response> {
        return send("PATCH", url, body, contentType, headers, requestTimeout)
    }
}

