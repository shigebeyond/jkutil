package net.jkcode.jkutil.common

import net.jkcode.jkutil.collection.CollectionDecorator
import net.jkcode.jkutil.collection.SetDecorator
import net.jkcode.jkutil.iterator.NextMethodIterator
import org.apache.commons.collections.iterators.AbstractIteratorDecorator
import java.math.BigDecimal
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentMap
import kotlin.collections.HashSet


/****************************** 扩展 Array + Collection *****************************/

private val iarr = IntArray(0)
/**
 * 空数组
 * @return
 */
public fun emptyIntArray(): IntArray {
    return iarr
}

/**
 * 清空数据
 */
public fun Array<Any?>.clear() {
    for(i in 0 until size)
        this[i] = null
}

/**
 * 判断为空
 */
public fun ByteArray?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}

/**
 * 非空参数转为array, 仅用于在 DbKey/Orm 的构造函数中转参数
 * @param params
 * @return
 */
public inline fun <T> toArray(vararg params:T): Array<T> {
    return params as Array<T>
}

/**
 * 构建重复元素的数组
 *
 * @param times 重复次数
 * @return
 */
public fun Any.repeateToArray(times: Int): Array<Any> {
    return (1..times).mapToArray { this }
}

/**
 * 构建重复元素的列表
 *
 * @param times 重复次数
 * @return
 */
public fun Any.repeateToList(times: Int): List<Any> {
    return (1..times).map { this }
}

/**
 * 是否数组
 * @return
 */
public fun Any.isArray(): Boolean {
    //return this is Array<*> || this is IntArray || this is ShortArray || this is LongArray || this is FloatArray || this is DoubleArray || this is BooleanArray
    return this.javaClass.isArray()
}

/**
 * 是否数组或集合
 * @return
 */
public fun Any?.isArrayOrCollection(): Boolean {
    return this != null && (this.isArray() || this is Collection<*>)
}

/**
 * 是否数组为空
 * @return
 */
public fun Any.isArrayEmpty(): Boolean {
    if(this is Array<*>)
        return this.isEmpty()

    if(this is IntArray)
        return this.isEmpty()

    if(this is ShortArray)
        return this.isEmpty()

    if(this is LongArray)
        return this.isEmpty()

    if(this is FloatArray)
        return this.isEmpty()

    if(this is DoubleArray)
        return this.isEmpty()

    if(this is BooleanArray)
        return this.isEmpty()

    return false
}

/**
 * 是否数组或集合为空
 * @return
 */
public fun Any.isArrayOrCollectionEmpty(): Boolean {
    if(isArrayEmpty())
        return true

    if(this is Collection<*>)
        return this.isEmpty()

    return false
}

/**
 * 获得数组或集合的迭代器
 * @return
 */
public fun Any.iteratorArrayOrCollection(): Iterator<*>? {
    if(this is Array<*>)
        return this.iterator()

    if(this is IntArray)
        return this.iterator()

    if(this is ShortArray)
        return this.iterator()

    if(this is LongArray)
        return this.iterator()

    if(this is FloatArray)
        return this.iterator()

    if(this is DoubleArray)
        return this.iterator()

    if(this is BooleanArray)
        return this.iterator()

    if(this is Collection<*>)
        return this.iterator()

    return null
}

/**
 * 检查集合是否为空
 * @return
 */
public fun <E> Collection<E>?.isNullOrEmpty(): Boolean {
    return this === null || this.isEmpty()
}

/**
 * 检查集合中的元素是否相同
 * @return
 */
public fun <E> Collection<E>.isSame(): Boolean {
    if(this.isEmpty())
        return false

    var ele: E? = null
    return this.all {
        if(ele == null){
            ele = it
            true
        }else
            ele == it
    }
}

/**
 * Returns the element at the specified position in this collection.
 *
 * @param index index of the element to return
 * @return the element at the specified position in this collection
 * @throws IndexOutOfBoundsException if the index is out of range
 * (<tt>index &lt; 0 || index &gt;= size()</tt>)
 */
public operator fun <E> Collection<E>.get(index: Int): E {
    if(index < 0 || index > size)
        throw IndexOutOfBoundsException("Index: $index, Size: $size")

    // list
    if(this is List)
        return this[index]

    // 非list
    var i = 0
    for (item in this){
        if(i++ == index)
            return item
    }

    // 不可达
    throw Exception("Unreachable code")
}

