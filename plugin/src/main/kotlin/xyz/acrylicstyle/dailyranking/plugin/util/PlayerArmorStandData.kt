package xyz.acrylicstyle.dailyranking.plugin.util

import net.minecraft.server.v1_16_R3.EntityArmorStand
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import util.promise.rewrite.Promise
import xyz.acrylicstyle.dailyranking.api.util.Util.or
import xyz.acrylicstyle.dailyranking.plugin.DailyRankingBoardPlugin.Companion.instance
import xyz.acrylicstyle.dailyranking.plugin.game.GameManager
import xyz.acrylicstyle.dailyranking.plugin.listener.JoinLobbyListener
import xyz.acrylicstyle.dailyranking.plugin.util.ArmorStandUtil.destroy
import xyz.acrylicstyle.dailyranking.plugin.util.ArmorStandUtil.getUpdatePacket
import xyz.acrylicstyle.dailyranking.plugin.util.ArmorStandUtil.sendPacket
import xyz.acrylicstyle.dailyranking.plugin.util.ArmorStandUtil.setText
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.LOCATION_ZERO
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.addY
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.getArmorStandData
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.sendTo
import xyz.acrylicstyle.mcutil.mojang.MojangAPI
import java.util.UUID

class PlayerArmorStandData(private val uuid: UUID) {
    companion object {
        fun refreshAll() {
            Bukkit.getOnlinePlayers().forEach {
                it.getArmorStandData().destroyAll(it)
                JoinLobbyListener.checkWorld(it)
            }
        }
    }

    var currentGameIndex = 0
    var currentMapIndex = 0

    private val leaderboardGameDummyUpArrow = ArmorStandUtil.createHologram(instance.getGameSelectorLocation().or(LOCATION_ZERO).addY(ArmorStandUtil.offset * 0.5), null)
    val leaderboardGameUpArrow = ArmorStandUtil.createHologram(instance.getGameSelectorLocation().or(LOCATION_ZERO).addY(ArmorStandUtil.offset * -0.5), "${ChatColor.GOLD}▲△▲")
    private val leaderboardGameCurrent = ArmorStandUtil.createHologram(instance.getGameSelectorLocation().or(LOCATION_ZERO).addY(ArmorStandUtil.offset * -1.5), "") // GOLD + BOLD + game name
    val leaderboardGameDownArrow = ArmorStandUtil.createHologram(instance.getGameSelectorLocation().or(LOCATION_ZERO).addY(ArmorStandUtil.offset * -2.5), "${ChatColor.GOLD}▼▽▼")

    private val leaderboardMapDummyUpArrow = ArmorStandUtil.createHologram(instance.getMapSelectorLocation().or(LOCATION_ZERO).addY(ArmorStandUtil.offset * 0.5), null)
    val leaderboardMapUpArrow = ArmorStandUtil.createHologram(instance.getMapSelectorLocation().or(LOCATION_ZERO).addY(ArmorStandUtil.offset * -0.5), "${ChatColor.GOLD}▲△▲")
    private val leaderboardMapCurrent = ArmorStandUtil.createHologram(instance.getMapSelectorLocation().or(LOCATION_ZERO).addY(ArmorStandUtil.offset * -1.5), "") // GOLD + BOLD + map name
    val leaderboardMapDownArrow = ArmorStandUtil.createHologram(instance.getMapSelectorLocation().or(LOCATION_ZERO).addY(ArmorStandUtil.offset * -2.5), "${ChatColor.GOLD}▼▽▼")

    private val leaderboardEntriesTitle = ArmorStandUtil.createHologram(instance.getBoardLocation().or(LOCATION_ZERO).addY(ArmorStandUtil.offset), "${ChatColor.AQUA}${ChatColor.BOLD}デイリーランキング")
    private val leaderboardEntriesCurrentMap = ArmorStandUtil.createHologram(instance.getBoardLocation().or(LOCATION_ZERO), "") // GRAY + game name + WHITE + map name
    private val leaderboardEntries = ArrayList<EntityArmorStand>()

    fun spawnAll(player: Player) {
        leaderboardGameDummyUpArrow.sendPacket(player)
        leaderboardGameUpArrow.sendPacket(player)
        leaderboardGameCurrent.sendPacket(player)
        leaderboardGameDownArrow.sendPacket(player)

        leaderboardMapDummyUpArrow.sendPacket(player)
        leaderboardMapUpArrow.sendPacket(player)
        leaderboardMapCurrent.sendPacket(player)
        leaderboardMapDownArrow.sendPacket(player)

        leaderboardEntriesTitle.sendPacket(player)
        leaderboardEntriesCurrentMap.sendPacket(player)
        leaderboardEntries.forEach { it.sendPacket(player) }
    }

