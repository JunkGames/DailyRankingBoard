package xyz.acrylicstyle.dailyranking.plugin.packet

import net.minecraft.network.protocol.Packet
import org.bukkit.entity.Player

data class OutgoingPacket<T: Packet<*>>(val player: Player, val packet: T)
