package net.jkcode.jkutil.common

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * 输出文件
 * @param file
 * @return
 */
public fun OutputStream.writeFile(file: String) {
    writeFromInput(FileInputStream(file))
}
/**
 * 输出文件
 * @param file
 * @return
 */
public fun OutputStream.writeFile(file: File) {
    writeFromInput(FileInputStream(file))
}

/**
 * 从输入流中读取数据并输出
 *    会帮你关掉输入流
 * @param in
 * @return
 */
public fun OutputStream.writeFromInput(`in`: InputStream) {
    `in`.use {
        var length = -1
        val buffer = ByteArray(1024)
        do {
            length = `in`.read(buffer)
            this.write(buffer, 0, length)
        } while (length != -1)
    }
}

/**
 * 遍历压缩文件中的内容
 * @param action
 */
public fun ZipInputStream.forEacheEntry(action: (ZipEntry)->Unit){
    use {
        while (true) {
            val entry = nextEntry
            if (entry == null)
                break
            action(entry)
        }
    }
}