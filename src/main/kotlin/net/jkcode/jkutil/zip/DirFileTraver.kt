package net.jkcode.jkutil.zip

import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * 目录下文件遍历器
 * @author shijianhang<772910474@qq.com>
 * @date 2020-10-9 11:03 AM
 */
class DirFileTraver(path: String) : IFileTraver {

    /**
     * 目录
     */
    protected val dir: File = File(path)

    /**
     * 遍历文件项
     */
    override fun forEachEntry(action: (IFileEntry) -> Unit) {
        dir.listFiles().forEach { file ->
            action(DirFileEntry(file))
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