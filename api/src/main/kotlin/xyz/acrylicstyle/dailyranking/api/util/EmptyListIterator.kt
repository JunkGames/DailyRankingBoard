package xyz.acrylicstyle.dailyranking.api.util

object EmptyListIterator: ListIterator<Nothing> {
    override fun hasNext() = false
    override fun hasPrevious() = false
    override fun next() = throw NoSuchElementException()
    override fun nextIndex() = -1
    override fun previous() = throw NoSuchElementException()
    override fun previousIndex() = -1
}
