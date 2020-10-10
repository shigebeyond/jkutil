package net.jkcode.jkutil.zip

import net.jkcode.jkutil.common.forEachEntry
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * zip下文件遍历器
 * @author shijianhang<772910474@qq.com>
 * @date 2020-10-9 11:03 AM
 */
class ZipFileTraver(protected var zip: ByteArray) : IFileTraver {

    constructor(path: String): this(ZipFileTraver::class.java.getResourceAsStream(path).use{ it.readBytes() })

    /**
     * 遍历文件项
     */
    override fun forEachEntry(action: (IFileEntry) -> Unit) {
        val `in` = ZipInputStream(ByteArrayInputStream(zip))
        `in`.forEachEntry{ entry ->
            action(ZipFileEntry(entry, `in`))
        }
    }
}

/**
 * zip下文件项目
 * @author shijianhang<772910474@qq.com>
 * @date 2020-10-9 11:03 AM
 */
class ZipFileEntry(protected val entry: ZipEntry, protected val `in`: ZipInputStream): IFileEntry{

    override val name: String
        get() = entry.name

    override val isDirectory: Boolean
        get() = entry.isDirectory

    override fun inputStream(): InputStream {
        return `in`
    }
}