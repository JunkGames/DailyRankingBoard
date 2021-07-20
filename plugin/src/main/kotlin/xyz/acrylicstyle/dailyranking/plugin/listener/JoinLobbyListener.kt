package xyz.acrylicstyle.dailyranking.plugin.listener

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import xyz.acrylicstyle.dailyranking.plugin.DailyRankingBoardPlugin
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.getArmorStandData
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.injectPacketHandler
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.isInWorld
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.schedule

object JoinLobbyListener: EventListener2<PlayerJoinEvent, PlayerChangedWorldEvent> {
    override fun handle1(e: PlayerJoinEvent) {
        checkWorld(e.player);
        {
            e.player.injectPacketHandler()
        }.schedule(1)
    }

    override fun handle2(e: PlayerChangedWorldEvent) {
        checkWorld(e.player)
    }

    fun checkWorld(player: Player) {
        {
            if (player.isInWorld(DailyRankingBoardPlugin.instance.getBoardLocation()?.world)) {
                player.getArmorStandData().spawnAll(player)
            } else {
                player.getArmorStandData().destroyAll(player)
            }
        }.schedule(1)
    }
}
