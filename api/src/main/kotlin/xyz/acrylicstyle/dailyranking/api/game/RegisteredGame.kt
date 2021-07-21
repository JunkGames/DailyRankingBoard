package xyz.acrylicstyle.dailyranking.api.game

import xyz.acrylicstyle.dailyranking.api.map.GameMap

interface RegisteredGame {
    val id: String
        get() = game.id
    val name: String
        get() = game.name
    val maps: MutableList<GameMap>
    val game: Game

    fun registerMap(map: GameMap): GameMap

    fun getAsMap(): Map<*, *> = mapOf(
        "game" to game.getAsMap(),
        "maps" to maps.map { it.getAsMap() },
    )
}
