package net.jkcode.jkutil.scope

import java.io.Closeable
import java.util.*

/**
 * 有层级的作用域对象
 *    1. 实现该接口, 必须承诺 beginScope()/endScope()会在作用域开始与结束时调用, 一般用于初始化与销毁资源/状态, 以保证作用域内的状态干净.
 *    2. 父作用域的 beginScope()/endScope() 会自动调用子作用域的 beginScope()/endScope()
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-17 9:52 AM
 */
abstract class HierarchicalScope : IScope {

    /**
     * 子作用域
     */
    protected val childScopes: MutableList<IScope> = Vector<IScope>()

    /**
     * 添加子作用域
     * @param closing
     */
    fun addChildScope(closing: Closeable){
        addChildScope(CloseableScope(closing))
    }

    /**
     * 添加子作用域
     * @param childScope
     */
    public fun addChildScope(childScope: IScope){
        childScopes.add(childScope)
    }

    /**
     * 作用域开始
     */
    public override fun beginScope(){
        // 自身作用域开始
        doBeginScope()

        // 子作用域开始
        for(c in childScopes)
            c.beginScope()
    }

    /**
     * 作用域结束
     */
    public override fun endScope(){
        // 自身作用域结束
        doEndScope()

        // 子作用域结束
        for(c in childScopes)
            c.endScope()
    }

    /**
     * 作用域开始
     */
    public open fun doBeginScope(){
    }

    /**
     * 作用域结束
     */
    public open fun doEndScope(){
    }

}