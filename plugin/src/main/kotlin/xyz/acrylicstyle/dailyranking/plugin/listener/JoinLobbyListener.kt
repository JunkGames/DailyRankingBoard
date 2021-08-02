package xyz.acrylicstyle.dailyranking.plugin.listener

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import xyz.acrylicstyle.dailyranking.plugin.DailyRankingBoardPlugin
import xyz.acrylicstyle.dailyranking.plugin.configuration.UserCacheFile
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.getArmorStandData
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.injectPacketHandler
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.isInWorld
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.schedule
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.unregisterArmorStandData

object JoinLobbyListener: EventListener2<PlayerJoinEvent, PlayerChangedWorldEvent> {
    override fun handle1(e: PlayerJoinEvent) {
        UserCacheFile[e.player.uniqueId] = e.player.name
        checkWorld(e.player, true)
    }

    override fun handle2(e: PlayerChangedWorldEvent) {
        checkWorld(e.player)
    }

    fun checkWorld(player: Player, inject: Boolean = false) {
        {
            if (inject) player.injectPacketHandler()
            if (player.isInWorld(DailyRankingBoardPlugin.instance.getBoardLocation()?.world)) {
                player.unregisterArmorStandData() // unregister if any
                val data = player.getArmorStandData();
                {
                    data.updateText().then { data.spawnAll(player) }
                }.schedule(10)
            } else {
                player.getArmorStandData().destroyAll(player)
            }
        }.schedule(1)
    }
}
