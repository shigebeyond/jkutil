package net.jkcode.jkutil.common

import net.jkcode.jkutil.ttl.SttlThreadPool
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

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
     * 应用名
     */
    public val name: String = config["name"]!!

    /**
     * 环境
     */
    public val env: String = config["env"]!!

    /**
     * 是否应用可传递ScopedTransferableThreadLocal, 影响
     * 1. CompletableFuture 的线程池
     * 2. 公共线程池
     * 3. IRequestScope#sttlWrap()
     */
    public val useSttl: Boolean = config["useSttl"]!!

    init{
        // 将 SttlThreadPool 应用到 CompletableFuture.asyncPool
        if(useSttl)
            SttlThreadPool.applyCommonPoolToCompletableFuture()
    }

    /**
     * 是否测试环境
     */
    public val isTest: Boolean = env == "test"

    /**
     * 是否开始环境
     */
    public val isDev: Boolean = env == "dev"


    /**
     * 是否线上环境
     */
    public val isPro: Boolean = env == "pro"

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