package net.jkcode.jkutil.iterator

/**
 * 包装只有一个next方法的对象, 成为迭代器
 * @author shijianhang<772910474@qq.com>
 * @date 2020-10-10 10:23 AM
 */
abstract class NextMethodIterator<T>: Iterator<T> {

    /**
     * 是否初始化
     */
    protected var _inited = false

    /**
     * 下一个元素
     */
    //protected var _next: T? = callNext() // callNext()中依赖的属性, 可能在子类(ZipFileIterator)构造函数中赋值, 延后于当前类构造函数执行, 因此会报 NullPointerException 异常
    protected var _next: T? = null

    /**
     * 迭代开始前置事件
     */
    open fun beforeBegin(){}

    /**
     * 迭代结束后置事件
     */
    open fun afterEnd(){}

    /**
     * 调用next方法
     */
    abstract fun callNext(): T?

    override fun hasNext(): Boolean {
        // 迭代开始
        if(!_inited)
            beforeBegin()

        // 第一次或之前有值, 则可获得下一个值
        if(!_inited || _next != null)
            _next = callNext()

        if(!_inited)
            _inited = true

        val ret = _next != null

        // 无下一个, 则迭代结束
        if(!ret)
            afterEnd()

        return ret
    }

    override fun next(): T {
        if(_next === null)
            throw NoSuchElementException()

        return _next!!
    }
}