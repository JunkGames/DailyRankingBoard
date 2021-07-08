package xyz.acrylicstyle.dailyranking.api.util

internal class SingletonReadonlyList<out E>(private val value: E): ReadonlyList<E>, List<E> {
    override val size: Int = 1

    override fun isEmpty(): Boolean = false

    override fun contains(element: @UnsafeVariance E): Boolean = element == value

    override fun iterator(): Iterator<E> = SingletonIterator(value)

    override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean {
        if (elements.size >= 2) return false
        return elements.firstOrNull() == value
    }

    override fun get(index: Int): E {
        if (index != 0) throw IndexOutOfBoundsException("Index $index is out of bounds of size $size")
        return value
    }

    override fun indexOf(element: @UnsafeVariance E): Int = if (element == value) 0 else -1

    override fun lastIndexOf(element: @UnsafeVariance E): Int = if (element == value) 0 else -1

    override fun listIterator(): ListIterator<E> = SingletonListIterator(value)

    override fun listIterator(index: Int): ListIterator<E> {
        if (index != 0) throw IndexOutOfBoundsException("Index $index is out of bounds of size $size")
        return SingletonListIterator(value)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<E> {
        if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex = $fromIndex")
        if (toIndex > 0) throw IndexOutOfBoundsException("toIndex = $toIndex")
        require(fromIndex <= toIndex) { "fromIndex($fromIndex) > toIndex($toIndex)" }
        return this
    }
}
