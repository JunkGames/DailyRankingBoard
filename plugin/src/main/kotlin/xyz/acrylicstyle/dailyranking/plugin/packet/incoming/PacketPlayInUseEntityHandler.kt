package xyz.acrylicstyle.dailyranking.plugin.packet.incoming

import net.minecraft.network.protocol.game.PacketPlayInUseEntity
import util.reflect.Reflect
import xyz.acrylicstyle.dailyranking.plugin.DailyRankingBoardPlugin
import xyz.acrylicstyle.dailyranking.plugin.packet.IncomingPacket
import xyz.acrylicstyle.dailyranking.plugin.packet.IncomingPacketHandler
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.getArmorStandData
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.rayTraceEntity
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.isInWorld
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.updateData

object PacketPlayInUseEntityHandler: IncomingPacketHandler<PacketPlayInUseEntity> {
    override fun handle(packet: IncomingPacket<PacketPlayInUseEntity>) {
        val player = packet.player
        val ordinal = Reflect
            .on(packet.packet)
            .field<Any>("b")
            .call<Any>("a")
            .call<Int>("ordinal")
            .get()
        if (ordinal >= 1 && !packet.packet.b()) {
            if (!player.isInWorld(DailyRankingBoardPlugin.instance.getBoardLocation()?.world)) return
            val data = player.getArmorStandData()
            val entity = player.rayTraceEntity(
                player.eyeLocation.subtract(0.0, 1.0, 0.0),
                5,
                listOf(
                    data.leaderboardGameDownArrow,
                    data.leaderboardGameUpArrow,
                    data.leaderboardMapDownArrow,
                    data.leaderboardMapUpArrow,
                ),
            )
            player.updateData(entity)
            // for debugging
            // player.sendMessage("You clicked (Packet): $entity")
        }
    }
}
