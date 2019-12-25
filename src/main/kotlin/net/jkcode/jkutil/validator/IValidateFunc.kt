package net.jkcode.jkutil.validator

/**
 * 校验函数
 *
 * @author shijianhang
 * @date 2016-10-19 下午3:40:55
 */
interface IValidateFunc {

    /**
     * 执行函数
     *    如果是预言函数 + 预言失败, 则抛 ValidateException 异常
     *
     * @param value 待校验的值
     * @param params 参数
     * @param variables 变量
     * @param label 值的标识, 如orm中的字段名, 如请求中的表单域名
     * @return 校验结果: 1. 如果是预言函数, value为原值, 否则value为执行结果 2. error为null则校验成功
     */
    fun execute(value: Any?, params: Array<String>, variables: Map<String, Any?>, label: String): ValidateResult
}