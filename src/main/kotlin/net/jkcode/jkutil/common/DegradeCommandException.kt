package net.jkcode.jkutil.common

/**
 * 带降级命令的异常
 *   主要用于封装异常降级处理的命令
 */
class DegradeCommandException(protected val cmd: ()->Any?) : RuntimeException() {

    /**
     * 处理降级后备
     * @return
     */
    public fun handleFallback(): Any? {
        return cmd.invoke()
    }
}