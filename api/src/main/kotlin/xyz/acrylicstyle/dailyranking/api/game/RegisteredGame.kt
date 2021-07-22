package xyz.acrylicstyle.dailyranking.api.game

import xyz.acrylicstyle.dailyranking.api.map.GameMap
import xyz.acrylicstyle.dailyranking.api.util.MapSerializable
import kotlin.jvm.Throws

interface RegisteredGame: MapSerializable {
    val id: String
        get() = game.id
    val name: String
        get() = game.name
    val maps: MutableList<GameMap>
    val game: Game

    fun registerMap(map: GameMap): GameMap
    fun removeMap(map: GameMap): Boolean
    fun findMap(predicate: (GameMap) -> Boolean): GameMap? = maps.find(predicate)
    @Throws(NoSuchElementException::class)
    fun getMapById(id: String): GameMap
    fun getMapByIdOrNull(id: String): GameMap?

    override fun getAsMap(): Map<String, Any> = mapOf(
        "game" to game.getAsMap(),
        "maps" to maps.map { it.getAsMap() },
    )

    companion object {
        private val PATTERN = "^[a-zA-Z0-9_\\-+]+$".toRegex()

        fun isValidId(id: String) = id.matches(PATTERN)
    }
}
