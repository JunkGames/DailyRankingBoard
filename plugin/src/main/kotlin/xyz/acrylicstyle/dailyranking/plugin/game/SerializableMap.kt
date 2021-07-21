package xyz.acrylicstyle.dailyranking.plugin.game

import util.yaml.YamlObject
import xyz.acrylicstyle.dailyranking.api.map.GameMap
import java.util.UUID

data class SerializableMap(override val id: String, override val name: String, val entries: MutableMap<UUID, Comparable<Comparable<*>>>) : GameMap {
    override fun getLeaderboardEntries(): MutableMap<UUID, Comparable<Comparable<*>>> = entries

    fun serialize(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "entries" to entries.mapKeys { it.key.toString() },
    )

    companion object {
        fun deserialize(obj: YamlObject): SerializableMap {
            val id = obj.getString("id") ?: error("Missing id")
            val name = obj.getString("name") ?: error("Missing name")
            @Suppress("UNCHECKED_CAST")
            val entries = obj.getObject("entries")
                .rawData
                .mapKeys { UUID.fromString(it.key) }
                .mapValues { it.value as Comparable<Comparable<*>> }
                .toMutableMap()
            return SerializableMap(id, name, entries)
        }
    }
}
