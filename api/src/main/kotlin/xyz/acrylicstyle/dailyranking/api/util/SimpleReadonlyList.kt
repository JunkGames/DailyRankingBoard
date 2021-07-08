package xyz.acrylicstyle.dailyranking.api.util

internal class SimpleReadonlyList<out E>(private val array: Array<out E>): ReadonlyList<E> {
    override val size: Int
        get() = array.size

    override fun isEmpty(): Boolean = array.isEmpty()

    override fun contains(element: @UnsafeVariance E): Boolean = array.contains(element)

    override fun iterator(): Iterator<E> = array.iterator()

    override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean = toMutableList().containsAll(elements)

    override fun get(index: Int): E = array[index]

    override fun indexOf(element: @UnsafeVariance E): Int = array.indexOf(element)

    override fun lastIndexOf(element: @UnsafeVariance E): Int = array.lastIndexOf(element)

    override fun listIterator(): ListIterator<E> = toMutableList().listIterator()

    override fun listIterator(index: Int): ListIterator<E> = toMutableList().listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): List<E> = toMutableList().subList(fromIndex, toIndex)
}
