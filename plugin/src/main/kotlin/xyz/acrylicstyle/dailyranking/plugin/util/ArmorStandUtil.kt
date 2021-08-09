package xyz.acrylicstyle.dailyranking.plugin.util

import net.minecraft.network.chat.ChatComponentText
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity
import net.minecraft.world.entity.decoration.EntityArmorStand
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import xyz.acrylicstyle.dailyranking.plugin.DailyRankingBoardPlugin.Companion.debug

object ArmorStandUtil {
    const val offset = 0.4

    fun createHologram(location: Location, text: String?): EntityArmorStand {
        debug("Creating hologram with text '$text' at $location", 2)
        val worldServer = (location.world!! as CraftWorld).handle
        val armorStand = EntityArmorStand(worldServer, location.x, location.y, location.z)
        armorStand.isInvisible = true
        armorStand.isInvulnerable = true
        armorStand.isNoGravity = true
        if (text != null) {
            armorStand.customNameVisible = true
            armorStand.customName = ChatComponentText(text)
        }
        return armorStand
    }

    fun EntityArmorStand.sendPacket(player: Player): EntityArmorStand {
        (player as CraftPlayer).handle.b.apply {
            sendPacket(getSpawnPacket())
            sendPacket(getUpdatePacket())
        }
        return this
    }

    fun EntityArmorStand.destroy(player: Player): EntityArmorStand {
        (player as CraftPlayer).handle.b.apply {
            sendPacket(getDestroyPacket())
        }
        return this
    }

    private fun EntityArmorStand.getSpawnPacket() = PacketPlayOutSpawnEntity(this)

    private fun EntityArmorStand.getDestroyPacket() = PacketPlayOutEntityDestroy(this.id)

    fun EntityArmorStand.getUpdatePacket() = PacketPlayOutEntityMetadata(this.id, this.dataWatcher, true)

    fun EntityArmorStand.setText(text: String) {
        this.customName = ChatComponentText(text)
    }
}
