package net.jkcode.jkutil.common

/**
 * 带颜色的文本输出格式控制器
 * 参考 https://www.jianshu.com/p/6659c72ac0f5
 */
object ColorFormatter {

    /**
     * 通过\033特殊转义字符实现输出格式控制
     * @param content 待格式化的内容
     * @param fontColor 字体颜色：30黑 31红 32绿 33黄 34蓝 35紫 36深绿 37白
     * @param fontType 字体格式：0重置 1加粗 2减弱 3斜体 4下划线 5慢速闪烁 6快速闪烁
     * @param backgroundColor 字背景颜色：40黑 41红 42绿 43黄 44蓝 45紫 46深绿 47白
     */
    fun applyFormat(content: String, fontColor: Int, fontType: Int, backgroundColor: Int): String {
        return String.format("\u001b[%d;%d;%dm%s\u001b[0m", fontColor, fontType, backgroundColor, content)
    }

    /**
     * 通过\033特殊转义字符实现输出格式控制，获得带颜色的字体输出
     * @param content 待格式化的内容
     * @param fontColor 字体颜色：30黑 31红 32绿 33黄 34蓝 35紫 36深绿 37白
     */
    fun applyTextColor(content: String, fontColor: Int): String {
        return String.format("\u001b[%dm%s\u001b[0m", fontColor, content)
    }

    /**
     * 通过\033特殊转义字符实现输出格式控制，获得带背景颜色的字体输出
     * @param content 待格式化的内容
     * @param backgroundColor 字背景颜色：40黑 41红 42绿 43黄 44蓝 45紫 46深绿 47白
     */
    fun applyBackgroundColor(content: String, backgroundColor: Int): String {
        return String.format("\u001b[%dm%s\u001b[0m", backgroundColor, content)
    }

    /**
     * 能接受一个顺序标识，按顺序产生带颜色的输出字符串
     */
    fun applyOrderedColor(content: String, i: Int): String {
        val tmpColor = 31 + i % 7
        return String.format("\u001b[%dm%s\u001b[0m", tmpColor, content)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println(applyFormat("字体颜色为红色，背景色为黄色，带下划线", 31, 4, 43))
        //按顺序输出各个颜色代码的字符串
        for (i in 0..6) {
            println(applyTextColor(String.format("color code: %d", 31 + i), 31 + i))
        }
        //按顺序输出各个背景颜色代码的字符串
        for (i in 0..6) {
            println(applyBackgroundColor(String.format("background color code: %d", 41 + i), 41 + i))
        }
        //按顺序输出各个颜色代码的字符串
        for (i in 0..6) {
            println(applyOrderedColor(String.format("font color code: %d", 31 + i), i))
        }
    }
}
