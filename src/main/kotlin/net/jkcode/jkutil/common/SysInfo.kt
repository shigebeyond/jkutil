package net.jkcode.jkutil.common

import java.net.InetAddress
import java.util.*

/**
 * 系统信息
 */
object SysInfo {

    /**
     * 机器名，也是容器id
     */
    public val hostname: String
        get() {
            return InetAddress.getLocalHost().hostName
        }

    /**
     * 系统信息
     */
    @JvmStatic
    val systemProperties: Properties
        get() {
            val system = System.getProperties()
            system.putAll(jvmProperties)
            return system
        }

    /**
     * jvm信息
     */
    @JvmStatic
    val jvmProperties: Properties
        get() {
            val mb = 1048576
            val runtime = Runtime.getRuntime()
            val used = (runtime.totalMemory() - runtime.freeMemory()) / mb // 已用内存
            val total = runtime.totalMemory() / mb // 总内存
            val max = runtime.maxMemory() / mb // 最大内存
            val processors = runtime.availableProcessors() // cpu核数
            val jvm = Properties()
            jvm.put("platform.jvm.used", "$used MB")
            jvm.put("platform.jvm.total", "$total MB")
            jvm.put("platform.jvm.max", "$max MB")
            jvm.put("platform.jvm.processors", processors.toString())
            return jvm
        }

    /**
     * cpu核数
     */
    @JvmStatic
    val cpuCores: Int
        get() {
            var command = ""
            if (OSInfo.isMac)
                command = "sysctl -n machdep.cpu.core_count"
            else if (OSInfo.isUnix)
                command = "lscpu"
            else if (OSInfo.isWindows) {
                command = "cmd /C WMIC CPU Get /Format:List"
            }

            // 执行命令
            var numberOfCores = 0
            var sockets = 0
            execCommand(command) { line ->
                if (OSInfo.isMac) {
                    numberOfCores = line.toIntOrNull() ?: 0
                } else if (OSInfo.isUnix) {
                    if (line.contains("Core(s) per socket:")) {
                        numberOfCores = line.substringAfter(':').trim().toInt()
                    }
                    if (line.contains("Socket(s):"))
                        sockets = line.substringAfter(':').trim().toInt()
                } else if (OSInfo.isWindows && line.contains("NumberOfCores")) {
                    numberOfCores = line.substringAfter('=').toInt()
                }
            }

            if (OSInfo.isUnix)
                return numberOfCores * sockets
            return numberOfCores
        }
}

