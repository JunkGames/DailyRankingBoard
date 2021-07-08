package xyz.acrylicstyle.dailyranking.api.util

open class SingletonIterator<out E>(protected val value: E): Iterator<E> {
    protected var hasNext = true

    override fun hasNext(): Boolean = hasNext

    override fun next(): E {
        if (!hasNext()) throw NoSuchElementException()
        hasNext = false
        return value
    }
}
