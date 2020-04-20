package net.jkcode.jkutil.tests

import com.thoughtworks.xstream.XStream
import net.jkcode.jkutil.common.lcFirst
import org.junit.Test
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*
import com.thoughtworks.xstream.converters.SingleValueConverter
import com.thoughtworks.xstream.converters.Converter
import com.thoughtworks.xstream.converters.MarshallingContext
import com.thoughtworks.xstream.converters.UnmarshallingContext
import com.thoughtworks.xstream.io.HierarchicalStreamReader
import com.thoughtworks.xstream.io.HierarchicalStreamWriter
import net.jkcode.jkutil.xml.StringMapConverter
import org.simpleframework.xml.core.Persister


/**
 * xml序列化
 */
class XmlTests {

    fun <T> parserXML(xml: String): T {
        val `in` = ByteArrayInputStream(xml.toByteArray())
        val decoder = XMLDecoder(BufferedInputStream(`in`))
        decoder.close()
        return decoder.readObject() as T
    }

    fun <T> formatXML(entity: T): String {
        val out = ByteArrayOutputStream()
        val encoder = XMLEncoder(BufferedOutputStream(out))
        encoder.writeObject(entity)
        encoder.close()
        return out.toString()
    }

    @Test
    fun testJavaXml() {
        val man = Man("shi", 12)
        val family = Family(man, emptyList())

        val xml = formatXML<Any>(family)
        println("序列化到XML:\n$xml")

        val obj = parserXML<Any>(xml)
        println("反序列化Bean:\n$obj")
    }

    @Test
    fun testSimpleXml() {
        val man = Man("shi", 12)
        val writer = StringWriter()
        // 写
        val serializer = Persister()
        serializer.write(man, writer)
        val xml = writer.toString()
        println(xml)

        // 读
        val appDef = serializer.read(Man::class.java, xml, false)
        println(appDef)
    }

    @Test
    fun testXstream() {
        val man = Man("shi", 12)
        val family = Family(man, emptyList())

        val xstream = XStream()
        xstream.alias("family", Family::class.java)
        xstream.alias("man", Man::class.java) // 类的别名, 作为标签名
        xstream.useAttributeFor(Man::class.java, "id") // 输出为属性, 否则输出为子元素
        xstream.aliasField("clone", Family::class.java, "deepClone") // 属性别名
//        xstream.aliasAttribute(Family::class.java, "deepClone", "clone") // 属性别名, 也会输出为属性

        val xml = xstream.toXML(family)
        println("序列化到XML:\n$xml")

        val obj = xstream.fromXML(xml)
        println("反序列化Bean:\n$obj")
    }

    @Test
    fun testXstream2() {
        val man = Man("shi", 12)
        val family = Family(man, emptyList())

        val xstream = XStream()
        xstream.alias("family", Family::class.java)
        xstream.alias("man", Man::class.java) // 类的别名, 作为标签名
//        xstream.aliasPackage("ns", "net.jkcode.jkutil.tests") // 但实际引用的是 <ns.Family> 需要加.
//        xstream.aliasPackage("ns:", "net.jkcode.jkutil.tests") // 但实际引用的是 <ns:.Family> 需要加.

        xstream.useAttributeFor(Man::class.java, "id") // 输出为属性, 否则输出为子元素
        xstream.useAttributeFor(Address::class.java, "alias") // 输出为属性, 否则输出为子元素

        // 自定义转换器
//        xstream.registerConverter(AddressConverter())
        xstream.registerConverter(AddressConverter2())

        val file = File("/home/shi/test/family.xml")
        xstream.toXML(family, file.outputStream())
        println("序列化到XML文件:\n$file")

        val obj = xstream.fromXML(file)
        println("反序列化Bean:\n$obj")
    }

    @Test
    fun testXpdlLowcaseTagName(){
        val file = File("/home/shi/code/java/jkerp/wfengine/src/test/resources/test.xpdl")
        var xpdl = file.readText()
        xpdl = "(?<=</?)(xpdl:)?([\\w\\d]+)".toRegex().replace(xpdl) { m: MatchResult ->
            val name = m.groupValues[2]!!
            name.lcFirst()
        }
        println(xpdl)
    }

    @Test
    fun testXstream2Map() {
        val man = mapOf("name" to "shi", "age" to 12)
        val xstream = XStream()
        // 自定义转换器
        xstream.registerConverter(StringMapConverter(xstream.mapper))

        val xml = xstream.toXML(man)
        println("序列化到XML:\n$xml")

        val obj = xstream.fromXML(xml)
        println("反序列化Bean:\n$obj")
    }

}

internal class AddressConverter : SingleValueConverter {

    override fun toString(obj: Any): String {
        return (obj as Address).value
    }

    override fun fromString(value: String): Any {
        return Address(value)
    }

    override fun canConvert(type: Class<*>): Boolean {
        return type == Address::class.java
    }

}

class AddressConverter2 : Converter {

    /**
     * 是否创建下级节点来存储value
     */
    val createDownNode = false

    override fun canConvert(clazz: Class<*>): Boolean {
        return clazz == Address::class.java
    }

    override fun marshal(value: Any, writer: HierarchicalStreamWriter,
                         context: MarshallingContext) {
        val address = value as Address
        // 新的下级节点
        if(createDownNode)
            writer.startNode("Address")
        writer.addAttribute("alias", address.alias)
        writer.setValue(address.value)
        if(createDownNode)
            writer.endNode()
    }

    override fun unmarshal(reader: HierarchicalStreamReader,
                           context: UnmarshallingContext): Any {
        // 从下级节点取值
        if(createDownNode)
            reader.moveDown();
        val alias = reader.getAttribute("alias")
        val value = reader.getValue()
        if(createDownNode)
            reader.moveUp();
        return Address(value, alias)
    }

}