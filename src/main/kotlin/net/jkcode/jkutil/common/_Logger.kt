package net.jkcode.jkutil.common

import org.slf4j.Logger

internal val switcher = ModuleLogSwitcher("common")

// 公用的日志
val commonLogger = switcher.getLogger("net.jkcode")
// db的日志
val dbLogger = switcher.getLogger("net.jkcode.jkmvc.db")
// http的日志
val httpLogger = switcher.getLogger("net.jkcode.jkmvc.http")

/**
 * 对异常打日志+输出
 * @param msg
 * @param e
 */
public fun Logger.errorAndPrint(msg: String, e: Exception) {
    this.error(msg, e)
    e.printStackTrace()
}