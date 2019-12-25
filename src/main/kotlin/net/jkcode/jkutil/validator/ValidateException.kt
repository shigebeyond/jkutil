package net.jkcode.jkutil.validator

import net.jkcode.jkutil.common.JkException

/**
 * 校验异常
 *   不需要`cause: Throwable?`, 因为 errors 已包含异常信息
 */
class ValidateException(
        message: String,
        public val name: String = "", // 对象名
        public val errors: Map<String, String> = emptyMap() // 对象字段的错误
) : JkException(message) {
}