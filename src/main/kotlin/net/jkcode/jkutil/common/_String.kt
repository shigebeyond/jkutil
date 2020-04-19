package net.jkcode.jkutil.common

import java.io.PrintWriter
import java.io.StringWriter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * 合并哈希码
 * @param args
 * @return
 */
public fun joinHashCode(vararg args: String): Int {
    var hash = 0
    for (arg in args)
        hash = hash * Math.pow(31.0, arg.length.toDouble()).toInt() + arg.hashCode()
    return hash
}

/**
 * 缓存字符串的64位哈希码
 */
private val longHashCodes = ConcurrentHashMap<String, Long>()

/**
 * 获得字符串64位哈希码
 * @return
 */
public fun String.longHashCode(): Long {
    return longHashCodes.getOrPut(this) {
        var h = 0L
        for (ch in this) {
            h = 31 * h + ch.toLong()
        }
        h
    }
}

/****************************** 字符串扩展 *******************************/
/**
 * 根据Unicode编码完美的判断中文汉字和符号
 */
public fun Char.isChinese(): Boolean {
    // 根据字节码判断
    // return this >= 0x4E00 &&  this <= 0x9FA5;
    // 根据UnicodeBlock来判断
    val ub = Character.UnicodeBlock.of(this)
    return ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
            || ub === Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            || ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
            || ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
            || ub === Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            || ub === Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
            || ub === Character.UnicodeBlock.GENERAL_PUNCTUATION
}

/**
 * Unicode编码
 */
public fun String.unicodeEncode(): String {
    val builder = StringBuilder()
    for (c in this) {
        val code = c.toInt()
        if (code >= 0 && code <= 127) {
            builder.append(c)
        } else {
            builder.append("\\u")
            val hx = Integer.toString(code, 16)
            if (hx.length < 4) {
                builder.append("0000".substring(hx.length)).append(hx)
            } else {
                builder.append(hx)
            }
        }

    }
    return builder.toString()
}

/**
 * Unicode解码
 */
public fun String.unicodeDecode(): String {
    var start = 0
    var end = 0
    val buffer = StringBuffer()
    while (start > -1) {
        end = indexOf("\\u", start + 2)
        var charStr = ""
        if (end == -1) {
            charStr = substring(start + 2, length)
        } else {
            charStr = substring(start + 2, end)
        }
        val letter = Integer.parseInt(charStr, 16).toChar() // 16进制parse整形字符串。
        buffer.append(letter)
        start = end
    }
    return buffer.toString()
}

/**
 * Returns a substring after the first occurrence of [delimiter], including [delimiter]
 * If the string does not contain the delimiter, returns [missingDelimiterValue] which defaults to the original string.
 */
public fun String.substringAfterInclude(delimiter: String, missingDelimiterValue: String = this): String {
    val index = indexOf(delimiter)
    return if (index == -1) missingDelimiterValue else substring(index, length)
}

/**
 * Returns a substring after the first occurrence of [delimiter], including [delimiter]
 * If the string does not contain the delimiter, returns [missingDelimiterValue] which defaults to the original string.
 */
public fun String.substringAfterLastInclude(delimiter: String, missingDelimiterValue: String = this): String {
    val index = lastIndexOf(delimiter)
    return if (index == -1) missingDelimiterValue else substring(index, length)
}

/**
 * 截取开始字符与结束字符之间的子字符串
 * @param start
 * @param end
 * @return
 */
public inline fun String.substringBetween(start: String, end: String): String {
    val startIndex = indexOf(start) + start.length
    val endIndex = indexOf(end)
    return substring(startIndex, endIndex)
}

/**
 * 截取开始字符与结束字符之间的子字符串
 * @param start
 * @param end
 * @return
 */
public inline fun String.substringBetween(start: Char, end: Char): String {
    val startIndex = indexOf(start) + 1
    val endIndex = indexOf(end)
    return substring(startIndex, endIndex)
}

/**
 * StringBuilder扩展
 * 清空
 *
 * @return
 */
public inline fun StringBuilder.clear(): StringBuilder {
    return this.delete(0, this.length - 1)
}

/**
 * StringBuilder扩展
 *  删除最后的一段子字符串
 * @param str 要删除的子字符串
 * @return
 */
public inline fun StringBuilder.deleteSuffix(str: String): StringBuilder {
    if (this.endsWith(str)) {
        val start = length - str.length;
        delete(start, length);
    }
    return this;
}

/**
 * 首字母大写
 * @return
 */
public inline fun String.ucFirst(): String {
    val cs = this.toCharArray()
    if (cs[0] in 'a'..'z')
        cs[0] = cs[0] - 32
    return String(cs)
}

/**
 * 首字母小写
 * @return
 */
