package net.jkcode.jkutil.serialize

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import net.jkcode.jkutil.common.commonLogger
import net.jkcode.jkutil.common.errorAndPrint
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * 基于Hessian的序列化
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-11-10 4:18 PM
 */
class HessianSerializer: ISerializer {

    /**
     * 序列化
     *
     * @param obj
     * @return
     */
    public override fun serialize(obj: Any): ByteArray? {
        try {
            val bo = ByteArrayOutputStream()
            val hos = Hessian2Output(bo)
            hos.writeObject(obj)
            hos.flush()
            return bo.toByteArray()
        } catch (e: IOException) {
            commonLogger.errorAndPrint(this.javaClass.name + "序列化错误", e)
            return null;
        }
    }

    /**
     * 反序列化
     *
     * @param input
     * @return
     */
    public override fun unserialize(input: InputStream): Any? {
        try{
            val his = Hessian2Input(input)
            return his.readObject()
        } catch (e: IOException) {
            commonLogger.errorAndPrint(this.javaClass.name + "序列化错误", e)
            return null;
        }
    }

}