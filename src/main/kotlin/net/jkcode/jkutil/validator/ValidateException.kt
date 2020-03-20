package net.jkcode.jkutil.validator

import net.jkcode.jkutil.common.JkException

/**
 * 校验异常
 *   不需要`cause: Throwable?`, 因为 errors 已包含异常信息
 */
class ValidateException(val result: ValidateResult<*>) : JkException() {

    /**
     * 消息
     */
    public override val message: String?
        get(){
            val error = result.error
            return if(error is Map<*, *>){ // 多个错误
                        error.values.joinToString("\n")
                    }else{ // 单个错误
                        error.toString()
                    }
        }
}