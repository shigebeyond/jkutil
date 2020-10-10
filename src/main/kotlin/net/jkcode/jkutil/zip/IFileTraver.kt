package net.jkcode.jkutil.zip

import java.io.InputStream

/**
 * 文件遍历器
 * @author shijianhang<772910474@qq.com>
 * @date 2020-10-9 11:03 AM
 */
interface IFileTraver {

    companion object{

        public fun create(path: String): IFileTraver {
            if(path.endsWith(".zip"))
                return ZipFileTraver(path)

            return DirFileTraver(path)
        }

    }

    /**
     * 遍历每个文件项
     */
    fun forEachEntry(action: (IFileEntry)->Unit)
}

/**
 * 文件项目
 * @author shijianhang<772910474@qq.com>
 * @date 2020-10-9 11:03 AM
 */
interface IFileEntry {

    /**
     * 文件名
     */
    val name: String

    /**
     * 是否目录
     */
    val isDirectory: Boolean

    /**
     * 是否文件
     */
    val isFile: Boolean
        get() {
            return !isDirectory
        }

    /**
     * 获得文件的输入流
     * @return
     */
    fun inputStream(): InputStream
}