public inline fun String.lcFirst(): String {
    val cs = this.toCharArray()
    if (cs[0] in 'A'..'Z')
        cs[0] = cs[0] + 32
    return String(cs)
}

/**
 * 去掉两头的字符
 *
 * @param str 要去掉的字符串
 * @param ignoreCase 是否忽略大小写
 * @return
 */
public inline fun String.trim(str: String, ignoreCase: Boolean = false): String {
    return trim(str, str, ignoreCase)
}

/**
 * 去掉两头的字符
 * @param preffix 头部要去掉的子字符串
 * @param suffix 尾部要去掉的子字符串
 * @param ignoreCase 忽略大小写
 * @return
 */
public inline fun String.trim(preffix: String, suffix: String = "", ignoreCase: Boolean = false): String {
    if (preffix.isEmpty() && suffix.isEmpty())
        return this

    var start = if (preffix.isNotEmpty() && this.startsWith(preffix, ignoreCase))
        preffix.length
    else
        0
    var end = if (suffix.isNotEmpty() && this.endsWith(suffix, ignoreCase))
        length - suffix.length;
    else
        length

    if (start == 0 && end == length)
        return this;

    if (start >= end)
        return "";

    return this.substring(start, end);
}

/**
 * 替换字符串
 *   参数名是int
 *
 * @param params 参数值
 * @param prefix 参数名前缀正则
 * @param postfix 参数名后缀正则
 * @return
 */
public inline fun String.replaces(params: Array<String>, prefix: CharSequence = ":", postfix: CharSequence = ""): String {
    return this.replace("$prefix(\\d+)$postfix".toRegex()) { matches: MatchResult ->
        val i = matches.groupValues[1]
        val value = params.get(i.toInt());
        value
    };
}

/**
 * 替换字符串
 *   参数名是int
 *
 * @param params 参数值
 * @param prefix 参数名前缀正则
 * @param postfix 参数名后缀正则
 * @return
 */
public inline fun String.replaces(params: List<String>, prefix: CharSequence = ":", postfix: CharSequence = ""): String {
    return this.replace("$prefix(\\d+)$postfix".toRegex()) { matches: MatchResult ->
        val i = matches.groupValues[1]
        val value = params.get(i.toInt());
        value
    };
}

/**
 * 替换字符串
 *   参数名是字符串
 *
 * @param params 参数值
 * @param prefix 参数名前缀正则
 * @param postfix 参数名后缀正则
 * @return
 */
public inline fun String.replaces(params: Map<String, *>, prefix: CharSequence = ":", postfix: CharSequence = ""): String {
    return this.replace("$prefix([\\w\\d\\.@_]+)$postfix".toRegex()) { matches: MatchResult ->
        val key = matches.groupValues[1]
        val value = params.get(key);
        if (value == null)
            ""
        else
            value.toString()
    };
}

/**
 * 替换字符串
 *   参数名是字符串, 但是自带格式, 如 <default:DEFAULT '?'>
 *   如果参数值为null, 才不会格式化输出
 *
 * @param params 参数值
 * @param prefix 参数名前缀正则
 * @param postfix 参数名后缀正则
 * @return
 */
public inline fun String.replacesFormat(params: Map<String, Any?>, prefix: CharSequence = "<", postfix: CharSequence = ">"): String {
    // .+ 贪婪 .+? 非贪婪
    return this.replace("$prefix(.+?)$postfix".toRegex()) { matches: MatchResult ->
        val exp = matches.groupValues[1].split(':', limit = 2)
        val key = exp[0]
        val format = if (exp.size > 1) exp[1] else null // 格式
        val value = params.get(key);
        if (value == null)
            ""
        else if (format != null) // 格式化输出
            format.replace("?", value.toString())
        else
            value.toString()
    };
}

/**
 * 下划线转驼峰
 * @return
 */
public fun String.underline2Camel(): String {
    return "_\\w".toRegex().replace(this) { result: MatchResult ->
        // 获得下划线后的字母
        val ch = result.value[1]
        // 转为大写
        Character.toUpperCase(ch).toString()
    }
}

/**
 * 驼峰转下划线
 * @return
 */
public fun String.camel2Underline(): String {
    return "[A-Z]".toRegex().replace(this) { result: MatchResult ->
        // 获得大写字母
        val ch = result.value
        // 转为下划线+小写
        "_" + ch.toLowerCase()
    }
}

/****************************** 字符串转化其他类型 *******************************/
/**
 * 日期格式
 */
val dateFormat = SimpleDateFormat("yyyy-MM-dd")
/**
 * 日期-时间格式
 */
val datetimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

/**
 * 转换为日期类型
 * @param isFullTime 充满时间
 * @return
 */
