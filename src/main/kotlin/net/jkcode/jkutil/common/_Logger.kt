package net.jkcode.jkutil.common

import org.slf4j.Logger

internal val switcher = ModuleLogSwitcher("common")

// 公用的日志
val commonLogger = switcher.getLogger("net.jkcode")
// db的日志
val dbLogger = switcher.getLogger("net.jkcode.jkmvc.db")
// es的日志
val esLogger = switcher.getLogger("net.jkcode.jkmvc.db")
// http的日志
val httpLogger = switcher.getLogger("net.jkcode.jkmvc.http")

// php异常类
val phpExceptionClazz: Class<*>? by lazy {
    try{
        Class.forName("php.runtime.ext.java.JavaException")
    }catch(e: Exception){
        null
    }
}

val throwableField = phpExceptionClazz?.getAccessibleField("throwable")

/**
 * 对异常打日志, 带颜色
 * @param msg
 * @param e
 */
public fun Logger.errorColor(msg: String, e: Throwable) {
    val msg = ColorFormatter.applyTextColor(msg, 31) // 红色
    var ex = e
    // 如果是php包装的异常, 则输出java原生异常
    //if(e.cause is JavaException)
    //  ex = (e.cause as JavaException).throwable
    if(phpExceptionClazz != null && phpExceptionClazz!!.isInstance(e.cause))
        ex = throwableField!!.get(e.cause) as Throwable
    this.error(msg, ex)
}