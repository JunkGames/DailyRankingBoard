package xyz.acrylicstyle.dailyranking.api.util

internal object EmptyReadonlyList: ReadonlyList<Nothing> {
    override val size: Int = 0
    override fun isEmpty(): Boolean = true
    override fun contains(element: Nothing): Boolean = false
    override fun iterator(): Iterator<Nothing> = EmptyIterator
    override fun containsAll(elements: Collection<Nothing>) = false
    override fun get(index: Int): Nothing = throw IndexOutOfBoundsException("Index $index is out of bounds of size 0")
    override fun indexOf(element: Nothing): Int = -1
    override fun lastIndexOf(element: Nothing): Int = -1
    override fun listIterator(): ListIterator<Nothing> = EmptyListIterator
    override fun listIterator(index: Int): ListIterator<Nothing> = throw IndexOutOfBoundsException()
    override fun subList(fromIndex: Int, toIndex: Int): List<Nothing> = throw IndexOutOfBoundsException()
}
