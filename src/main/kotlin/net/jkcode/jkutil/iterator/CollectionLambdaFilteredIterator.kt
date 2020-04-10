package net.jkcode.jkutil.iterator

/**
 * 对集合进行lambda过滤的迭代
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-21 10:23 AM
 */
class CollectionLambdaFilteredIterator<E>(col: Collection<E>, protected val lambda: (E)->Boolean): CollectionFilteredIterator<E>(col){

    /**
     * 调用lambda来过滤
     */
    override fun filter(ele: E): Boolean {
        return lambda(ele)
    }

}