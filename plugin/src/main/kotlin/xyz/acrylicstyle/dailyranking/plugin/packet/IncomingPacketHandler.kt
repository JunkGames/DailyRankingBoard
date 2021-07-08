package xyz.acrylicstyle.dailyranking.plugin.packet

import net.minecraft.server.v1_16_R3.Packet

@FunctionalInterface
interface IncomingPacketHandler<T: Packet<*>> {
    fun handle(packet: IncomingPacket<T>)
}