    fun destroyAll(player: Player) {
        leaderboardGameDummyUpArrow.destroy(player)
        leaderboardGameUpArrow.destroy(player)
        leaderboardGameCurrent.destroy(player)
        leaderboardGameDownArrow.destroy(player)

        leaderboardMapDummyUpArrow.destroy(player)
        leaderboardMapUpArrow.destroy(player)
        leaderboardMapCurrent.destroy(player)
        leaderboardMapDownArrow.destroy(player)

        leaderboardEntriesTitle.destroy(player)
        leaderboardEntriesCurrentMap.destroy(player)
        leaderboardEntries.forEach { it.destroy(player) }
    }

    fun updateAll(player: Player) {
        leaderboardGameCurrent.getUpdatePacket().sendTo(player)

        leaderboardMapCurrent.getUpdatePacket().sendTo(player)

        leaderboardEntriesTitle.getUpdatePacket().sendTo(player)
        leaderboardEntriesCurrentMap.getUpdatePacket().sendTo(player)
        leaderboardEntries.forEach { it.getUpdatePacket().sendTo(player) }
    }

    fun updateText() = Promise.create<Unit> { context ->
        if (GameManager.getAvailableGames().isEmpty()) return@create context.resolve()
        val registeredGame = GameManager.getAvailableGames().getOrNull(currentGameIndex) ?: return@create context.resolve()
        if (registeredGame.maps.isEmpty()) return@create context.resolve()
        val map = registeredGame.maps.getOrNull(currentMapIndex) ?: return@create context.resolve()
        leaderboardGameCurrent.setText("${ChatColor.GOLD}ゲーム: ${ChatColor.BOLD}${registeredGame.game.name}")
        leaderboardMapCurrent.setText("${ChatColor.GOLD}マップ: ${ChatColor.BOLD}${map.name}")
        leaderboardEntriesCurrentMap.setText("${ChatColor.GRAY}${registeredGame.game.name} ${ChatColor.WHITE}- ${ChatColor.GRAY}${map.name}")
        val entries = map.getLeaderboardEntries().entries.sortedBy { it.value }
        for (i in 0..9) {
            entries.getOrNull(i).let { entry ->
                if (entry == null) {
                    leaderboardEntries[i].setText("${ChatColor.YELLOW}${i + 1}. ${ChatColor.GRAY}...")
                    return@let
                }
                MojangAPI.getName(entry.key).then { name ->
                    leaderboardEntries[i].setText("${ChatColor.YELLOW}${i + 1}. ${ChatColor.WHITE}$name ${ChatColor.GRAY}- ${ChatColor.YELLOW}${registeredGame.game.getValueToStringFunction(entry.value)}")
                }.onCatch { throwable ->
                    instance.logger.warning("Could not get the player name of ${entry.key}")
                    throwable.printStackTrace()
                }.complete()
            }
        }
        map.getLeaderboardEntries()[uuid].let { value ->
            MojangAPI.getName(uuid).then { name ->
                if (value == null) {
                    leaderboardEntries[10].setText("")
                } else {
                    val rank = entries.indexOfFirst { entry -> entry.key == uuid } + 1
                    leaderboardEntries[10].setText("${ChatColor.YELLOW}${ChatColor.BOLD}$rank. ${ChatColor.WHITE}${ChatColor.BOLD}$name ${ChatColor.GRAY}- ${ChatColor.YELLOW}${ChatColor.BOLD}${registeredGame.game.getValueToStringFunction(value)}")
                }
            }.onCatch { throwable ->
                instance.logger.warning("Could not get the player name of $uuid")
                throwable.printStackTrace()
            }.complete()
        }
        context.resolve()
    }

    init {
        for (i in 1..10) {
            leaderboardEntries.add(ArmorStandUtil.createHologram(instance.getBoardLocation().or(LOCATION_ZERO).addY(ArmorStandUtil.offset * -i), "${ChatColor.YELLOW}$i. ${ChatColor.GRAY}..."))
        }
        leaderboardEntries.add(ArmorStandUtil.createHologram(instance.getBoardLocation().or(LOCATION_ZERO).addY(ArmorStandUtil.offset * -11), ""))
    }
}
