package xyz.acrylicstyle.dailyranking.api

import org.bukkit.Bukkit
import org.bukkit.Location
import xyz.acrylicstyle.dailyranking.api.game.Game
import xyz.acrylicstyle.dailyranking.api.game.RegisteredGame
import xyz.acrylicstyle.dailyranking.api.util.ReadonlyList
import java.util.logging.Logger

interface DailyRankingBoardAPI {
    companion object {
        @JvmStatic
        fun getInstance(): DailyRankingBoardAPI =
            (Bukkit.getServicesManager().getRegistration(DailyRankingBoardAPI::class.java)
                ?: throw NoSuchElementException("DailyRankingBoardAPI is not available in this environment")).provider
    }

    /**
     * @return Logger that can be used to log messages.
     */
    fun getLogger(): Logger

    /**
     * Adds a game to games list.
     * @throws IllegalArgumentException When a method was called with same game (identified by game id) that's already
     * in the list.
     */
    fun addGame(game: Game): RegisteredGame

    /**
     * @return All defined games. The returned list is immutable and cannot be modified.
     */
    fun getGames(): ReadonlyList<RegisteredGame>

    /**
     * Find and return game by id.
     * @see getGameOrNullById
     * @throws NoSuchElementException if game wasn't found by specified id
     * @return game if found
     */
    fun getGameById(id: String): RegisteredGame

    /**
     * Find and return game by id if any.
     * @see getGameById
     * @return game if found, null otherwise
     */
    fun getGameOrNullById(id: String): RegisteredGame?

    /**
     * Find and return game by specified predicate.
     * @see getGameOrNull
     * @throws NoSuchElementException if game wasn't found by specified id
     * @return game if found
     */
    fun getGame(predicate: (RegisteredGame) -> Boolean): RegisteredGame

    /**
     * Find and return game by specified predicate.
     * @see getGame
     * @return game if found, null otherwise
     */
    fun getGameOrNull(predicate: (RegisteredGame) -> Boolean): RegisteredGame?

    /**
     * Get leaderboard location defined by player.
     * @return location if configured, null otherwise
     */
    fun getBoardLocation(): Location?

    /**
     * Get game selector location defined by player.
     * @return location if configured, null otherwise
     */
    fun getGameSelectorLocation(): Location?

    /**
     * Get map selector location defined by player.
     * @return location if configured, null otherwise
     */
    fun getMapSelectorLocation(): Location?
}
