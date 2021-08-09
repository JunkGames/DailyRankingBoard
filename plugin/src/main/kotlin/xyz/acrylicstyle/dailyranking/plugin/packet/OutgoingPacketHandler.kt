package xyz.acrylicstyle.dailyranking.plugin.packet

import net.minecraft.network.protocol.Packet

@FunctionalInterface
interface OutgoingPacketHandler<T: Packet<*>> {
    fun handle(packet: OutgoingPacket<T>)
}
