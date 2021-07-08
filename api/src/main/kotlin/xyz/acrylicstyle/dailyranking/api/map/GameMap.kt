package xyz.acrylicstyle.dailyranking.api.map

import java.util.UUID
import kotlin.collections.Map

interface GameMap {
    val id: String

    val name: String

    fun getValueToStringFunction(comparable: Comparable<*>): String = comparable.toString()

    fun getLeaderboardEntries(): Map<UUID, Comparable<Comparable<*>>>

    fun getAsMap(): Map<*, *> = mapOf(
        "id" to id,
        "leaderboard_entries" to getLeaderboardEntries().mapKeys { it.key.toString() },
    )
}
