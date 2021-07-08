package xyz.acrylicstyle.dailyranking.api.util

interface ReadonlyList<out E>: List<E>, RandomAccess {
    companion object {
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")
        fun <E> copyOf(list: List<E>): ReadonlyList<E> = of(*(list as java.util.List<*>).toArray() as Array<E>)

        fun <E> of(vararg values: E): ReadonlyList<E> {
            if (values.isEmpty()) return make()
            if (values.size == 1) return make(values[0])
            return SimpleReadonlyList(values)
        }

        private fun <E> make(): ReadonlyList<E> = EmptyReadonlyList
        private fun <E> make(value: E): ReadonlyList<E> = SingletonReadonlyList(value)
    }

    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: @UnsafeVariance E): Boolean
    override fun iterator(): Iterator<E>
    override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean
    override operator fun get(index: Int): E
    override fun indexOf(element: @UnsafeVariance E): Int
    override fun lastIndexOf(element: @UnsafeVariance E): Int
    override fun listIterator(): ListIterator<E>
    override fun listIterator(index: Int): ListIterator<E>
    override fun subList(fromIndex: Int, toIndex: Int): List<E>
    fun set(index: Int, value: @UnsafeVariance E): Nothing = throw UnsupportedOperationException("Nope")
    fun add(value: @UnsafeVariance E): Nothing = throw UnsupportedOperationException("Nope")
}
