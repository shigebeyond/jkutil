package net.jkcode.jkutil.common

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

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