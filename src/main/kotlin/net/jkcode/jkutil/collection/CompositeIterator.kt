package net.jkcode.jkutil.collection

import java.util.*

/**
 * 组合的迭代器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2020-1-22 4:24 PM
 */
class CompositeIterator<T>(vararg iterators: Iterator<*>) : Iterator<T> {
    
    protected val it: Iterator<Iterator<T>> = iterators.iterator() as Iterator<Iterator<T>>
    
    protected var current: Iterator<T>? = null

    override fun hasNext(): Boolean {
        // current还有
        if (current != null && current!!.hasNext())
            return true

        // 切换到下一个迭代器
        do {
            if (!it.hasNext())
                return false

            current = it.next()
        } while (!current!!.hasNext())

        return true
    }

    override fun next(): T {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        return current!!.next()
    }
}