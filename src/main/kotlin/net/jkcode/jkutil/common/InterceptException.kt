package net.jkcode.jkutil.common

import net.jkcode.jkutil.common.JkException

/**
 * 拦截器异常
 */
class InterceptException : JkException {

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable? = null) : super(message, cause) {
    }
}