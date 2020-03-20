package net.jkcode.jkutil.validator

// 校验lambda: 如果校验失败, 则抛 ValidateException 异常, 否则返回最终的值
public typealias ValidateLambda = (value: Any?, variables: Map<String, Any?>) -> ValueValidateResult;

/**
 * 校验器
 *   其校验方法是要被ValidationUnit调用的，通过反射来调用，反射时不能识别参数的默认值，因此在定义校验方法时不要设置参数默认值
 *
 * @author shijianhang
 * @date 2016-10-20 下午2:20:13  
 */
interface IValidator {

	/**
	 * 校验器
	 *
	 * @param value 要校验的数值，该值可能被修改
	 * @param variables 变量
	 * @return 校验结果: 1. 如果是预言函数, value为原值, 否则value为执行结果 2. error为null则校验成功
	 */
	@Throws(ValidateException::class)
	fun validate(value:Any?, variables:Map<String, Any?> = emptyMap()): ValueValidateResult

	/**
	 * 合并2个校验器
	 *
	 * @param other
	 * @return
	 */
	fun combile(other: IValidator?): IValidator{
		if(other == null)
			return this

		val me = this
		return object: IValidator{
			override fun validate(value: Any?, variables: Map<String, Any?>): ValueValidateResult {
				val result = me.validate(value, variables)
				if(result.error != null)
					return result

				return other.validate(result.value, variables)
			}

		}
	}

	/**
	 * 合并2个校验器
	 *
	 * @param other
	 * @return
	 */
	fun combile(other: ValidateLambda?): IValidator{
		if(other == null)
			return this

		val me = this
		return object: IValidator{
			override fun validate(value: Any?, variables: Map<String, Any?>): ValueValidateResult {
				val result = me.validate(value, variables)
				return other(result, variables)
			}

		}
	}

}