/**
 * Returns a list containing only the non-null/blank string results of applying the given [transform] function
 * to each element in the original collection.
 */
public inline fun <T> Iterable<T>.mapNotNullOrBlankString(transform: (T)->String?): List<String> {
    val result = ArrayList<String>()
    forEach { element ->
        val str = transform(element)
        if(!str.isNullOrBlank())
            result.add(str)
    }
    return result
}

/**
 * Returns a list containing only the non-null/blank string in the original collection.
 */
public inline fun Iterable<String?>.filterNotNullOrBlankString(): List<String> {
    return this.filter {
        !it.isNullOrBlank()
    } as List<String>
}

/**
 * 迭代器转数组
 *   注: Array<R> 不能使用R作为泛型参数, 只能使用具体类
 */
public inline fun <E, reified R> Iterator<E>.mapToArray(size:Int, transform: (E) -> R): Array<R> {
    val arr = arrayOfNulls<R?>(size);
    var i = 0;
    for (item in this)
        arr[i++] = transform(item)
    return arr as Array<R>
}

/**
 * 集合转数组
 *   注: Array<R> 不能使用R作为泛型参数, 只能使用具体类
 */
public inline fun <E, reified R> Collection<E>.mapToArray(transform: (E) -> R): Array<R> {
    val arr = arrayOfNulls<R?>(this.size);
    var i = 0;
    for (item in this)
        arr[i++] = transform(item)
    return arr as Array<R>
}

/**
 * 数组转数组
 *   注: Array<R> 不能使用R作为泛型参数, 只能使用具体类
 */
public inline fun <T, reified R> Array<T>.mapToArray(transform: (T) -> R): Array<R> {
    val arr = arrayOfNulls<R?>(this.size);
    var i = 0;
    for (item in this)
        arr[i++] = transform(item)
    return arr as Array<R>
}

/**
 * 数组转数组
 *   注: Array<R> 不能使用R作为泛型参数, 只能使用具体类
 */
public inline fun <T, reified R> Array<T>.mapIndexedToArray(transform: (i: Int, T) -> R): Array<R> {
    val arr = arrayOfNulls<R?>(this.size);
    var i = 0;
    for (item in this) {
        arr[i] = transform(i, item)
        i++
    }
    return arr as Array<R>
}

// 空选项
public val emptyOption = toOptionMap("", "")

/**
 * 封装为选项的map
 * @param value
 * @param label
 * @return
 */
public inline fun toOptionMap(value: String, label: String, group: String? = null): Map<String, String> {
    val map = mutableMapOf(
            "value" to value,
            "label" to label
    )

    if(group != null)
        map["grouping"] = group
    return map
}

/**
 * 添加选项的map
 * @param value
 * @param label
 * @return
 */
public inline fun MutableCollection<Map<String, String>>.addOptionMap(value: String, label: String){
    this.add(toOptionMap(value, label))
}

/**
 * 对象列表转为选项列表
 * @param withEmpty 是否带空选项
 * @param transform 转换函数
 * @return
 */
public inline fun <T> Collection<T>.toOptions(withEmpty: Boolean = false, transform: (T) -> Pair<String, String>?): List<Map<String, String>> {
    if(this.isEmpty())
        return if(withEmpty) listOf(emptyOption) else emptyList()

    val result = ArrayList<Map<String, String>>()
    if(withEmpty)
        result.add(emptyOption)
    return toOptions(result, transform)
}

/**
 * 对象列表转为选项列表
 * @param list 要存储的结果
 * @param transform 转换函数
 * @return
 */
public inline fun <T> Collection<T>.toOptions(list: MutableList<Map<String, String>>, transform: (T) -> Pair<String, String>?): List<Map<String, String>> {
    return this.mapNotNullTo(list) {
        val o = transform(it)
        if(o == null)
            null
        else
            toOptionMap(o.first, o.second)
    }
}



/**
 * 对象列表转为分组选项列表
 * @param withEmpty 是否带空选项
 * @param transform 转换函数
 * @return
 */
