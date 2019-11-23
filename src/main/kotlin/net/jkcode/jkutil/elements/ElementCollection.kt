package net.jkcode.jkutil.elements

import net.jkcode.jkutil.common.get

/**
 * 元素集合
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-21 10:58 AM
 */
class ElementCollection<E>(protected val col: Collection<E>): IElements<E> {

    public override fun size(): Int {
        return col.size
    }

    public override fun getElement(index: Int): E {
        return col[index]
    }

}