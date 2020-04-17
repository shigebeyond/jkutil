package net.jkcode.jkutil.message

import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.commonLogger
import net.jkcode.jkutil.common.getDefaultClassLoader
import net.jkcode.jkutil.message.MessageSource.Companion.config
import java.io.InputStreamReader
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
        for (basename in basenames) {
            val bundle = doGetBundle(basename, locale)
            try {
                val result = bundle?.getString(code)
                if (result != null)
                    return result
            }catch (e: MissingResourceException){
            }
        }

        return ""
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