public inline fun <T> Collection<T>.toGroupOptions(withEmpty: Boolean = false, transform: (T) -> Triple<String, String, String>?): List<Map<String, String>> {
    if(this.isEmpty())
        return if(withEmpty) listOf(emptyOption) else emptyList()

    val result = ArrayList<Map<String, String>>()
    if(withEmpty)
        result.add(emptyOption)
    return toGroupOptions(result, transform)
}

/**
 * 对象列表转为分组选项列表
 * @param list 要存储的结果
 * @param transform 转换函数
 * @return
 */
public inline fun <T> Collection<T>.toGroupOptions(list: MutableList<Map<String, String>>, transform: (T) -> Triple<String, String, String>?): List<Map<String, String>> {
    return this.mapNotNullTo(list) {
        val o = transform(it)
        if(o == null)
            null
        else
            toOptionMap(o.first, o.second, o.third)
    }
}

/**
 * 获得数组的某个元素值，如果值为空，则给该元素赋值
 * @param index 元素索引
 * @param default 赋值回调
 * @return
 */
public inline fun <T> Array<T>.getOrPut(index: Int, defaultValue: (Int) -> T): T {
    if(this[index] == null)
        this[index] = defaultValue(index)
    return this[index];
}

/**
 * 统计个数
 */
public inline fun <T, K> Iterable<T>.groupCount(counter: MutableMap<K, Int> = HashMap(), keySelector: (T) -> K): Map<K, Int> {
    for (element in this) {
        val key = keySelector(element)
        val count = counter[key] ?: 0
        counter[key] = count + 1
    }
    return counter
}


/**
 * 包含
 */
public infix fun <T> Collection<T>.containsAny(c: Collection<T>): Boolean {
    return this.any {
        c.contains(it)
    }
}

/**
 * 添加另外一个集合的转换后的元素
 * @param c
 * @param transform 元素转换函数
 */
public fun <R, T> MutableCollection<R>.addAllMap(c: Collection<T>, transform: (T) -> R){
    for(e in c){
        this.add(transform(e))
    }
}

/****************************** progression *****************************/
/**
 * 大小
 */
public val IntProgression.size: Int
    get() = (last - first) / step + 1

/**
 * 大小
 */
public val LongProgression.size: Int
    get() = ((last - first) / step + 1).toInt()

/**
 * IntProgression转数组
 *   注: Array<R> 不能使用R作为泛型参数, 只能使用具体类
 */
public inline fun <reified R> IntProgression.mapToArray(transform: (Int) -> R): Array<R> {
    val arr = arrayOfNulls<R?>(this.size);
    var i = 0;
    for (item in this)
        arr[i++] = transform(item)
    return arr as Array<R>
}

/**
 * LongProgression转数组
 *   注: Array<R> 不能使用R作为泛型参数, 只能使用具体类
 */
public inline fun <reified R> LongProgression.mapToArray(transform: (Long) -> R): Array<R> {
    val arr = arrayOfNulls<R?>(this.size);
    var i = 0;
    for (item in this)
        arr[i++] = transform(item)
    return arr as Array<R>
}

/****************************** query string *****************************/
/**
 * 请求参数转query string
 * @param buffer
 * @return
 */
public fun Map<String, Array<String>>.toQueryString(buffer: StringBuilder): StringBuilder {
    entries.joinTo(buffer, "") {
        "${it.key}=${it.value.first()}&"
    }
    return buffer.deleteSuffix("&")
}

/**
 * 请求参数转query string
 * @param buffer
 * @return
 */
public fun Map<String, Array<String>>.toQueryString(): String {
    if(this.isEmpty())
        return ""

    return toQueryString(StringBuilder()).toString()
}

/****************************** 扩展 Iterator *****************************/
/**
 * Returns a list containing the results of applying the given [transform] function
 * to each element in the original collection.
 */
public inline fun <T, R> Iterator<T>.map(transform: (T) -> R): List<R> {
    return mapTo(ArrayList<R>(), transform)
}

/**
 * Applies the given [transform] function to each element of the original collection
 * and appends the results to the given [destination].
 */
public inline fun <T, R, C : MutableCollection<in R>> Iterator<T>.mapTo(destination: C, transform: (T) -> R): C {
    for (item in this)
        destination.add(transform(item))
    return destination
}

