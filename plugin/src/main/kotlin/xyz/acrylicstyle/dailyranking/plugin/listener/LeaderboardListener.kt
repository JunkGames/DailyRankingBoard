package xyz.acrylicstyle.dailyranking.plugin.listener

import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import xyz.acrylicstyle.dailyranking.plugin.DailyRankingBoardPlugin
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.getArmorStandData
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.isInWorld
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.rayTraceEntity
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.updateData

object LeaderboardListener: EventListener<PlayerInteractEvent> {
    override fun handle(e: PlayerInteractEvent) {
        if (e.action == Action.PHYSICAL) return
        if (e.hand != EquipmentSlot.HAND) return
        if (!e.player.isInWorld(DailyRankingBoardPlugin.instance.getBoardLocation()?.world)) return
        val data = e.player.getArmorStandData()
        val entity = e.player.rayTraceEntity(
            e.player.eyeLocation.subtract(0.0, 1.0, 0.0),
            5,
            listOf(
                data.leaderboardGameDownArrow,
                data.leaderboardGameUpArrow,
                data.leaderboardMapDownArrow,
                data.leaderboardMapUpArrow,
            ),
        )
        e.player.updateData(entity)
        // for debugging
        // e.player.sendMessage("You clicked (PlayerInteractEvent): $entity")
    }
}
