package net.jkcode.jkutil.zip

import net.jkcode.jkutil.common.decorateIterator
import net.jkcode.jkutil.common.getRootPath
import net.jkcode.jkutil.common.isAbsolutePath
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * 目录下文件迭代器
 * @author shijianhang<772910474@qq.com>
 * @date 2020-10-9 11:03 AM
 */
class DirFileIterable(path: String) : IFileIterable {

    companion object{

        fun fixPath(path: String): String {
            if(path.isAbsolutePath)
                return path

            val res = Thread.currentThread().contextClassLoader.getResource(path)
            return res.path
        }

    }

    /**
     * 目录下文件
     */
    protected val files = File(fixPath(path)).listFiles()

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