package xyz.acrylicstyle.dailyranking.api.map

import org.intellij.lang.annotations.MagicConstant
import xyz.acrylicstyle.dailyranking.api.util.MapSerializable
import java.util.UUID
import kotlin.collections.Map

interface GameMap: MapSerializable {
    val id: String

    val name: String

    fun getLeaderboardEntries(): Map<UUID, Int>

    fun removeLeaderboardEntry(uuid: UUID) {
        val entries = getLeaderboardEntries()
        if (entries !is MutableMap<UUID, Int>) return
        entries.remove(uuid)
    }

    fun clearLeaderboardEntries() {
        val entries = getLeaderboardEntries()
        if (entries !is MutableMap<UUID, Int>) return
        entries.clear()
    }

    object UpdateFlag {
        const val NONE = 1 shl 0 // => 0
        const val NEW_PERSONAL_RECORD = 1 shl 1 // => 2
        const val NEW_RECORD = 1 shl 2 // => 4
    }

    @MagicConstant(flagsFromClass = UpdateFlag::class)
    fun addLeaderboardEntry(uuid: UUID, value: Int): Int {
        val entries = getLeaderboardEntries()
        if (entries !is MutableMap<UUID, Int>) return -1
        var personalBest = false
        val newRecord = (entries.values.maxOrNull() ?: 0) > value
        val oldTime = entries[uuid]
        if (oldTime == null) {
            entries[uuid] = value
            personalBest = true
            return buildUpdateType(personalBest, newRecord)
        }
        if (value < oldTime) {
            entries[uuid] = value
            personalBest = false
        }
        return buildUpdateType(personalBest, newRecord)
    }

    private fun buildUpdateType(personalBest: Boolean, newRecord: Boolean): Int {
        if (personalBest && newRecord) return UpdateFlag.NEW_PERSONAL_RECORD or UpdateFlag.NEW_RECORD
        if (personalBest) return UpdateFlag.NEW_PERSONAL_RECORD
        if (newRecord) return UpdateFlag.NEW_RECORD
        return UpdateFlag.NONE
    }

    override fun getAsMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "leaderboard_entries" to getLeaderboardEntries().mapKeys { it.key.toString() },
    )
}
