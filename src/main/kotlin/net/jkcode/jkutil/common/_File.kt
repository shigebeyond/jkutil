package net.jkcode.jkutil.common

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.JarURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.util.*
import org.mozilla.universalchardet.Constants
import org.mozilla.universalchardet.UniversalDetector
import java.io.FileInputStream
import java.lang.IllegalStateException


/****************************** 文件大小 *******************************/
/**
 * 文件大小单位
 *   相邻单位相差1024倍
 */
private val fileSizeUnits: String = "BKMGT";

/**
 * 文件大小单位换算为字节数
 * @param unit
 * @return Int
 */
public fun fileSizeUnit2Bytes(unit: Char): Long {
    val i:Int = fileSizeUnits.indexOf(unit);
    if(i == -1)
        throw IllegalArgumentException("无效文件大小单位: $unit");

    return Math.pow(1024.0, i.toDouble()).toLong()
}

/**
 * 文件大小字符串换算为字节数
 * @param sizeStr
 * @return Int
 */
public fun fileSize2Bytes(sizeStr: String): Long {
    val size: Int = sizeStr.substring(0, sizeStr.length - 1).toInt() // 大小
    val unit: Char = sizeStr[sizeStr.length - 1] // 单位
    return size * fileSizeUnit2Bytes(unit)
}

/**
 * 字节数换算为文件大小字符串
 * @param size
 * @return
 */
public fun bytes2FileSize(size: Long): String {
    if (size <= 0)
        return "0B"

    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) +
            " " + fileSizeUnits[digitGroups]
}

/****************************** 编码 *******************************/
/**
 * 是否utf8编码
 */
public fun File.isValidUTF8(): Boolean {
    return Constants.CHARSET_UTF_8.equals(this.detectedCharset())
}

/**
 * 是否utf8编码
 */
public fun InputStream.isValidUTF8(): Boolean {
    return Constants.CHARSET_UTF_8.equals(this.detectedCharset())
}

/**
 * 识别编码
 */
public fun File.detectedCharset(): String {
    return FileInputStream(this).detectedCharset()
}

/**
 * 识别编码
 */
public fun InputStream.detectedCharset(): String {
    return this.use {
        val detector = UniversalDetector(null)
        val buf = ByteArray(4096)
        while (true) {
            val nread = this.read(buf)
            if (nread == 0 || detector.isDone())
                break

            detector.handleData(buf, 0, nread)
        }
        detector.dataEnd()

        detector.getDetectedCharset()
    }

}

/****************************** 文件路径 *******************************/
/**
 * 判断是否是绝对路径
 * @param path
 * @return
 */
public val String.isAbsolutePath: Boolean
    get(){
        return startsWith("/") || indexOf(":\\") > 0;
    }

/**
 * 准备目录
 */
public fun String.prepareDirectory(){
    val dir = File(this)
    if(!dir.exists())
        dir.mkdirs()
}

/**
 * 尝试创建文件, 如果已有同名文件, 则重命名为新文件, 加计数后缀
 * @return
 */
public fun File.createOrRename(): File {
    if (this.createNewFileSafely())
        return this

    val name = this.name
    val body: String
    val ext: String
    val dot = name.lastIndexOf(".")
    if (dot != -1) {
        body = name.substring(0, dot)
        ext = name.substring(dot)
    } else {
        body = name
        ext = ""
    }

    var count = 0
    var newfile: File
    do {
        count++
        val newName = body + count + ext
        newfile = File(this.parent, newName)
    }while (!newfile.createNewFileSafely() && count < 9999)

    return newfile
}

/**
 * 无异常的创建新文件
 * @return
 */
private fun File.createNewFileSafely(): Boolean {
    try {
        return createNewFile()
    } catch (ignored: IOException) {
        return false
    }
}

/****************************** 文本处理 *******************************/
/**
 * 整个文件替换文本内容
 *
 * @param transform 文本转换lambda
 */
public fun File.replaceText(transform:(txt: String) -> String){
    val txt = this.readText()
    val newTxt = transform(txt)
    this.writeText(newTxt)
}

/****************************** 文件遍历 *******************************/
/**
 * 遍历文件
 *   使用栈来优化
 * @param action 访问者函数
 */
public fun File.travel(action:(file: File) -> Unit) {
    val files: Stack<File> = Stack()
    files.push(this)
    travelFiles(files, action)
}

/**
 * 遍历文件
 * @param files 文件栈
 * @param action 访问者函数
 */
public fun travelFiles(files: Stack<File>, action:(file: File) -> Unit) {
    while (!files.isEmpty()){
        val file = files.pop();
        if(file.isDirectory)
            files.addAll(file.listFiles())
        else
            action(file)
    }
}

/****************************** URL遍历 *******************************/
// 文件分隔符
private val SEP = File.separatorChar;

// jar url协议的正则
private val jarUrlProtocol = "jar|zip|wsjar|code-source".toRegex()

/**
 * url是否是jar包
 */
public fun URL.isJar(): Boolean {
    return jarUrlProtocol.matches(protocol)
}

/**
 * 获得根资源
 * @return
 */
public fun ClassLoader.getRootResource(): URL {
    val res = getResource("/") // web环境
    if(res != null)
        return res
    return getResource(".") // cli环境
}

/**
 * 获得根目录
 * @return
 */
