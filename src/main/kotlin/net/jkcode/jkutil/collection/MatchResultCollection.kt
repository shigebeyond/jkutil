package net.jkcode.jkutil.collection

/**
 * 指定分组的匹配结果的集合
 */
class MatchResultCollection<String>(public val rs: Sequence<MatchResult>, public val groupIndex: Int = 0): Collection<String>{
    override val size: Int
        get() = rs.count()

    override fun contains(element: String): Boolean {
        return rs.any {
            it.groupValues[groupIndex] == element
        }
    }

    override fun containsAll(elements: Collection<String>): Boolean {
        return elements.all {
            contains(it)
        }
    }

    override fun isEmpty(): Boolean {
        return rs.iterator().hasNext()
    }

    override fun iterator(): Iterator<String> {
        return MatchResultIterator(rs)
    }

}

/**
 * 指定分组的匹配结果的迭代器
 */
class MatchResultIterator<String>(public val it: Iterator<MatchResult>, public val groupIndex: Int = 0):Iterator<String> {

    constructor(rs: Sequence<MatchResult>, groupIndex: Int = 0): this(rs.iterator(), groupIndex)

    override fun hasNext(): Boolean {
        return it.hasNext()
    }

    override fun next(): String {
        val r = it.next()
        return r.groupValues[groupIndex] as String
    }
}