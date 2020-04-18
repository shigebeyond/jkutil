package net.jkcode.jkutil.common

import java.io.*
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
            // 读: in -> buffer
            length = `in`.read(buffer)
            // 写: buffer -> out
            if(length != -1)
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

/**
 * 尝试关闭
 */
public fun Closeable?.tryClose(){
    try {
        this?.close()
    } catch (e: Exception) {
    }
}