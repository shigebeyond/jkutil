package net.jkcode.jkutil.zip

import java.io.InputStream

/**
 * 文件迭代器
 * @author shijianhang<772910474@qq.com>
 * @date 2020-10-9 11:03 AM
 */
interface IFileIterable: Iterable<IFileEntry>{

    companion object{
        /**
         * 文件迭代器
         */
        public fun create(path: String): IFileIterable {
            if(path.endsWith(".zip"))
                return ZipFileIterable(path)

            return DirFileIterable(path)
        }
    }
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