public fun ClassLoader.getRootPath(): String {
    var root = getRootResource().path
    // println("classLoader根目录：" + root)
    // println("当前目录：" + path)

    /**
     * fix bug: window下路径对不上
     * classLoader根目录：/C:/Webclient/tomcat0/webapps/ROOT/WEB-INF/classes/
     * 文件绝对路径：      C:\Webclient\tomcat0\webapps\ROOT\WEB-INF\classes\com\jkmvc\szpower\controller\WorkInstructionController.class
     * 文件相对路径=文件绝对路径-跟路径：om\jkmvc\szpower\controller\WorkInstructionController.class
     *
     * => classLoader根目录开头多了一个/符号， 同时分隔符变为/（linux的分隔符）
     */
    if(JkApp.isWin && root.startsWith('/')){
        root = root.substring(1)
        root = root.replace('/', SEP)
    }
    return root
}

/**
 * 遍历url中的资源
 * @param action 访问者函数
 */
public fun URL.travel(action:(relativePath:String, isDir:Boolean) -> Unit){
    if(isJar()){ // 遍历jar
        val conn = openConnection() as JarURLConnection
        val jarFile = conn.jarFile
        for (entry in jarFile.entries()){
            val isDir = entry.name.endsWith(SEP)
            action(entry.name, isDir);
        }
    }else{ // 遍历目录
        val rootPath = Thread.currentThread().contextClassLoader.getRootPath()
        File(path).travel {
            // 文件相对路径
            val relativePath = getResourceRelativePath(it.path, rootPath)
            // println("文件相对路径：" + relativePath)
            action(relativePath, it.isDirectory)
        }
    }
}

/**
 * 获得资源的相对路径
 * @param absolutePath 资源的绝对路径
 * @param rootPath 根目录
 * @return
 */
private fun getResourceRelativePath(absolutePath: String, rootPath: String): String {
    // println("文件绝对路径：" + absolutePath)
    // 1 同一个工程下
    if (absolutePath.startsWith(rootPath))
        return absolutePath.substring(rootPath.length)

    // 2 其他工程下（兄弟工程/子工程）
    /**
     * 模式1： idea2018直接运行类， 编译输出目录为 out
     * fix bug: 运行main()或单元测试时路径对不上
     * classLoader根目录: /home/shi/code/java/java/jksoa/jksoa-rpc/jksoa-rpc-client/out/test/classes/
     * 文件绝对路径:       /home/shi/code/java/java/jksoa/jksoa-rpc/jksoa-rpc-client/out/production/classes/com/jksoa/example/SimpleService.class
     * 参考: ClientTests.testScanClass()
     *
     * classLoader根目录: /home/shi/code/java/jkjob/out/test/classes/
     * 文件绝对路径:       /home/shi/code/java/jksoa/jksoa-common/out/production/classes/com/jksoa/example/ISimpleService$DefaultImpls.class
     * 参考: JobTests.testRpcJob()
     *
     * classLoader根目录: /home/shi/code/java/jksoa/jksoa-dtx/jksoa-dtx-demo/jksoa-dtx-order/out/production/classes/
     * 文件绝对路径: /home/shi/code/java/jksoa/jksoa-rpc/jksoa-rpc-server/out/production/classes/net/jkcode/jksoa/rpc/example/SimpleService.class
     * 参考：JettyServerLauncher 运行在项目jksoa-dtx-order上
     *
     * => 模式是： out/production/classes/ 或 out/test/classes/, 直接取后续部分
     */

    /**
     * 模式2： gradle运行， 编译输出目录是 classes
     * fix bug: 运行gretty时路径对不上, 主要是启动gretty时连带启动rpc server
     * classLoader根目录: /home/shi/code/java/jksoa/jksoa-dtx/jksoa-dtx-demo/jksoa-dtx-order/build/classes/kotlin/main/
     * 文件绝对路径:       /home/shi/code/java/jksoa/jksoa-rpc/jksoa-rpc-server/build/classes/kotlin/main/net/jkcode/jksoa/rpc/example/SimpleService.class
     * 参考: gradle :jksoa-dtx:jksoa-dtx-demo:jksoa-dtx-order:appRun
     *
     * => 模式是： build/classes/kotlin/main/ 或 build/classes/java/main/, 直接取后续部分
     */

    /**
    * 模式3： idea2020直接运行类， 编译输出目录为 classes
    * classLoader根目录: /home/shi/code/java/jkerp/consoleweb/build/classes/java/main/
    * 文件绝对路径:       /home/shi/code/java/jkerp/consoleweb/build/classes/kotlin/main/net/jkcode/jkerp/apps/app/controller/SettingWebController.class
    * 参考: jkerp运行JettyServerLauncher
    *
    * => 模式是： 根目录是 build/classes/java/main/, 代码目录却是 build/classes/kotlin/main/
    */
    for (delimiter in relativePathDelimiters){
        val i = absolutePath.indexOf(delimiter)
        if(i != -1)
            return absolutePath.substring(i + delimiter.length)
    }
    throw IllegalStateException("Cannot determite relative path for resource: " + absolutePath)
}

// 资源的相对路径的分隔符
private val relativePathDelimiters = arrayOf(
        "classes${SEP}java${SEP}main${SEP}",
        "classes${SEP}kotlin${SEP}main${SEP}",
        "classes${SEP}scala${SEP}main${SEP}",
        "classes${SEP}clojure${SEP}main${SEP}",
        "classes${SEP}groovy${SEP}main${SEP}",
        "classes${SEP}jython${SEP}main${SEP}",
        "classes${SEP}jruby${SEP}main${SEP}",
        "classes${SEP}"
);