public fun String.toDate(isFullTime: Boolean = false): Date {
    val hasTime = contains(':') // 有时分秒, 否则为纯日期
    val format: SimpleDateFormat = if (hasTime) datetimeFormat else dateFormat;
    val date = format.parse(this)
    // 纯日期 + 充满时间
    if (!hasTime && isFullTime)
        return date.add(Calendar.SECOND, 86400 - 1)

    return date
}

/**
 * 格式化 double 值
 *
 * @param precision 精度
 * @return
 */
public fun Double.format(precision: Int = 2): String {
    val format = NumberFormat.getNumberInstance()
    format.maximumFractionDigits = precision
    return format.format(this)
}

/**
 * 将字符串转换为指定类型的可空值
 * @param class 要转换的类型
 * @param defaultValue 默认值
 * @return
 */
public inline fun <T : Any> String?.toNullable(clazz: KClass<T>, defaultValue: T? = null): T? {
    // 默认值
    if (this == null)
        return defaultValue

    // 转换
    return to(clazz)
}

/**
 * 将字符串转换为指定类型的非空值
 * @param class 要转换的类型
 * @return
 */
public inline fun <T : Any> String.to(clazz: KClass<T>): T {
    // 1 如果要转换为 String类及其父类，则直接返回，不用转换
    if (clazz.java.isAssignableFrom(String::class.java))
        return this as T

    // 2 转换为其他类型
    return when (clazz) {
        Int::class -> this.toInt()
        Long::class -> this.toLong()
        Float::class -> this.toFloat()
        Double::class -> this.toDouble()
        Boolean::class -> this.toBoolean()
        Short::class -> this.toShort()
        Byte::class -> this.toByte()
        Date::class -> this.toDate()
        else -> throw IllegalArgumentException("字符串不能自动转换为未识别的类型: " + clazz)
    } as T;
}

/**
 * 将字符串转换为指定类型的非空值
 * @param class 要转换的类型
 * @return
 */
public inline fun <T : Any> String.to(clazz: Class<T>): T {
    return this.to(clazz.kotlin)
}

/**
 * 将字符串转换为指定类型的非空值
 * @param type
 * @return
 */
public inline fun String.to(type: KType): Any {
    return this.to(type.classifier as KClass<*>)
}

/**
 * 将任意值转为表达式
 * @return
 */
public inline fun Any?.toExpr(): String {
    if (this == null)
        return "null"

    return when (this) {
        is Long -> "${this}L"
        is Float -> "${this}F"
        is Date -> "Date(\"$this\")"
        is String -> "\"$this\""
        else -> this.toString()
    }
}

/**
 * 将表达式转换为指定类型的非空值
 *   支持表达式如: "a"(包含引号) / null / 1 / 3 等
 * @param class 要转换的类型
 * @return
 */
public inline fun <T : Any> String.exprTo(clazz: KClass<T>): T? {
    if (clazz == String::class) {
        if (this.equals("null", true))
            return null

        return this.trim("\"") as T
    }

    var expr = this
    if (clazz == Date::class)
        expr = this.trim("Date\"", "\"", true)

    // 对Long要去掉最后的L, 否则报错 java.lang.NumberFormatException: For input string: "1L"
    if (clazz == Long::class)
        expr = this.trim("", "L", true)

    // 值：转换类型
    return expr.to(clazz)
}

/**
 * 将表达式转换为指定类型的非空值
 * @param class 要转换的类型
 * @return
 */
public inline fun <T : Any> String.exprTo(clazz: Class<T>): T? {
    return this.exprTo(clazz.kotlin)
}

/**
 * 将表达式转换为指定类型的非空值
 * @param type
 * @return
 */
public inline fun String.exprTo(type: KType): Any? {
    return this.exprTo(type.classifier as KClass<*>)
}

/**
 * 将当前值强制类型转换为指定类型的非空值
 * @param class 要转换的类型
 * @return
 */
public inline fun Any.castTo(clazz: KClass<*>): Any {
    // 强制类型转换
    return when (clazz) {
        Int::class -> this as Int
        Long::class -> this as Long
        Float::class -> this as Float
        Double::class -> this as Double
        Boolean::class -> this as Boolean
        Short::class -> this as Short
        Byte::class -> this as Byte
        Date::class -> this as Date
        else -> throw IllegalArgumentException("当前值不能自动转换为未识别的类型: " + clazz)
    }
}

/**
 * 将字符串转换为指定类型的非空值
 * @param type
 * @return
 */
public inline fun Any.castTo(type: KType): Any {
    return this.castTo(type.classifier as KClass<*>)
}

/**
 * 获得异常堆栈的字符串
 * @return
 */
public fun Throwable.stringifyStackTrace(): String {
    try {
        val sw = StringWriter()
        PrintWriter(sw).use {
            this.printStackTrace(it)
        }
        return sw.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        return this.message ?: ""
    }

}
