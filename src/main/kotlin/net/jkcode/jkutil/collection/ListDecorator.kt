package net.jkcode.jkutil.collection

import net.jkcode.jkutil.common.decorateIterator

/**
 * 列表的包装器
 *    应对 list 是变化的场景, 若非如此则直接使用 list.map() 即可
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2020-1-22 4:24 PM
 */
class ListDecorator<E, R>(
        protected val list: List<E>, // 被代理的集合
        protected val transform: (E) -> R // 转换函数
): List<R> by (list as List<R>) {

    /**
     * 判断是否包含单个元素
     */
    public override fun contains(element: R): Boolean {
        return list.any {
            transform(it) == element
        }
    }
    
    /**
     * 判断是否包含多个元素
     */
    public override fun containsAll(elements: Collection<R>): Boolean {
        return elements.all { contains(it) }
    }

    override fun get(index: Int): R {
        return transform(list.get(index))
    }

    override fun indexOf(element: R): Int {
        return list.indexOfFirst {
            transform(it) == element
        }
    }

    override fun lastIndexOf(element: R): Int {
        return list.indexOfLast {
            transform(it) == element
        }
    }

    override fun listIterator(): ListIterator<R> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun listIterator(index: Int): ListIterator<R> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<R> {
        val sublist = list.subList(fromIndex, toIndex)
        return ListDecorator(sublist, transform)
    }

    /**
     * 获得迭代器
     */
    public override fun iterator(): Iterator<R> {
        return decorateIterator(list.iterator(), transform)
    }

    public override fun toString(): String{
        return joinToString(", ", "[", "]")
    }

}