package xyz.acrylicstyle.dailyranking.plugin

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import xyz.acrylicstyle.dailyranking.api.DailyRankingBoardAPI
import xyz.acrylicstyle.dailyranking.api.game.Game
import xyz.acrylicstyle.dailyranking.api.game.RegisteredGame
import xyz.acrylicstyle.dailyranking.api.util.ReadonlyList
import xyz.acrylicstyle.dailyranking.plugin.game.GameManager
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.getArmorStandData
import java.util.logging.Logger

interface DailyRankingBoardAPIImpl: Plugin, DailyRankingBoardAPI {
    override fun getLogger(): Logger = Logger.getLogger("DailyRankingBoardAPIImpl")
    override fun addGame(game: Game): RegisteredGame = GameManager.addGame(game)
    override fun removeGame(game: Game) = GameManager.removeGame(game)
    override fun getGames(): ReadonlyList<RegisteredGame> = GameManager.getGames()
    override fun getGameById(id: String): RegisteredGame = getGames().first { it.game.id == id }
    override fun getGameOrNullById(id: String): RegisteredGame? = getGames().firstOrNull { it.game.id == id }
    override fun getGame(predicate: (RegisteredGame) -> Boolean): RegisteredGame = getGames().first(predicate)
    override fun getGameOrNull(predicate: (RegisteredGame) -> Boolean): RegisteredGame? = getGames().firstOrNull(predicate)
    override fun getBoardLocation(): Location? = config.getLocation("lobby_board_location")?.clone()
    override fun getGameSelectorLocation(): Location? = config.getLocation("lobby_game_selector_location")?.clone()
    override fun getMapSelectorLocation(): Location? = config.getLocation("lobby_map_selector_location")?.clone()
    override fun refreshLeaderboard() = Bukkit.getOnlinePlayers().forEach { player -> player.getArmorStandData().let { it.updateText().then { _ -> it.updateAll(player) } } }
}
