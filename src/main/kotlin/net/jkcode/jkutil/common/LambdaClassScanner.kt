package net.jkcode.jkutil.common

import java.util.*

/**
 * 用lambda封装对扫描到的类文件的处理
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-23 7:56 PM
 */
class LambdaClassScanner(
        public override val packages: MutableCollection<String> = LinkedList(),
        protected val lambda: (String) -> Unit
) : ClassScanner() {

    constructor(`package`: String, lambda: (String) -> Unit): this(mutableListOf(`package`), lambda)

    /**
     * 收集类文件
     *
     * @param relativePath 类文件相对路径
     */
    override fun collectClass(relativePath: String) {
        lambda.invoke(relativePath)
    }

}