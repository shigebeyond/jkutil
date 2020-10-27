package net.jkcode.jkutil.message

import net.jkcode.jkutil.common.Config
import java.util.*

/**
 * 消息源接口
 *   参考spring
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2020-2-30 4:57 PM
 */
public interface MessageSource {

    companion object{

        public val config: Config = Config.instance("message", "yaml")

        private val inst: MessageSource by lazy {
            val clazz: String = config["impl"] ?: ResourceBundleMessageSource::class.qualifiedName!!
            Class.forName(clazz).newInstance() as MessageSource
        }

        public fun instance(): MessageSource {
            return inst
        }

        val currentLocale: Locale
            get() = instance().currentLocale
    }

    /**
     * 当前语言
     */
    open val currentLocale: Locale
        get() = Locale .getDefault()

    /**
     * Try to resolve the message. Return default message if no message was found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
     * this class are encouraged to base message names on the relevant fully
     * qualified class name, thus avoiding conflict and ensuring maximum clarity.
     * @param args an array of arguments that will be filled in for params within
     * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
     * or `null` if none.
     * @param defaultMessage a default message to return if the lookup fails
     * @param locale the locale in which to do the lookup
     * @return the resolved message if the lookup was successful;
     * otherwise the default message passed as a parameter
     * @see java.text.MessageFormat
     */
    fun getMessage(code: String, args: Array<*>? = null, defaultMessage: String? = "", locale: Locale = currentLocale): String

    fun getMessage(code: String, defaultMessage: String? = "", locale: Locale = currentLocale): String{
        return getMessage(code, null, defaultMessage, locale)
    }

    fun getMessage(code: String, locale: Locale): String{
        return getMessage(code, null, "", locale)
    }
}