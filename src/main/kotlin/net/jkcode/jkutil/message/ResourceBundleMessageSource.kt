package net.jkcode.jkutil.message

import net.jkcode.jkutil.common.commonLogger
import net.jkcode.jkutil.common.getDefaultClassLoader
import net.jkcode.jkutil.message.MessageSource.Companion.config
import java.io.InputStreamReader
import java.text.MessageFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * 通过bundle来实现的消息源
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2020-2-30 4:57 PM
 */
open class ResourceBundleMessageSource : MessageSource {

    /**
     * ResourceBundles的缓存
     *    key是basename, value是Map<Locale, ResourceBundle>
     */
    private val cachedResourceBundles = HashMap<String, MutableMap<Locale, ResourceBundle>>()

    /**
     * 获得消息
     */
    override fun getMessage(code: String, args: Array<*>?, defaultMessage: String?, locale: Locale): String {
        val basenames: List<String> = config["basenames"]!!
        // 遍历每个消息文件
        for (basename in basenames) {
            val bundle = doGetBundle(basename, locale)
            try {
                // 获得消息
                val result = bundle?.getString(code)
                // 格式化消息(替换参数)
                if (result != null)
                    return formateMessage(result, args, locale)
            }catch (e: MissingResourceException){
            }
        }

        return ""
    }

    /**
     * 格式化消息(替换参数)
     *
     * @param msg 消息
     * @param args 参数
     * @param locale
     * @return
     */
    protected open fun formateMessage(msg: String, args: Array<*>?, locale: Locale): String {
        if(args.isNullOrEmpty())
            return msg

        // 拼接参数
        val formatter = MessageFormat(msg)
        if (locale != null)
            formatter.setLocale(locale)
        return formatter.format(args)
    }

    /**
     * 加载bundle
     */
    private fun doGetBundle(basename: String, locale: Locale): ResourceBundle? {
        try {
            return cachedResourceBundles.getOrPut(basename){ // 第一层basename
                        HashMap()
                    }
                    .getOrPut(locale){ // 第二层 Map<Locale, ResourceBundle>
                        ResourceBundle.getBundle(basename, locale, getDefaultClassLoader() /* 不能省, 否则加载不了资源 */, EncodingResourceBundleControl("utf-8"))
                    }
        } catch (ex: MissingResourceException) {
            // Assume bundle not found
            // -> do NOT throw the exception to allow for checking parent message source.
            return null
        }

    }

    /**
     * copy from javafx -- com.sun.webkit.LocalizedStrings.EncodingResourceBundleControl
     */
    inner class EncodingResourceBundleControl(private val encoding: String) : ResourceBundle.Control() {

        override fun newBundle(baseName: String, locale: Locale,
                               format: String, loader: ClassLoader,
                               reload: Boolean): ResourceBundle {
            val bundleName = toBundleName(baseName, locale)
            val resourceName = toResourceName(bundleName, "properties")
            val resourceURL = loader.getResource(resourceName)
            if (resourceURL != null) {
                try {
                    return PropertyResourceBundle(InputStreamReader(resourceURL.openStream(), encoding))
                } catch (z: Exception) {
                    commonLogger.error("exception thrown during bundle initialization", z)
                }
            }

            return super.newBundle(baseName, locale, format, loader, reload)
        }
    }
}