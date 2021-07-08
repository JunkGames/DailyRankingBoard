package xyz.acrylicstyle.dailyranking.plugin.packet

import net.minecraft.server.v1_16_R3.Packet
import org.bukkit.entity.Player

data class IncomingPacket<T: Packet<*>>(val player: Player, val packet: T)
