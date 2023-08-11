package net.jkcode.jkutil.common

import net.jkcode.jkutil.ttl.SttlThreadPool
import java.util.concurrent.atomic.AtomicInteger

/**
 * 应用信息
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-17 12:12 PM
 */
object JkApp {

    /**
     * 应用配置
     */
    public val config = Config.instance("jkapp", "yaml")

    /**
     * k8s命名空间, 可能包含环境(pro/dev/test)+版本: 优先读环境变量POD_NAMESPACE, 如果环境变量不存在再读配置项
     */
    public val namespace: String
        get() {
            return System.getenv("POD_NAMESPACE") ?: config["namespace"]!!
        }

    /**
     * k8s应用名: 优先读环境变量APP_NAME, 如果环境变量不存在再读配置项
     */
    public val name: String
        get(){
            return System.getenv("APP_NAME") ?: config["name"]!!
        }

    /**
     * 是否应用协程, 与useSttl不兼容, 如果useFiber=true, 则会强制使得 useSttl=false
     */
    public val useFiber: Boolean = config["useFiber"]!!

    /**
     * 是否应用可传递ScopedTransferableThreadLocal, 影响
     * 1. CompletableFuture 的线程池
     * 2. 公共线程池
     * 3. IRequestScope#sttlWrap()
     */
    public val useSttl: Boolean = !useFiber && config["useSttl"]!!

    init{
        // 将 SttlThreadPool 应用到 CompletableFuture.asyncPool
        if(useSttl)
            SttlThreadPool.applyCommonPoolToCompletableFuture()
    }

    /**
     * 是否测试环境
     */
    public val isTest: Boolean = "test" in namespace

    /**
     * 是否开始环境
     */
    public val isDev: Boolean = "dev" in namespace


    /**
     * 是否线上环境
     */
    public val isPro: Boolean = "pro" in namespace

    /**
     * 是否debug环境
     */
    public val isDebug: Boolean = System.getProperty("java.class.path").contains("debugger-agent.jar")

    /**
     * 是否是windows系统
     */
    public val isWin: Boolean = System.getProperty("os.name").startsWith("Windows", true)

    /**
     * 是否单元测试环境
     */
    public val isJunitTest: Boolean = System.getProperty("sun.java.command").contains("-junit")

    /**
     * 是否gradle的gretty插件运行环境
     */
    public val isGretty: Boolean = System.getProperty("sun.java.command").startsWith("org.akhikhl.gretty.Runner")

    /**
     * 机器的配置
     */
    private val workerConfig = Config.instance("snow-flake-id", "properties")

    /**
     * 数据中心id
     */
    public val datacenterId: Int = workerConfig["datacenterId"]!!

    /**
     * 机器id
     */
    public val workerId: Int = workerConfig["workerId"]!!

    /**
     * 完整的机器id
     */
    public val fullWorkerId: String = "$datacenterId.$workerId"

    /**
     * 线程id计数
     */
    private val threadCount: AtomicInteger = AtomicInteger(0)

    /**
     * 线程id池
     */
    private val threadIds:ThreadLocal<Int> = ThreadLocal.withInitial {
        threadCount.getAndIncrement()
    }

    /**
     * 当前线程id
     */
    public val threadId: Int
        get() = threadIds.get()

    /**
     * 完整的线程id
     */
    public val fullThreadId: String
        get() = "$datacenterId.$workerId.$threadId"
}