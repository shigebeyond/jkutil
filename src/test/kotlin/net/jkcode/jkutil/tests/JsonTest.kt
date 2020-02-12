package net.jkcode.jkutil.tests

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.serializer.SerializerFeature
import org.junit.Test

class JsonTest {


    @Test
    fun testEscape(){
        val str = "\b\n"
        println(str)
        val es = JSON.toJSONString(str)
        println(es)
    }


    // map -> json
    @Test
    fun testJson(){
        val o = HashMap<String, Any?>()
        o["notify_url"] = "http://baidu.com" // 原始值
        var json = JSON.toJSONString(o)
        println(json) // 输出 {"notify_url":"http://baidu.com"}

        json = JSON.toJSONString(o, SerializerFeature.WriteSlashAsSpecial)
        println(json) // 输出: {"notify_url":"http:\/\/baidu.com"}

        val o2 = JSONObject.parse(json) as JSONObject
        println(o2["notify_url"])
    }

    // object -> json
    @Test
    fun testJson2(){
        val o = Man("shi", 12)
        var json = JSON.toJSONString(o)
        println(json) // 输出 {"age":12,"id":105254286010613760,"name":"shi"}

        json = JSON.toJSONString(o, SerializerFeature.WriteSlashAsSpecial)
        println(json) // 输出: {"age":12,"id":105254286010613760,"name":"shi"}

        val o2 = JSON.parseObject(json, Man::class.java);
        println(o2)
    }

    // exception -> json
    @Test
    fun testJson3(){
        val o = IllegalArgumentException("test error") // 异常
        var json = JSON.toJSONString(o)
        println(json)

        val o2 = JSON.parseObject(json, IllegalArgumentException::class.java);
        println(o2)
    }

    // 特殊字符转义
    @Test
    fun testJson4(){
        val o = HashMap<String, Any?>()
        o["name"] = 129.toChar() // 特殊字符转义
        var json = JSON.toJSONString(o)
        println(json) // 输出: {"name":"\u0081"}

        val o2 = JSON.parseObject(json);
        println(o2)
    }


    // null
    @Test
    fun testJson5(){
        var json = JSON.toJSONString(null)
        println(json) // 输出: null

        val o2 = JSON.parseObject(json);
        println(o2)
    }

    class FormRowSet : ArrayList<String>() {

        /**
         * @return true indicates that multiple row results are to be expected,
         * even if the actual result is empty or just a single row.
         */
        var isMultiRow = false
        var referenceTable: String? = null
        var referenceKey: String? = null
    }

    // 继承 list, 有自己的属性 -- 只输出list元素, 不输出特有属性, 符合jkerp中要求
    @Test
    fun testJson6(){
        val o = FormRowSet()
        o.isMultiRow = true
        o.referenceTable = "test"
        o.referenceKey = "id"
        o.add("a")
        var json = JSON.toJSONString(o) // ["a"]
        println(json) // 输出: {"name":"\u0081"}

        val o2 = JSON.parse(json)
        println(o2)
    }
}