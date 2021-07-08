package xyz.acrylicstyle.dailyranking.plugin.packet

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.server.v1_16_R3.Packet
import org.bukkit.entity.Player

class DailyRankingBoardPacketHandler(private val player: Player): ChannelDuplexHandler() {
    companion object {
        val outgoingPacketHandler = HashMap<Class<*>, ArrayList<OutgoingPacketHandler<*>>>()
        val incomingPacketHandler = HashMap<Class<*>, ArrayList<IncomingPacketHandler<*>>>()

        @Suppress("UNCHECKED_CAST")
        inline fun <reified T: Packet<*>> registerOutgoingPacketHandler(handler: OutgoingPacketHandler<T>) {
            if (!outgoingPacketHandler.containsKey(T::class.java)) outgoingPacketHandler[T::class.java] = ArrayList()
            outgoingPacketHandler[T::class.java]!!.add(handler)
        }

        @Suppress("UNCHECKED_CAST")
        inline fun <reified T: Packet<*>> registerIncomingPacketHandler(handler: IncomingPacketHandler<T>) {
            if (!incomingPacketHandler.containsKey(T::class.java)) incomingPacketHandler[T::class.java] = ArrayList()
            incomingPacketHandler[T::class.java]!!.add(handler)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val packet = IncomingPacket(player, msg as Packet<*>)
        incomingPacketHandler[msg::class.java]?.forEach { (it as IncomingPacketHandler<Packet<*>>).handle(packet) }
        super.channelRead(ctx, msg)
    }

    @Suppress("UNCHECKED_CAST")
    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        val packet = OutgoingPacket(player, msg as Packet<*>)
        outgoingPacketHandler[msg::class.java]?.forEach { (it as OutgoingPacketHandler<Packet<*>>).handle(packet) }
        super.write(ctx, msg, promise)
    }
}
