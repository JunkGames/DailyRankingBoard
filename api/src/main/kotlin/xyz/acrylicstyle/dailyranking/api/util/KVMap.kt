package xyz.acrylicstyle.dailyranking.api.util

import org.jetbrains.annotations.Contract
import util.collection.Collection
import java.util.function.Function

class KVMap<K, V>(private val constructor: Function<K, V>) : Collection<K, V>() {
    @Contract("_ -> fail")
    override operator fun get(key: K): V {
        if (key == null) throw NullPointerException("key cannot be null")
        if (!this.containsKey(key)) super.put(key, constructor.apply(key))
        return super.get(key)!!
    }
}
