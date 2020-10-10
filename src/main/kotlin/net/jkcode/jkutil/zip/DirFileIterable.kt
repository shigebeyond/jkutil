package net.jkcode.jkutil.zip

import net.jkcode.jkutil.common.decorateIterator
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * 目录下文件迭代器
 * @author shijianhang<772910474@qq.com>
 * @date 2020-10-9 11:03 AM
 */
class DirFileIterable(path: String) : IFileIterable {

    /**
     * 目录下文件
     */
    protected val files = File(path).listFiles()

    /**
     * 包装目录下文件迭代
     */
    override fun iterator(): Iterator<IFileEntry> {
        return decorateIterator(files.iterator()){ file ->
            DirFileEntry(file)
        }
    }
}

/**
 * 目录下文件项目
 * @author shijianhang<772910474@qq.com>
 * @date 2020-10-9 11:03 AM
 */
class DirFileEntry(protected val file: File): IFileEntry{

    override val name: String
        get() = file.absolutePath

    override val isDirectory: Boolean
        get() = file.isDirectory

    override fun inputStream(): InputStream {
        return FileInputStream(file)
    }
}