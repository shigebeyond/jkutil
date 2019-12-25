package net.jkcode.jkutil.validator

import net.jkcode.jkutil.common.JkException

/**
 * 校验异常
 */
class ValidateException(message: String, cause: Throwable? = null, public val errors: Map<String, String> = emptyMap()) : JkException(message, cause) {

    public constructor(cause: Throwable, errors: Map<String, String> = emptyMap()) : this(cause.toString(), cause, errors) {
    }
}