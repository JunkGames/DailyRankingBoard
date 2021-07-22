package xyz.acrylicstyle.dailyranking.plugin.game

import xyz.acrylicstyle.dailyranking.api.game.Game
import xyz.acrylicstyle.dailyranking.api.game.RegisteredGame
import xyz.acrylicstyle.dailyranking.api.map.GameMap

class SimpleRegisteredGame(override val game: Game): RegisteredGame {
    override val maps = ArrayList<GameMap>()

    override fun registerMap(map: GameMap): GameMap {
        if (!RegisteredGame.isValidId(map.id)) throw IllegalArgumentException("Invalid id: ${map.id}")
        if (maps.any { it.id.equals(map.id, true) }) throw IllegalArgumentException("Duplicate map: ${map.id}")
        maps.add(map)
        return map
    }

    override fun removeMap(map: GameMap): Boolean = maps.remove(map)
    override fun getMapById(id: String): GameMap = maps.find { it.id == id } ?: throw NoSuchElementException()
    override fun getMapByIdOrNull(id: String): GameMap? = maps.find { it.id == id }
}
