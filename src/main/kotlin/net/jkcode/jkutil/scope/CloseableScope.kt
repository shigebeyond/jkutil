package net.jkcode.jkutil.scope

import java.io.Closeable

/**
 * 将 Closeable 转为 IScope
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-18 9:24 AM
 */
class CloseableScope(val closing: Closeable): IScope {

    /**
     * 作用域开始
     */
    public override fun beginScope() {
    }

    /**
     * 作用域结束
     */
    public override fun endScope() {
        closing.close()
    }


}