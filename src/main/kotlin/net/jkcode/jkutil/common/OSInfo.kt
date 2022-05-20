package net.jkcode.jkutil.common

/**
 * 操作系统系统
 */
object OSInfo {

    private val OS = System.getProperty("os.name").toLowerCase()

    val isWindows: Boolean
        get() = this.OS.contains("win")

    val isMac: Boolean
        get() = this.OS.contains("mac")

    val isUnix: Boolean
        get() = this.OS.contains("nix") || this.OS.contains("nux") || this.OS.contains("aix")

    val isSolaris: Boolean
        get() = this.OS.contains("sunos")

    val os: String
        get() {
            if (isWindows)
                return "win"

            if (isMac)
                return "osx"

            if (isUnix)
                return "uni"

            if (isSolaris)
                return "sol"

            return "err"
        }
}