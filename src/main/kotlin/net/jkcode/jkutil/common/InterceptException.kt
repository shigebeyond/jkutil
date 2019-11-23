package net.jkcode.jkutil.common

import net.jkcode.jkutil.common.JkException

/**
 * 拦截器异常
 */
class InterceptException : JkException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}