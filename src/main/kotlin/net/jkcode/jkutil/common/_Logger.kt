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

/**
 * 对异常打日志, 带颜色
 * @param msg
 * @param e
 */
public fun Logger.errorColor(msg: String, e: Throwable) {
    val msg = ColorFormatter.applyTextColor(msg, 31) // 红色
    this.error(msg, e)
}