package net.jkcode.jkutil.validator

import java.util.concurrent.ConcurrentHashMap

// 规则表达式的运算单位： 1 函数名 2 函数参数
private typealias SubRule = Pair<String, List<String>>

/**
 * 规则校验器, 由一个规则表达式, 来表达校验逻辑
 *
 * 1 格式
 *    规则表达式是由多个(函数调用的)规则子表达式组成, 规则子表达式之间以空格分隔, 格式为 a(1) b(1,"2") c(3,4)
 *    规则子表达式是函数调用, 格式为 a(1,"2")
 *
 * 2 限制
 *   无意于实现完整语义的布尔表达式, 暂时先满足于输入校验与orm保存数据时的校验, 因此:
 *   运算符没有优先级, 只能按顺序执行, 不支持带括号的规则子表达式
 *
 * @author shijianhang
 * @date 2016-10-19 下午3:40:55
 */
class RuleValidator(public val label: String /* 值的标识, 如orm中的字段名, 如请求中的表单域名 */,
					public val rule: String /* 规则表达式 */

) : IValidator {

	companion object{

		/**
		 * 缓存编译后的规则子表达式
		 */
		protected val compiledSubRules: ConcurrentHashMap<String, List<SubRule>> = ConcurrentHashMap();

		/**
		 * 编译规则表达式
		 *     规则表达式是由多个(函数调用的)规则子表达式组成, 规则子表达式之间以空格分隔, 格式为 a(1) b(1,2) c(3,4)
		 * <code>
		 *     val subRules = ValidationExpr::compileSubRules("trim notEmpty email");
		 * </code>
		 *
		 * @param rule
		 * @return
		 */
		public fun compileSubRules(rule:String): List<SubRule> {
			if(rule.isEmpty())
				return emptyList()

			// 规则子表达式之间以空格分隔, 格式为 a(1) b(1,2) c(3,4)
			val subRules = rule.split(" ")
			return subRules.map { subRule ->
				// 规则子表达式是函数调用, 格式为 a(1,2)
				val i = subRule.indexOf('(')
				if(i > -1){ // 包含()对
					val func = subRule.substring(0, i)
					val args = subRule.substring(i) // 包含()
					SubRule(func, ArgsParser.parse(args))
				}else{
					SubRule(subRule, emptyList())
				}
			}
		}
	}

	/**
	 * 规则子表达式的数组
	 *   一个规则子表达式 = listOf(函数名, 参数数组)
	 *   参数数组 = listOf("1", "2", ":name") 参数有值/变量（如:name）
	 */
	protected val subRules:List<SubRule> = compiledSubRules.getOrPut(rule){
		compileSubRules(rule)
	}

	/**
	 * 执行规则表达式
	 * <code>
	 * 	   // 编译
	 *     val rule = ValidationExpr("trim notEmpty email");
	 *     // 执行
	 *     val result = rule.validate(value);
	 * </code>
	 *
	 * @param value 要校验的数值，该值可能被修改
	 * @param variables 变量
	 * @return 校验结果: 1. 如果是预言函数, value为原值, 否则value为执行结果 2. error为null则校验成功
	 */
	public override fun validate(value:Any?, variables:Map<String, Any?>): ValidateResult {
		if(subRules.isEmpty())
			return ValidateResult(value, null, label)

		// 逐个运算规则子表达式
		var value2:Any? = value
		for ((func, args) in subRules) {
			val result = ValidateFunc.get(func).execute(value2, args, variables, label)
			if(result.error != null)
				return result

			value2 = result.value
		}
		return ValidateResult(value2, null, label)
	}

}
