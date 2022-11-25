package net.jkcode.jkutil.serialize

import net.jkcode.jkutil.common.commonLogger
import net.jkcode.jkutil.common.errorColor
import java.io.*

/**
 * 基于jdk的序列化
 *
 * @author shijianhang
 * @create 2017-10-04 下午3:29
 **/
class JdkSerializer : ISerializer {

    /**
     * 序列化
     *
     * @param obj
     * @return
     */
    public override fun serialize(obj: Any): ByteArray? {
        try {
            val bo = ByteArrayOutputStream()
            ObjectOutputStream(bo).use {
                it.writeObject(obj)
            }
            return bo.toByteArray()
        } catch (e: IOException) {
            commonLogger.errorColor(this.javaClass.name + "序列化错误", e)
            return null
        }
    }

    /**
     * 反序列化
     *
     * @param input
     * @return
     */
    public override fun unserialize(input: InputStream): Any? {
        try {
            val oi = ObjectInputStream(input)
            return oi.readObject()
        } catch (e: Exception) {
            commonLogger.errorColor(this.javaClass.name + "反序列化错误", e)
            return null
        }
    }
}