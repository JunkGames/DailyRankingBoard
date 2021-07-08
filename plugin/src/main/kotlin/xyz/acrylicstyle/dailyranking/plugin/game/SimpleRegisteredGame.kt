package xyz.acrylicstyle.dailyranking.plugin.game

import xyz.acrylicstyle.dailyranking.api.game.Game
import xyz.acrylicstyle.dailyranking.api.game.RegisteredGame
import xyz.acrylicstyle.dailyranking.api.map.GameMap

class SimpleRegisteredGame(override val game: Game): RegisteredGame {
    override val maps = ArrayList<GameMap>()

    override fun registerMap(map: GameMap): GameMap {
        maps.add(map)
        return map
    }
}