/**
 * Creates a string from all the elements separated using [separator] and using the given [prefix] and [postfix] if supplied.
 *
 * If the collection could be huge, you can specify a non-negative value of [limit], in which case only the first [limit]
 * elements will be appended, followed by the [truncated] string (which defaults to "...").
 *
 * @sample samples.collections.Collections.Transformations.joinToString
 */
public fun <T> Iterator<T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): String {
    return joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
}

/**
 * Appends the string from all the elements separated using [separator] and using the given [prefix] and [postfix] if supplied.
 *
 * If the collection could be huge, you can specify a non-negative value of [limit], in which case only the first [limit]
 * elements will be appended, followed by the [truncated] string (which defaults to "...").
 *
 * @sample samples.collections.Collections.Transformations.joinTo
 */
public fun <T> Iterator<T>.joinTo(buffer: StringBuilder, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): StringBuilder {
    buffer.append(prefix)
    var count = 0
    for (element in this) {
        if (++count > 1) buffer.append(separator)
        if (limit < 0 || count <= limit) {
            val value = if(transform == null) element else transform(element)
            buffer.append(value)
        } else break
    }
    if (limit >= 0 && count > limit) buffer.append(truncated)
    buffer.append(postfix)
    return buffer
}

/**
 * 转字符串
 * @return
 */
public fun Iterator<*>.toDesc(): String {
    return this.joinToString(", ",javaClass.name + "(", ")")
}

/**
 * Returns `true` if all elements match the given [predicate].
 */
public inline fun <T> Enumeration<T>.all(predicate: (T) -> Boolean): Boolean {
    for (element in this)
        if (!predicate(element))
            return false
    return true
}

/**
 * Returns `true` if collection has at least one element.
 */
public fun <T> Enumeration<T>.any(): Boolean {
    return iterator().hasNext()
}

/**
 * Returns `true` if at least one element matches the given [predicate].
 */
public inline fun <T> Enumeration<T>.any(predicate: (T) -> Boolean): Boolean {
    for (element in this) if (predicate(element))
        return true
    return false
}

/**
 * Enumeration相减
 * Returns a set containing all elements that are contained by this Enumeration and not contained by the specified Enumeration.
 * The returned set preserves the element iteration order of the original collection.
 */
public infix fun <T> Enumeration<T>.subtract(other: Enumeration<T>): Set<T> {
    // bug: 由于Enumeration类似于迭代器, 只能用一次迭代, 因此不能直接在循环中多次迭代来处理
    /*val set = HashSet<T>()
    for(e in this){
        if(!other.any{e == it}) // wrong: 只能用一次迭代
            set.add(e)
    }
    return set*/
    // fix: 将要多次迭代的Enumeration放到list中, list可以多次迭代
    val c = other.toList()
    return this.subtract(c)
}

public infix fun <T> Enumeration<T>.subtract(other: Collection<T>): Set<T> {
    val set = HashSet<T>()
    for(e in this){
        if(!other.contains(e))
            set.add(e)
    }
    return set
}

/**
 * 遍历枚举
 * @param action
 */
public fun <T> Enumeration<T>.forEach(action: (T) -> Unit){
    for (element in this) {
        action(element)
    }
}

/**
 * 转为map
 * @param transform
 * @return
 */
public fun <T, K, V> Enumeration<T>.associate(transform: (T) -> Pair<K, V>): Map<K, V> {
    return associateTo(LinkedHashMap<K, V>(), transform)
}

/**
 * 转为map
 * @param destination
 * @param transform
 */
public inline fun <T, K, V, M : MutableMap<in K, in V>> Enumeration<T>.associateTo(destination: M, transform: (T) -> Pair<K, V>): M {
    for (element in this) {
        destination += transform(element)
    }
    return destination
}

/**
 * Iterator转Enumeration
 */
class ItEnumeration<T>(val it: Iterator<T>) : Enumeration<T> {

    override fun hasMoreElements(): Boolean{
        return it.hasNext()
    }

    override fun nextElement(): T {
        return it.next();
    }
}

/**
 * 获得Enumeration
 * @return
 */
public fun <T> Iterable<T>.enumeration(): ItEnumeration<T> {
    return ItEnumeration(iterator())
}

/**
 * 包装迭代器
 * @param iterator 被包装的迭代器
 * @param 元素转换器
 * @return 新的迭代器
 */
