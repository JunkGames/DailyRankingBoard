package xyz.acrylicstyle.dailyranking.plugin.packet

import net.minecraft.network.protocol.Packet

@FunctionalInterface
interface IncomingPacketHandler<T: Packet<*>> {
    fun handle(packet: IncomingPacket<T>)
}
