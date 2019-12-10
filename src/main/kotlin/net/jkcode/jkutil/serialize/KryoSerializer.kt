package net.jkcode.jkutil.serialize

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import net.jkcode.jkutil.common.commonLogger
import net.jkcode.jkutil.common.errorAndPrint
import java.io.IOException
import java.io.InputStream

/**
 * 基于Kryo的序列化
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-11-10 4:18 PM
 */
class KryoSerializer: ISerializer {

    /**
     * 序列化
     *
     * @param obj
     * @return
     */
    public override fun serialize(obj: Any): ByteArray? {
        try {
            val kryo = Kryo()
            val output = Output(4096, 4096)
            kryo.writeClassAndObject(output, obj)
            return output.toBytes()
        } catch (e: IOException) {
            commonLogger.errorAndPrint(this.javaClass.name + "序列化错误", e)
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
            val kryo = Kryo()
            val input = Input(input)
            return kryo.readClassAndObject(input)
        } catch (e: IOException) {
            commonLogger.errorAndPrint(this.javaClass.name + "反序列化错误", e)
            return null
        }
    }

}