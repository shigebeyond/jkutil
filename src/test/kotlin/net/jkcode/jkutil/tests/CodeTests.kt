package net.jkcode.jkutil.tests

import net.jkcode.jkutil.common.lcFirst
import net.jkcode.jkutil.common.replaceText
import net.jkcode.jkutil.common.travel
import net.jkcode.jkutil.common.trim
import org.junit.Test
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class CodeTests {

    /**
     * 统计行数
     */
    @Test
    fun testLineStat(){
        var count:Int = 0
        val dir = File("/home/shi/code/java/jkmvc/")
        val reg = "^\\s*(//|/\\*|\\*|\\*/).*".toRegex() // 注释
        dir.travel { file ->
            if(file.name.endsWith(".kt")){
                file.forEachLine { line ->
                    if(line.isNotBlank() && !reg.matches(line)){ // 非空 + 非注释
                        count++
                    }else{
                        //println(line)
                    }
                }
            }
        }
        println(count)
    }

    /**
     * Escape regex syntax in a string
     * @param inStr
     * @return
     */
    fun escapeRegex(inStr: String): String{
        return inStr?.replace("([\\\\*+\\[\\](){}\$.?\\^|])".toRegex(), "\\\$1")
    }

    /**
     * 多行的复杂的模式替换
     */
    @Test
    fun testCompletedReplace(){
        val dir = File("/home/shi/code/java/jkerp")
        val fileExtReg = "(java|kt|json|jsp|ftl)$".toRegex() // 文件扩展名
        val prefReg = "val (\\w+) = JSONObject\\(\\)".toRegex() // 前缀
        val replace = { str: String, m: MatchResult -> // 参数: 1 作用域字符串 2 prefReg匹配结果
            str
        }

        dir.travel { file ->
            if (!file.name.matches(fileExtReg))
                return@travel

            // 循环读写, 因为写的话会导致匹配重新开始
            while (true) {
                // 收集前缀
                var code = file.readText()
                val m = prefReg.find(code)
                if(m == null)
                    break

                // 找到当前作用域范围
                val range = getCurrScopeBackRange(code, m.range.endInclusive + 1)
                if(range == null)
                    continue
                // 获得作用域代码
                var scope = code.substring(range)
                return@travel

                // 替换作用域代码
//                scope = replace(scope, m)
//                code = code.replaceRange(range, scope)

                // 回写文件
//                file.writeText(code)
            }
        }
    }

    /**
     * 获得作用域的后半部分范围
     */
    val scopeReg = "[\\{\\}]".toRegex()
    fun getCurrScopeBackRange(str: String, start:Int): IntRange? {
        var level = 0
        val ms = scopeReg.findAll(str, start)
        for(m in ms){
            val delimiter = m.groups[0]!!.value
            if(delimiter == "{") // {
                level++
            else // }
                level--
            if(level == -1) // 超过作用域
            {
                val end = m.range.endInclusive - 1
                return IntRange(start, end)
            }
        }
        return null
    }

    /**
     * 获得作用域的前半部分范围
     */
    fun getCurrScopeFrontRange(str: String, end:Int): IntRange? {
        // 由于 Regex.findAll() 没有向前匹配的方法, 因此只能截取子字符串+反序来做匹配
        // TODO: 截取子字符串+反序在大文件中会有内存消耗大的问题
        val front = str.substring(0, end).reversed()
        var level = 0
        val ms = scopeReg.findAll(front)
        for(m in ms){
            val delimiter = m.groups[0]!!.value
            // 由于是反序, 这里的判断与 getCurrScopeBackRange() 相反
            if(delimiter == "{") // {
                level--
            else // }
                level++
            if(level == -1) // 超过作用域
            {
                val endReversed = m.range.endInclusive - 1
                val start = end - endReversed
                return IntRange(start, end)
            }
        }
        return null
    }

    /**
     * 删除异常捕获的语句+提升try{}内部的作用域到上级
     */
    @Test
    fun testDeleteExceptionCatchBubbleScop(){
        val dir = File("/home/shi/code/java/jkerp")
        val fileExtReg = "(java|kt|json|jsp|ftl)$".toRegex() // 文件扩展名
        val sentenceReg = "\\} catch \\(e: FileLimitException\\) \\{".toRegex() // 异常捕获的语句
        val prefLen = "try {\n".length
        val postLen = "}\n".length

        dir.travel { file ->
            if (!file.name.endsWith(".kt"))
                return@travel

            // 循环读写, 因为写的话会导致匹配重新开始
            while (true) {
                // 收集异常捕获的语句
                var code = file.readText()
                val m = sentenceReg.find(code)
                if(m == null)
                    break

                // 1 前半部分
                val frontRange = getCurrScopeFrontRange(code, m.range.start - 1)!!
                // 获得作用域代码
                val frontScope = code.substring(frontRange)
                // 删除一个缩进
                val newFrontScope = frontScope.replace("\n\t", "\n").replace("^\t+", "")
                // 替换作用域代码
                val start = frontRange.start - prefLen - 1
                //val end = frontRange.endInclusive //
                var end = m.range.endInclusive + 1// 连当前的异常捕获句子也删掉
                code = code.replaceRange(start, end + 1, newFrontScope) // +1是因为replaceRange()的第二个参数: the index of the first character after the replacement to keep in the string.

                val span = newFrontScope.length - (end - start)
                end += span

                // 2 后半部分
                val backRange = getCurrScopeBackRange(code, end)!!
                // 直接删除作用域代码
                code = code.replaceRange(backRange.start, backRange.endInclusive + postLen + 1, "")

                // 回写文件
                file.writeText(code)
            }
        }
    }

    /**
     * php转kt -- 变量处理
     *   直接找第二层的{}作用域, 然后对作用域内的代码进行变量替换
     *   问题: 如果是参数进行赋值, 则仍被识别为首次声明, 添加 val 标识
     *   => 参数名也加入到 names: HashSet<String> 中, 当做已声明变量
     */
    @Test
    fun testPhp2KtVar(){
        val dir = File("/home/shi/code/php/beyond2/modules-ext/workflow/classes/Model/Wf")
        dir.travel { file ->
            if (!file.name.endsWith(".php"))
                return@travel

            println("fix file: ${file.name}")
            var code = file.readText()

            // 找到第二层的多个作用域范围
            val ranges = getLevelScopeRanges(code, 2)
            var span = 0 // 偏移
            // 逐个作用域替换
            for (range in ranges) {
                val range2 = (range.start + span)..(range.endInclusive + span)
                // 获得作用域代码
                val scope = code.substring(range2)
//                println(scope)
//                return@travel

                // 作用域内修正php变量
                val vars = HashSet<String>()
                vars.add("this")
                val newScope = fixPhpVar(scope, vars)

                // 变了才替换
                if(newScope != scope) {
                    // 替换作用域代码
                    code = code.replaceRange(range2, newScope)

                    // 偏移
                    span += (newScope.length - scope.length)
                }

            }
            // 回写文件
            file.writeText(code)
        }
    }

    /**
     * php转kt -- 变量处理
     */
    @Test
    fun testPhp2KtVar2(){
        val dir = File("/home/shi/code/php/beyond2/modules-ext/workflow/classes")
        dir.travel { file ->
            if (!file.name.endsWith(".php"))
                return@travel

            println("fix file: ${file.name}")
            var code = file.readText()
            
            // 解析函数: 函数名+参数+范围
            val funs = parseFuns(code)
            var span = 0 // 偏移
            // 逐个函数的作用域替换
            for (f in funs) {
                val (name, args, range) = f
                // 获得函数内的作用域
                val range2 = getCurrScopeBackRange(code, range.endInclusive + 1 + span)!!
                // 获得作用域代码
                val scope = code.substring(range2)
//                println(scope)
//                return@travel

                // 作用域内修正php变量
                val vars = HashSet<String>()
                vars.add("this")
                vars.addAll(args) // 函数变量也认做变量
                val newScope = fixPhpVar(scope, vars)

                // 变了才替换
                if(newScope != scope) {
                    // 替换作用域代码
                    code = code.replaceRange(range2, newScope)

                    // 偏移
                    span += (newScope.length - scope.length)
                }

            }
            // 回写文件
            file.writeText(code)
        }
    }

    /**
     * 解析函数: 函数名+参数+范围
     */
    val funReg = "function\\s+(\\w+)\\s*\\((.*)\\)\\s*\\{".toRegex()
    fun parseFuns(code: String): ArrayList<Triple<String, List<String>, IntRange>> {
        val matches = funReg.findAll(code)
        val result = ArrayList<Triple<String, List<String>, IntRange>>()
        for(m in matches){
            val name = m.groups[1]!!.value // 函数名
            val argExp = m.groups[2]!!.value // 参数表达式
            val args = parseArgs(argExp) // 参数列表
            result.add(Triple(name, args, m.range))
        }
        return result
    }

    /**
     * 解析函数实参, 不解析具体类型
     *
     * @param exp 参数表达式, 参数之间用`,`分隔, 如 `"hello", 1` 
     *            每个参数不能包含以下字符: `,()`
     * @return
     */
    val argReg: Regex = ("([^,]+),?").toRegex();
    fun parseArgs(exp: String): List<String> {
        if(exp.isBlank())
            return emptyList()

        val matches: Sequence<MatchResult> = argReg.findAll(exp);
        val result = java.util.ArrayList<String>();
        for(m in matches) {
            var name = m.groups[1]!!.value
            name = name.split("=").first()
            name = name.trim().trim("\$")
            result.add(name)
        }

        return result
    }

    /**
     * 修正php变量
     *   识别行开头就是变量的场景, 但忽略  $this / $var->xxx 的情况
     */
    val varReg = "\n(\\s+)\\$(\\w+)(.)".toRegex()
    fun fixPhpVar(code: String, names: HashSet<String>): String {
        return varReg.replace(code){ m ->
            val origin = m.groupValues[0] // 原文
            val blank = m.groupValues[1] // 空白
            val name = m.groupValues[2] // 变量名
            val op = m.groupValues[3] // -> 符号
            if(op == "-" /* -> */ || op == "["  || names.contains(name)){ // 旧变量
                origin
            }else { // 新变量: 声明
                names.add(name)
                "\n${blank}val \$" + name + op
            }
        }
    }

    /**
     * 获得某一层的多个作用域范围
     */
    fun getLevelScopeRanges(str: String, level: Int): ArrayList<IntRange> {
        var curr = 0
        val ms = scopeReg.findAll(str)
        val ranges = ArrayList<IntRange>()
        var start = -1
        for(m in ms){
            val delimiter = m.groups[0]!!.value
            if(delimiter == "{") { // {
                curr++
                if(curr == level) //
                    start = m.range.start + 1
            }else { // }
                curr--
                if(curr == level - 1){
                    if(start == -1)
                        throw IllegalStateException("{}没有配对")

                    val end = m.range.endInclusive - 1
                    val range = IntRange(start, end)
                    ranges.add(range)

                    start = -1
                }
            }
        }
        return ranges
    }

    /**
     * 下载网易公开课
     */
    @Test
    fun testDownCourse(){
        // http://yipeiwu.com/getvideo.html
        // 下载网易公开课
        val f = File("/home/shi/test/course.html")
        val ms = "<tr>\\s*<td>(.+)\\s*</td>\\s*<td><a href=\"([^\"]+)\".+</td>\\s*</tr>".toRegex().findAll(f.readText())
        for(m in ms){
            val title = m.groups[1]!!.value
            val url = m.groups[2]!!.value
            val ext = url.substringAfterLast('.')
            // 服务器拒绝 aria2c 下载，只能用curl
            println("aria2c -s 2 '$url' -o '$title.$ext'")
        }
    }

    /**
     * 添加行号
     */
    @Test
    fun testAddLineNo(){
        // 添加行号
        val f = File("/home/shi/test/voice.txt")
        var i = 1
        f.forEachLine { line ->
            if(needLineNo(line))
                println((i++).toString() + ". " + line)
            else
                println(line)
        }
    }

    fun needLineNo(line: String): Boolean {
        // 空行
        if(line.isBlank())
            return false

        // 标题
        if(line.startsWith("#"))
            return false

        // 已有行号
        if("^\\d+".toRegex().matches(line))
            return false

        return true
    }

    /**
     * controller的action方法改名
     */
    @Test
    fun testFixAction (){
        // 由actionIndex, 改为index
        val dir = File("/oldhome/shi/code/java/jkmvc/jkmvc-example/src/main/kotlin/com/jkmvc/example")
        dir.travel {
            if(it.name.indexOf("Controller.kt") > 0){
                it.replaceText {
                    "fun\\s+action([\\w\\d]+)".toRegex().replace(it){ result: MatchResult ->
                        "fun " + result.groups[1]!!.value.decapitalize() + "Action"
                    }
                }
                println("处理文件: " + it.name)
            }
        }

    }

    /**
     * 扫描出分析模型的字段与注释
     */
    @Test
    fun testScanModelField(){
        val dir = File("/home/shi/下载/电网项目/source/021RecoverPowerFast_AlarmAnalyse/src/com/yingkai/lpam/pojo")
        var i = 0;
        var clazz = ""
        dir.travel {
            //println(it)
            it.forEachLine {
                // 获得类注释
                val matches = "^\\s+\\*\\s+([^@]+)$".toRegex().find(it)
                if(matches != null){
                    i++
                    clazz = matches.groups[1]!!.value
                }else{
                    // 获得表名
                    val matches = "^@Table\\(name=\"(.+)\"\\)$".toRegex().find(it)
                    if(matches != null){
                        println("\n-------------------------------------------------\n")
                        println(i.toString() + matches.groups[1]!!.value + "\t" + clazz)
                        println("-- 字段")
                    }else{
                        // 获得字段
                        val matches = "^\\s+private\\s+(.+)$".toRegex().find(it)
                        if(matches != null){
                            val field = matches.groups[1]!!.value
                            val arr = field.split("\\s+".toRegex())
                            var (type, name) = arr
                            name = name.trim(";")
                            var comment = if(arr.size > 2) arr[2] else ""
                            comment = comment.trim("//")
                            println("$name\t$type\t$comment")
                        }
                    }
                }

            }
        }
    }

    @Test
    fun testLog(){
        // testLogger.info("打信息日志")
        // testLogger.debug("打调试日志")
        // testLogger.error("打错误日志")

        // 去掉短信的异常
        val dir = File("/home/shi/test/szdl/logs/cn")
        val reg = "短信发送失败: null\\s\njava\\.lang\\.NullPointerException".toRegex() //
        dir.travel { file ->
            /*val txt = file.readText()
            val m = reg.find(txt)
            println(m?.value)*/
            file.replaceText {
                reg.replace(it, "")
            }
        }
    }

    /**
     * 域名检查
     */
    @Test
    fun testDomainCheck() {
        val dir = File("/home/shi/code/php/sk")
        val domainReg = "http://([\\w\\d-_\\.]+)\\.(sk(\\d)?|shikee)\\.com".toRegex() //
        val subDomains = HashSet<String>()
        dir.travel { file ->
            if (!file.name.endsWith(".php"))
                return@travel

            // 配置文件处理
            if(file.name == "shikee.php")
                return@travel

            // 收集域名
            val txt = file.readText()
            val matches = domainReg.findAll(txt)
            for(m in matches){
                subDomains.add(m.groupValues.get(1)) // 子域名
            }
        }

        val f = File(dir, "common/config/sk0.com/shikee.php")
        val txt = f.readText()
        val configReg = "domain_([\\w\\d-_]+)".toRegex()
        val matches = configReg.findAll(txt)
        val configDomains = HashSet<String>()
        for(m in matches){
            configDomains.add(m.groupValues.get(1))
        }
        println("****用到子域名****")
        println(subDomains.joinToString("\n"))
        println("****配置子域名****")
        println(configDomains.joinToString("\n"))
        println("****没有配置的子域名****")
        subDomains.removeAll(configDomains)
        println(subDomains.joinToString("\n"))

    }

    /**
     * 域名替换
     */
    @Test
    fun testDomainReplace(){
        val dir = File("/home/shi/code/php/sk")
        val domainPattern = "http://([\\w\\d-_\\.]+)\\.(sk(\\d)?|shikee)\\.com/?" // 1 子域名
        val domainReg = domainPattern.toRegex() // 1 子域名
        val assignReg = "((=|=>|\\?)\\s*)('|\")$domainPattern".toRegex()  // 1 赋值号+空格 2 赋值号 3 引号 4 子域名
        dir.travel { file ->
            if(!file.name.endsWith(".php"))
                return@travel

            // 配置文件处理
            if(file.name == "shikee.php")
                return@travel

            // view文件处理
            if(file.absolutePath.contains("/views/")) {
                /*file.replaceText { txt ->
                    domainReg.replace(txt) { result: MatchResult ->
                        val subDomain = result.groupValues.get(1)
                        "<?= config_item('domain_$subDomain') ?>"
                    };
                }*/

                return@travel
            }

            // 业务文件处理
            file.replaceText { txt ->
                assignReg.replace(txt) { result: MatchResult ->
                    val assign = result.groupValues.get(1) // 1 赋值号
                    val quote = result.groupValues.get(3) // 2 引号
                    val subDomain = result.groupValues.get(4) // 3 子域名
                    "${assign}config_item('domain_$subDomain').$quote"
                };
            }
        }
        println("over")
    }

    // 删注释
    @Test
    fun testDeleteNotes(){
        val singleReg = "(?!property\\(\\) )//.*\\n".toRegex() // 单行注释
        val multipleReg = "/\\*.+?\\*/".toRegex(setOf(RegexOption.DOT_MATCHES_ALL)) // 单行注释
        val blank2Reg = "\n\\s*\n\\s*\n".toRegex() // 双空行
        val firstReg = "\\{\\s*\n\\s*\n".toRegex() // {下的第一个空行
        /*
        var content = File("/home/shi/code/java/szpower/szpower2/src/main/kotlin/com/jkmvc/szpower/controller/AlarmController.kt").readText()
         // println(multipleReg.findAll(content).joinToString {
         //     it.value
         // })
         content = singleReg.replace(content, "\n")
         content = multipleReg.replace(content, "")
         println(content)
         */

        val dir = File("/home/shi/code/java/szpower/szpower2/src")
        dir.travel { file ->
            if(file.name.endsWith(".kt")){
                file.replaceText {
                    var content = singleReg.replace(it, "\n")
                    content = multipleReg.replace(content, "")
                    content = blank2Reg.replace(content, "\n\n")
                    firstReg.replace(content, "{\n")
                }
            }
        }
    }

}