package net.jkcode.jkutil.zk

import org.I0Itec.zkclient.serialize.ZkSerializer

/**
 * zk序列化实现
 */
class ZkStringSerializer : ZkSerializer {

    override fun deserialize(bytes: ByteArray): Any {
        return String(bytes, Charsets.UTF_8)
    }

    override fun serialize(obj: Any): ByteArray {
        return obj.toString().toByteArray(Charsets.UTF_8)
    }
}
