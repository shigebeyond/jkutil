package net.jkcode.jkutil.zip

import net.jkcode.jkutil.common.decorateNextMethodIterator
import net.jkcode.jkutil.iterator.NextMethodIterator
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.FileSystems
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * zip下文件迭代器
 * @author shijianhang<772910474@qq.com>
 * @date 2020-10-9 11:03 AM
 */
class ZipFileIterable(protected var zip: ByteArray) : IFileIterable {

    constructor(path: String): this(readZipBytes(path))

    companion object{
        /**
         * 读取zip文件的数据
         * @param zip文件路径
         * @return
         */
        fun readZipBytes(file: String): ByteArray {
            val path = FileSystems.getDefault().getPath(file)
            if(path.isAbsolute)
                return File(file).readBytes()

            val res = Thread.currentThread().contextClassLoader.getResourceAsStream(file)
            return res.readBytes()
        }
    }

    /**
     * 包装zip下文件迭代
     */
    override fun iterator(): Iterator<IFileEntry> {
        val `in` = ZipInputStream(ByteArrayInputStream(zip))
        return decorateNextMethodIterator {
            val entry = `in`.nextEntry
            if(entry == null) {
                // 迭代到最后元素了,则关闭流
                `in`.close()
                null
            }else {
                ZipFileEntry(entry, `in`)
            }
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