package net.jkcode.jkutil.tests

import java.io.Serializable
import net.jkcode.jkutil.common.cloneProperties
import net.jkcode.jkutil.common.generateId

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


data class Man(var name: String, var age: Int): Cloneable, Serializable{
    val id = generateId("man")

    /*public override fun clone(): Any {
        return super.clone()
    }*/

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
