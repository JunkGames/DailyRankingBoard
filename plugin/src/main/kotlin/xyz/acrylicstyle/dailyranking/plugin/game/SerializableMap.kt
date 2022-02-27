package xyz.acrylicstyle.dailyranking.plugin.game

import util.yaml.YamlObject
import xyz.acrylicstyle.dailyranking.api.map.GameMap
import xyz.acrylicstyle.dailyranking.api.map.GameMap.UpdateFlag
import java.util.UUID

data class SerializableMap(override val id: String, override var name: String, val entries: Map<UUID, Int>) : GameMap {
    override fun getLeaderboardEntries(): Map<UUID, Int> = entries

    companion object {
        fun deserialize(obj: YamlObject): SerializableMap {
            val id = obj.getString("id") ?: error("Missing id")
            val name = obj.getString("name") ?: error("Missing name")
            @Suppress("UNCHECKED_CAST")
            val entries = obj.getObject("leaderboard_entries")
                .rawData
                .mapKeys { UUID.fromString(it.key) }
                .mapValues { it.value as Int }
                .toMutableMap()
            return SerializableMap(id, name, entries)
        }
    }
}