public fun <T, R> decorateIterator(iterator: Iterator<T>, transform: (T) -> R): Iterator<R> {
    return object: AbstractIteratorDecorator(iterator){
        override fun next(): Any? {
            val ele = super.next() as T
            return transform.invoke(ele)
        }
    } as Iterator<R>
}

/**
 * 包装只有一个next方法的对象成为迭代器
 * @param nextMethod next方法的回调
 * @return 新的迭代器
 */
public fun <R> decorateNextMethodIterator(nextMethod: () -> R?): Iterator<R> {
    return object: NextMethodIterator<R>(){
        override fun callNext(): R? {
            return nextMethod()
        }
    }
}

/**
 * 包装集合
 * @param col 被包装的集合
 * @param 元素转换器
 * @return 新的迭代器
 */
public fun <T, R> decorateCollection(col: Collection<T>, transform: (T) -> R): Collection<R> {
    return CollectionDecorator(col, transform)
}

/**
 * 包装Set
 * @param set 被包装的Set
 * @param 元素转换器
 * @return 新的迭代器
 */
public fun <T, R> decorateSet(set: Set<T>, transform: (T) -> R): Set<R> {
    return SetDecorator(set, transform)
}

/**
 * Returns a list containing the results of applying the given [transform] function
 * to each element in the original set.
 */
