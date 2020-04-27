package net.jkcode.jkutil.common

import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*

/**
 * Utility methods to generate an UUID
 *
 */
object UuidGenerator {

    private val seeder: Random = Random()

    private val zero = charArrayOf('0', '0', '0', '0', '0', '0', '0', '0', '0', '0')

    private val midValue: String by lazy{
        var inet: InetAddress? = null
        var bytes: ByteArray
        try {
            inet = InetAddress.getLocalHost()
            bytes = inet!!.address
        } catch (e: UnknownHostException) {
            bytes = "127.0.0.1".toByteArray()
        }

        val sb = StringBuffer()
        for (c in 0..3) {
            val i = bytes[c].toInt() and 0xff
            sb.append(hexFormat(i, 2))
        }

        val hexInetAddress = sb.toString()
        val thisHashCode = hexFormat(System.identityHashCode(this), 8)
        "$hexInetAddress-$thisHashCode"
    }

    private fun hexFormat(`val`: Int, length: Int): String {
        val sb = StringBuffer(Integer.toHexString(`val`))
        if (sb.length < length) {
            sb.append(zero, 0, length - sb.length)
        }
        return sb.toString()
    }

    /**
     * Generate an UUID
     * @return
     */
    @Synchronized
    fun nextId(): String {
        val timeNow = System.currentTimeMillis()
        val timeLow = timeNow.toInt() and -1
        val node = seeder.nextInt()
        return hexFormat(timeLow, 8) + "-" + midValue + "-" + hexFormat(node, 8)
    }

}
