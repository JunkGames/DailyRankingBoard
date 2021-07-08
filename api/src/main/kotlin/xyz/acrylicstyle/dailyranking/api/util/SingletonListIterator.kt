package xyz.acrylicstyle.dailyranking.api.util

class SingletonListIterator<out E>(value: E): SingletonIterator<E>(value), ListIterator<E> {
    override fun hasPrevious(): Boolean = !hasNext

    override fun nextIndex(): Int = if (hasNext) 0 else 1

    override fun previous(): E {
        if (hasNext) throw NoSuchElementException()
        hasNext = true
        return value
    }

    override fun previousIndex(): Int = if (hasNext) -1 else 0
}
