package xyz.acrylicstyle.dailyranking.plugin.util

import net.blueberrymc.native_util.NativeUtil
import net.minecraft.server.v1_16_R3.Entity
import net.minecraft.server.v1_16_R3.MinecraftServer
import net.minecraft.server.v1_16_R3.Packet
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.craftbukkit.v1_16_R3.CraftServer
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import util.function.ThrowableConsumer
import util.promise.rewrite.Promise
import xyz.acrylicstyle.dailyranking.api.DailyRankingBoardAPI
import xyz.acrylicstyle.dailyranking.api.util.KVMap
import xyz.acrylicstyle.dailyranking.api.util.Util
import xyz.acrylicstyle.dailyranking.plugin.DailyRankingBoardPlugin
import xyz.acrylicstyle.dailyranking.plugin.game.GameManager
import xyz.acrylicstyle.dailyranking.plugin.packet.DailyRankingBoardPacketHandler
import java.util.UUID

object InternalUtil {
    val LOCATION_ZERO: Location
        get() = Location(Util.getFirstWorld(), 0.0, 0.0, 0.0)

    fun <R> (() -> R).runOnMain(): Promise<R> = this.schedule(0)

    fun <R> (() -> R).schedule(delay: Long): Promise<R> = Promise(ThrowableConsumer {
        Bukkit.getScheduler().runTaskLater(DailyRankingBoardAPI.getInstance() as Plugin, Runnable {
            it.resolve(this.invoke())
        }, delay)
    })

    infix fun <R> (() -> R).catch(handler: ((e: Throwable) -> Unit)): R? {
        return try {
            this.invoke()
        } catch (e: Exception) {
            handler.invoke(e)
            null
        } catch (e: NoClassDefFoundError) {
            handler.invoke(e)
            null
        }
    }

    fun isPaper() = try {
        Class.forName("com.destroystokyo.paper.event.server.ServerExceptionEvent")
        true
    } catch (e: ClassNotFoundException) {
        false
    }

    private fun Player.getChannel() = (player as CraftPlayer).handle.playerConnection.networkManager.channel!!

    fun Player.injectPacketHandler() {
        val pipeline = this.getChannel().pipeline();
        {
            pipeline.addBefore("packet_handler", "daily_ranking_board", DailyRankingBoardPacketHandler(this))
            DailyRankingBoardPlugin.instance.logger.info("Injected packet handler for $name")
        } catch { _ ->
            {
                {
                    pipeline.addBefore("packet_handler", "daily_ranking_board", DailyRankingBoardPacketHandler(this))
                    DailyRankingBoardPlugin.instance.logger.info("Injected packet handler for $name")
                } catch {
                    DailyRankingBoardPlugin.instance.logger.warning("Failed to inject packet handler to ${this.name}")
                    it.printStackTrace()
                }
            }.schedule(1)
        }
    }

    fun Player.ejectPacketHandler() {
        val pipeline = this.getChannel().pipeline();
        {
            if (pipeline.get("daily_ranking_board") != null) pipeline.remove("daily_ranking_board")
            DailyRankingBoardPlugin.instance.logger.info("Ejected packet handler for $name")
        } catch { _ ->
            {
                if (pipeline.get("daily_ranking_board") != null) pipeline.remove("daily_ranking_board")
                DailyRankingBoardPlugin.instance.logger.info("Ejected packet handler for $name")
            }.schedule(1).onCatch {
                DailyRankingBoardPlugin.instance.logger.warning("Failed to eject packet handler from ${this.name}")
                it.printStackTrace()
            }
        }
    }

    fun Entity.toLocation() = this.bukkitEntity.location

    inline fun <reified E: Entity> Player.rayTraceEntity(loc: Location, distance: Int, entities: List<E>): E? {
        val vec = this.location.direction
        for (i in 1..distance * 10) {
            val loc2 = loc.clone().add(vec.clone().multiply(i / 10.0))
            // for debugging
            // entities.forEach { println("${loc2.x}, ${loc2.y}, ${loc2.z}, distance: " + it.toLocation().addY(1.35).distance(loc2)) }
            // TODO: check world?
            entities
                .filter { it.world.world.name == loc.world?.name }
                .filter { it.toLocation().addY(1.35).distance(loc2) < 0.2 }
                .minByOrNull { it.toLocation().addY(1.35).distance(loc2) }
                ?.let { return it }        }
        return null
    }

    fun Location.addY(y: Double) = this.apply { this.y += y }

    fun Packet<*>.sendTo(vararg players: Player) {
        players.map { (it as CraftPlayer).handle.playerConnection }.forEach { it.sendPacket(this) }
    }

    private val armorStandData = KVMap<UUID, PlayerArmorStandData> { PlayerArmorStandData(it) }

    fun Player.getArmorStandData() = armorStandData[this.uniqueId]

    fun Player.unregisterArmorStandData() {
        val data = armorStandData.remove(this.uniqueId)
        if (this.isOnline) data?.destroyAll(this)
    }

    fun Player.isInWorld(world: World?): Boolean {
        if (world == null) return false
        return this.world.name == world.name
    }

    fun Player.updateData(entity: Entity?) {
        val data = this.getArmorStandData()
        if (entity == data.leaderboardGameUpArrow) {
            if (++data.currentGameIndex >= GameManager.getAvailableGames().size) {
                data.currentGameIndex = 0
                data.currentMapIndex = 0
            }
        } else if (entity == data.leaderboardGameDownArrow) {
            if (--data.currentGameIndex < 0) {
                data.currentGameIndex = GameManager.getAvailableGames().size - 1
                data.currentMapIndex = 0
            }
        } else if (entity == data.leaderboardMapUpArrow) {
            val registeredGame = GameManager.getAvailableGames().getOrNull(data.currentGameIndex)
            if (registeredGame != null && ++data.currentMapIndex >= registeredGame.maps.size) {
                data.currentMapIndex = 0
            }
        } else if (entity == data.leaderboardMapDownArrow) {
            val registeredGame = GameManager.getAvailableGames().getOrNull(data.currentGameIndex)
            if (registeredGame != null && --data.currentMapIndex < 0) {
                data.currentMapIndex = registeredGame.maps.size - 1
            }
        }
        if (entity != null) {
            this.playSound(this.location, Sound.BLOCK_DISPENSER_DISPENSE, 1f, 1f)
            data.updateText().then { data.updateAll(this) }
        }
    }

    private fun getTicks() = NativeUtil.getInt(MinecraftServer::class.java.getDeclaredField("ticks"), (Bukkit.getServer() as CraftServer).server)

    fun isReload(): Boolean {
        if (Bukkit.getOnlinePlayers().isNotEmpty()) return true
        if (getTicks() > 10) return true
        return false
    }
}
