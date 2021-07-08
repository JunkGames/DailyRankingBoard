package xyz.acrylicstyle.dailyranking.plugin.game

import xyz.acrylicstyle.dailyranking.api.game.Game
import xyz.acrylicstyle.dailyranking.api.game.RegisteredGame
import xyz.acrylicstyle.dailyranking.api.util.ReadonlyList

object GameManager {
    private val games = ArrayList<RegisteredGame>()

    fun addGame(game: Game): SimpleRegisteredGame {
        if (games.any { it.game.id.equals(game.id, true) }) throw IllegalArgumentException("Duplicate game id: ${game.id}")
        val registeredGame = SimpleRegisteredGame(game)
        games.add(registeredGame)
        return registeredGame
    }

    fun getGames(): ReadonlyList<RegisteredGame> = ReadonlyList.copyOf(games)

    fun getAvailableGames(): ReadonlyList<RegisteredGame> = ReadonlyList.copyOf(games.filter { it.maps.isNotEmpty() })
}
