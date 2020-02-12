package net.jkcode.jkutil.collection

import net.jkcode.jkutil.common.mapToArray

/**
 * 组合的列表
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2020-1-22 4:24 PM
 */
class CompositeList<E>(vararg lists: List<E>): List<E> {
    
    protected var lists: Array<List<E>>
    
    init {
        this.lists = lists as Array<List<E>>
    }
    
    override val size: Int
        get() = lists.sumBy { it.size }

    override fun contains(element: E): Boolean {
        return lists.any {
            it.contains(element)
        }
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return elements.all {
            contains(it)
        }
    }

    override fun get(index: Int): E {
        if (index < 0)
            throw IndexOutOfBoundsException("Index: $index")

        // 累计下标
        var n = 0
        // 遍历每个list来查找
        for (list in lists){
            // 找到
            if(n + list.size > index)
                return list.get(index - n)

            // 没好到, 接着找下一个
            n += list.size
        }
        throw IndexOutOfBoundsException("Index: $index, Size: $size")
    }

    override fun indexOf(element: E): Int {
        // 累计下标
        var n = 0
        // 遍历每个list来查找
        for (list in lists){
            val j = list.indexOf(element)
            // 找到
            if(j > -1)
                return n + j

            // 没好到, 接着找下一个
            n += list.size
        }
        return -1
    }

    override fun isEmpty(): Boolean {
        return lists.all { it.isEmpty() }
    }

    override fun iterator(): Iterator<E> {
        val its = lists.mapToArray {
            it.iterator()
        }
        return CompositeIterator(*its)
    }

    override fun lastIndexOf(element: E): Int {
        // 累计下标
        var n = 0
        // 遍历每个list来查找
        for (i in (lists.size - 1)..0 step -1){
            val list = lists[i]
            val j = list.lastIndexOf(element)
            // 找到
            if(j > -1)
                return n + list.size - j

            // 没好到, 接着找下一个
            n += list.size
        }
        return -1
    }

    override fun listIterator(): ListIterator<E> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun listIterator(index: Int): ListIterator<E> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<E> {
        throw UnsupportedOperationException("not implemented")
    }
}