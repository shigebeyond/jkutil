package net.jkcode.jkutil.common

import net.jkcode.jkutil.zip.ZipFileEntry
import net.jkcode.jkutil.zip.ZipFileIterable
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * zip的压缩和解压
 */
object ZipUtil {
    /**
     * 将目录压缩成zip
     * @param srcDir 需要压缩的目录
     * @param destZip 目标压缩文件
     */
    public fun zip(srcDir: String, destZip: String) {
        FileOutputStream(destZip).use { fos ->
            ZipOutputStream(fos).use { zos ->
                val src = File(srcDir)
                // 如果是目录的话，需要指定根路径
                var root: String? = null
                if(src.isDirectory)
                    root = src.path + File.separatorChar

                // 压缩
                compress(src, zos, root)
            }
        }
    }

    /**
     * 将文件列表压缩成zip
     * @param srcFiles 需要压缩的文件列表, 兼容 List<File> 与 List<String>
     * @param destZip 目标压缩文件
     */
    public fun zip(srcFiles: List<*>, destZip: String) {
        FileOutputStream(destZip).use { fos ->
            ZipOutputStream(fos).use { zos ->
                for(src in srcFiles) {
                    // 转File
                    val file: File
                    if(src is File)
                        file = src
                    else
                        file = File(src.toString())

                    // 如果是目录的话，需要指定根路径
                    var root: String? = null
                    if(file.isDirectory)
                        root = file.path + File.separatorChar

                    // 逐个压缩
                    compress(file, zos, root)
                }
            }
        }
    }

    /**
     * 压缩单个文件
     * @param file 要压缩的文件
     * @param zos 压缩输出流
     * @param root 根路径, 如果不为空, 则输出要去掉根路径, 否则直接输出文件名
     */
    private fun compress(file: File, zos: ZipOutputStream, root: String?) {
        file.travel { f ->
            val name: String
            if(root.isNullOrBlank()) // 直接输出文件名
                name = f.name
            else // 去掉根路径, 只保留相对路径
                name = f.path.substring(root.length)
            zos.putNextEntry(ZipEntry(name)) // 1 写zip实体, 标识压缩项名
            zos.writeFile(f) // 2 写文件
            zos.closeEntry()
        }
    }

    /**
     * 解压zip到指定目录
     * @param srcZip zip文件
     * @param destDir 输出目录
     */
    public fun unzip(srcZip: String, destDir: String) {
        for(zipEntry in ZipFileIterable(srcZip)){
            (zipEntry as ZipFileEntry).extractTo(destDir)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val zipfile = "/ohome/shi/test/test.zip"
//        zip("/ohome/shi/test/compare", zipfile)
        unzip(zipfile, "/ohome/shi/test/compare2")
    }

}