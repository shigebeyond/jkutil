package net.jkcode.jkutil.iterator

import net.jkcode.jkutil.elements.ElementCollection
import net.jkcode.jkutil.elements.IElements

/**
 * 对集合进行有过滤条件有转换元素的迭代
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-21 10:23 AM
 */
abstract class CollectionFilteredTransformedIterator<E, R>(col: Collection<E> /* 目标集合 */):
        ElementsFilteredTransformedIterator<E, R>(),
        IElements<E> by ElementCollection(col) { // 通过代理 ElementCollection 来实现 IElements
}