public inline fun <T, R> Iterable<T>.mapToSet(transform: (T) -> R): Set<R> {
    return mapTo(HashSet<R>(), transform)
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the collection.
 */
public inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum: Long = 0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

/****************************** 扩展 Queue *****************************/

/**
 * 从队列中抽取指定数目的元素
 *    注意: ConcurrentLinkedQueue 中元素不能为null, 同时size() 是遍历性能慢, 尽量使用 isEmpty()
 *
 * @param c
 * @param maxElements
 * @return
 */
public fun <E> ConcurrentLinkedQueue<E>.drainTo(c: MutableCollection<E>, maxElements: Int): Int {
    var n = 0
    for(i in 0 until maxElements){
        // 元素出队
        val item = this.poll()
        // 如元素为null, 则队列为空
        if(item == null)
            break;

        // 记录出队元素
        c.add(item)
        n++
    }
    return n
}

/**
 * 逐个出队元素, 并访问
 * @param action 访问的回调
 * @return
 */
public fun <T> Queue<T>.pollEach(action: (T) -> Unit){
    var t: T = poll()
    while(t != null){
        action.invoke(t)
        t = poll()
    }
}

/****************************** 扩展 Map *****************************/
/**
 * 获得'.'分割的路径下的属性值
 *
 * @param path '.'分割的路径
 * @return
 */
public fun Map<*, *>.getPath(path: String): Any? {
    return PropertyUtil.getPath(this, path)
}

/**
 * 获得map的某个值，如果值为空，则返回默认值
 * @param key 键名
 * @param default 默认值
 * @return
 */
public inline fun <K, V> Map<K, V>?.getOrDefault(key:K, default:V? = null): V? {
    val value = this?.get(key)
    return if(value == null)
        default
    else
        value;
}

/**
 * 改进 getOrPut(), 其中 defaultValue() 只调用一次, 用于减少大对象与资源(如db连接)创建的情况
 */
public inline fun <K, V> ConcurrentMap<K, V>.getOrPutOnce(key: K, defaultValue: () -> V): V {
    return this.get(key) ?:
                synchronized(this){
                    this.get(key) ?:
                        defaultValue().let { default -> this.putIfAbsent(key, default) ?: default }
                }
}

/**
 * 获得map的某个值，并转换为指定类型
 * @param key 键名
 * @param default 默认值
 * @return
 */
public inline fun <reified T:Any>  Map<*, *>.getAndConvert(key:String, defaultValue:T? = null): T? {
    val value = get(key)
    // 默认值
    if(value === null)
        return defaultValue
    // 不用转换
    if(value is T)
        return value
    // 要转换
    if(value is String)
        return value.to(T::class)

    throw ClassCastException("Fail to convert [$value] to type [${T::class}]")
}

/**
 * 检查是否包含任一个键
 *
 * @param keys
 * @return
 */
public fun Map<*, *>.containsAnyKey(vararg keys: Any): Boolean {
    for(key in keys)
        if(this.containsKey(key))
            return true

    return false
}

/**
 * 检查是否包含所有键
 *
 * @param keys
 * @return
 */
public fun Map<*, *>.containsAllKeys(vararg keys: Any): Boolean {
    for(key in keys)
        if(!this.containsKey(key))
            return false

    return true
}

/**
 * 合并map, 只合并不存在的key/value
 * @param map
 */
public fun  <K, V> MutableMap<K, V>.putAllIfAbsent(map: Map<K, V>){
    for ((key, value) in map)
        this.putIfAbsent(key, value)
}

/**
 * 根据条件删除map中的项目
 * @param predicate 删除单项的条件
 */
public inline fun <K, V> MutableMap<out K, V>.removeBy(predicate: (Map.Entry<K, V>) -> Boolean) {
    val nit = this.entries.iterator()
    while (nit.hasNext()) {
        val entry = nit.next()
        if (predicate(entry))
            nit.remove()
    }
}

/**
 * map删除多个key
 * @param keys
 * @return
 */
public fun  <K, V> MutableMap<K, V>.removeAll(keys: Collection<K>): MutableMap<K, V> {
    for (key in keys)
        remove(key)
    return this
}

/**
 * Returns a [Map] containing key-value pairs provided by [transform] function
 * applied to elements of the given array.
 *
 * If any of two pairs would have the same key the last one gets added to the map.
 */
public inline fun <K, V> Map<*, *>.associate(transform: (Map.Entry<*, *>) -> Pair<K, V>): MutableMap<K, V> {
    val result:MutableMap<K, V> = HashMap<K, V>();
    for(e in this)
        result += transform(e)
    return result;
}

/**
 * Returns a [Map] containing key-value pairs provided by [transform] function
 * applied to elements of the given array.
 *
 * If any of two pairs would have the same key the last one gets added to the map.
 */
public inline fun <A, B, K, V> Map<A, B>.associate(transform: (key: A, value: B) -> Pair<K, V>): MutableMap<K, V> {
    val result:MutableMap<K, V> = HashMap<K, V>();
    for((key, value) in this)
        result += transform(key, value)
    return result;
}

/**
 * Returns `true` if at least one entry matches the given [predicate].
 *
 * @sample samples.collections.Collections.Aggregates.anyWithPredicate
 */
public inline fun <K, V> Map<out K, V>.any(predicate: (key: K, value: V) -> Boolean): Boolean {
    if (isEmpty()) return false
    for ((key, value) in this) if (predicate(key, value)) return true
    return false
}

/**
 * 收集某列的值
 *
 * @param key
 * @return
 */
public fun <K, V> Collection<Map<K, V>>.collectColumn(key:K):Collection<V>{
    return this.map {
        it[key]
    } as Collection<V>
}

/**
 * map列表转哈希
 *
 * @param keyField 子项字段名，其值作为结果哈希的key
 * @param valueField 子项字段名，其值作为结果哈希的value，如果为null，则用子项作为结果哈希的value
 * @return
 */
public fun Collection<out Map<*, *>>.toMap(keyField:String, valueField:String? = null): Map<*, *> {
    if(this.isEmpty())
        return emptyMap<Any, Any>()

    return this.associate {
        // key to value
        it[keyField]!! to (if(valueField == null) it else it[valueField])
    }
}

/**
 * 将参数转为查询字符串
 * @param str
 * @param encoding 是否编码
 * @return
 */
public fun Map<*, *>.buildQueryString(str: StringBuilder, encoding: Boolean = false): StringBuilder {
    // fix: 不能处理value是数组的情况
    /*return entries.joinTo(str, "&") {
        var key = it.key.toString()
        var value = it.value.toString()
        if(encoding){
            key = URLEncoder.encode(key, "UTF-8")
            value = URLEncoder.encode(value, "UTF-8")
        }
        "$key=$value"
    }*/
    for((key, value) in this){
        if(value is Array<*>){ // value是多值
            for(v in value)
                addQueryItem(str, key, v, encoding)
        }else { // value是单值
            addQueryItem(str, key, value, encoding)
        }
    }
    return str
}

/**
 * 为查询字符串添加单个键值
 */
private fun addQueryItem(str: StringBuilder, key: Any?, value: Any?, encoding: Boolean){
    var key2 = key.toString()
    var value2 = value?.toString() ?: ""
    if (encoding) {
        key2 = URLEncoder.encode(key2, "UTF-8")
        value2 = URLEncoder.encode(value2, "UTF-8")
    }
    str.append(key2).append("=").append(value2).append("&")
}

/**
 * 将参数转为查询字符串
 * @param str
 * @param encoding 是否编码
 * @return
 */
public fun Map<*, *>.buildQueryString(encoding: Boolean = false): String {
    if(this.isEmpty())
        return ""
    return buildQueryString(StringBuilder(), encoding).toString()
}

/********************** 扩展List *********************/
/**
 * Returns a view of the portion of this list between the specified [fromIndex] (inclusive) and [toIndex] (exclusive).
 * The returned list is backed by this list, so non-structural changes in the returned list are reflected in this list, and vice-versa.
 *
 * Structural changes in the base list make the behavior of the view undefined.
 */
public fun <E> List<E>.subListSafe(fromIndex0: Int, toIndex0: Int): List<E>{
    val fromIndex = if(fromIndex0 < 0) 0 else fromIndex0
    val toIndex = if(toIndex0 > this.size) this.size else toIndex0
    if(fromIndex >= toIndex)
        return emptyList()

    return this.subList(fromIndex, toIndex)
}

/**
 * 将自己的元素给转变了，还是存在自己list中
 *   主要是为了复用list, 优化map性能, 特别是调用频繁或list很大的场景
 */
public inline fun <T, R> List<T>.mapSelf(transform: (T) -> R): List<R> {
    var list = (this as List<*>) as MutableList<R>
    for(i in 0 until this.size){
        list[i] = transform(this[i])
    }
    return list
}

/********************** 转为各种类型的Map<String, *>.getter *********************/
/**
 * Get attribute of db type: varchar, char, enum, set, text, tinytext, mediumtext, longtext
 */
fun Map<String, *>.getString(name: String): String {
    return this[name] as String
}

/**
 * Get attribute of db type: int, integer, tinyint(n) n > 1, smallint, mediumint
 */
fun Map<String, *>.getInt(name: String): Int? {
    return this[name] as Int?
}

/**
 * Get attribute of db type: bigint, unsign int
 */
fun Map<String, *>.getLong(name: String): Long? {
    return this[name] as Long?
}

/**
 * Get attribute of db type: unsigned bigint
 */
fun Map<String, *>.getBigInteger(name: String): java.math.BigInteger {
    return this[name] as java.math.BigInteger
}

/**
 * Get attribute of db type: date, year
 */
fun Map<String, *>.getDate(name: String): java.util.Date {
    return this[name] as java.util.Date
}

/**
 * Get attribute of db type: time
 */
fun Map<String, *>.getTime(name: String): java.sql.Time {
    return this[name] as java.sql.Time
}

/**
 * Get attribute of db type: timestamp, datetime
 */
fun Map<String, *>.getTimestamp(name: String): java.sql.Timestamp {
    return this[name] as java.sql.Timestamp
}

/**
 * Get attribute of db type: real, double
 */
fun Map<String, *>.getDouble(name: String): Double? {
    return this[name] as Double?
}

/**
 * Get attribute of db type: float
 */
fun Map<String, *>.getFloat(name: String): Float? {
    return this[name] as Float?
}

/**
 * Get attribute of db type: bit, tinyint(1)
 */
fun Map<String, *>.getBoolean(name: String): Boolean? {
    return this[name] as Boolean?
}

/**
 * Get attribute of db type: tinyint(1)
 */
fun Map<String, *>.getShort(name: String): Short? {
    return this[name] as Short?
}

/**
 * Get attribute of db type: decimal, numeric
 */
fun Map<String, *>.getBigDecimal(name: String): BigDecimal {
    return this[name] as BigDecimal
}

/**
 * Get attribute of db type: binary, varbinary, tinyblob, blob, mediumblob, longblob
 */
fun Map<String, *>.getBytes(name: String): ByteArray {
    return this[name] as ByteArray
}

/**
 * Get attribute of any type that extends from Number
 */
fun Map<String, *>.getNumber(name: String): Number {
    return this[name] as Number
}