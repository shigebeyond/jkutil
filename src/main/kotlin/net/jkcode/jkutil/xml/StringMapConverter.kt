package net.jkcode.jkutil.xml

import com.thoughtworks.xstream.converters.MarshallingContext
import com.thoughtworks.xstream.converters.UnmarshallingContext
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter
import com.thoughtworks.xstream.io.HierarchicalStreamReader
import com.thoughtworks.xstream.io.HierarchicalStreamWriter
import com.thoughtworks.xstream.mapper.Mapper
import net.jkcode.jkutil.common.isSuperClass

/**
 * map的xstream转换器, 只适用于 Map<String, String>, 会导致类型丢失
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2020-2-12 11:22 AM
 */
class StringMapConverter(mapper: Mapper) : AbstractCollectionConverter(mapper) {

    override fun canConvert(type: Class<*>): Boolean {
        return Map::class.java.isSuperClass(type)
    }

    // 序列化
    override fun marshal(source: Any, writer: HierarchicalStreamWriter, context: MarshallingContext) {
        val map = source as Map<*, *>
        // 遍历map创建子节点
        for((key, value) in map){
            writer.startNode(key.toString()) // key作为节点名
            writer.setValue(value?.toString()) // value作为节点值
            writer.endNode()
        }
    }

    // 反序列化
    override fun unmarshal(reader: HierarchicalStreamReader, context: UnmarshallingContext): Any {
        val map = createCollection(context.requiredType) as MutableMap<String, String>
        // 遍历子节点来构建map
        while (reader.hasMoreChildren()) {
            reader.moveDown()
            val key = reader.nodeName // key作为节点名
            val value = reader.value // value作为节点值
            map[key] = value
            reader.moveUp()
        }
        return map
    }
}  