package net.jkcode.jkutil.tests

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable
import net.jkcode.jkutil.common.cloneProperties
import net.jkcode.jkutil.common.generateId
import java.util.*

open class A() {
    open fun echo(){}
}
class B(): A() {
    /*override fun echo(){
        println("Ah")
    }*/
}

fun A.sayHi(){
    println("hi, I'm A")
}

fun B.sayHi(){
    println("hi, I'm B")
}

enum class NumType {
    Byte,
    Short,
    INT,
    LONG
}

class Lambda {
}

data class Thing(val name: String, val weight: Int)

data class Address(val value: String, val alias: String? = null)

class ManHolder(val man: IMan)

interface IMan {
    val id: Long
    var name: String
    var age: Int
}

data class Man(override var name: String, override var age: Int): Cloneable, Serializable, IMan {

    @JSONField
    override val id: Long = generateId("man")

    // 属性值如果为null, 则压根不输出
    @JSONField
    val birthday: Date? = null

    /*public override fun clone(): Any {
        return super.clone()
    }*/

    @JSONField
    override fun toString(): String {
        return "${javaClass.name}: id=$id, name=$name, age=$age"
    }
}

class Family(val master: Man, val members: List<Man>): Cloneable, Serializable{

    val address: Address = Address("HongKong, China", "home")

    // 也会输出到xml中
    val size: Int = members.size

    var deepClone: Boolean = false

    public override fun clone(): Any {
        val o = super.clone()

        // 深克隆
        if(deepClone)
            o.cloneProperties("master", "members")
        return o
    }

    override fun toString(): String {
        return "${javaClass.name}: master=<$master>, members=<$members>, address=<$address>"
    }
}
