package xyz.acrylicstyle.dailyranking.plugin

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.minecraft.server.v1_16_R3.CommandListenerWrapper
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_16_R3.CraftServer
import org.bukkit.craftbukkit.v1_16_R3.command.VanillaCommandWrapper
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import xyz.acrylicstyle.dailyranking.api.DailyRankingBoardAPI
import xyz.acrylicstyle.dailyranking.api.util.Util.stringify
import xyz.acrylicstyle.dailyranking.plugin.listener.JoinLobbyListener
import xyz.acrylicstyle.dailyranking.plugin.listener.LeaderboardListener
import xyz.acrylicstyle.dailyranking.plugin.listener.ReregisterCommandsOnReloadListener
import xyz.acrylicstyle.dailyranking.plugin.packet.DailyRankingBoardPacketHandler
import xyz.acrylicstyle.dailyranking.plugin.packet.incoming.PacketPlayInUseEntityHandler
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.ejectPacketHandler
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.getArmorStandData
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.schedule
import xyz.acrylicstyle.dailyranking.plugin.util.PlayerArmorStandData
import java.io.File
import java.util.logging.Logger

@Suppress("unused")
class DailyRankingBoardPlugin: JavaPlugin(), DailyRankingBoardAPIImpl {
    companion object {
        lateinit var instance: DailyRankingBoardPlugin
    }

    init {
        instance = this
    }

    private val listeners = listOf(
        ReregisterCommandsOnReloadListener,
        LeaderboardListener,
        JoinLobbyListener,
    )

    override fun onEnable() {
        Bukkit.getServicesManager().register(DailyRankingBoardAPI::class.java, this, this, ServicePriority.Normal)
        listeners.forEach { server.pluginManager.registerEvents(it, this) }
        registerCompletions()
        preloadClasses()
        registerPacketHandlers()
        if (InternalUtil.isPaper()) {
            @Suppress("UNCHECKED_CAST")
            server.pluginManager.registerEvent(
                Class.forName("com.destroystokyo.paper.event.server.ServerExceptionEvent") as Class<out Event>,
                ReregisterCommandsOnReloadListener,
                EventPriority.NORMAL,
                { _, event ->
                    logger.warning("Caught server exception!")
                    (Class.forName("com.destroystokyo.paper.event.server.ServerExceptionEvent").getMethod("getException").invoke(event) as Exception).printStackTrace()
                },
                this,
            )
        }
        Bukkit.getOnlinePlayers().forEach { JoinLobbyListener.checkWorld(it) }
    }

    override fun onDisable() {
        Bukkit.getOnlinePlayers().forEach {
            it.getArmorStandData().destroyAll(it)
            it.ejectPacketHandler()
        }
        config.save(File(dataFolder, "config.yml"))
    }

    private fun registerPacketHandlers() {
        DailyRankingBoardPacketHandler.registerIncomingPacketHandler(PacketPlayInUseEntityHandler)
    }

    private fun preloadClasses() {
        InternalUtil.isPaper()
    }

    private fun String.tryLoadClass() {
        try {
            Class.forName(this)
        } catch (ignored: ClassNotFoundException) {}
    }

    private fun getDispatcher(): CommandDispatcher<CommandListenerWrapper> =
        (server as CraftServer).handle.server.commandDispatcher.a()

    internal fun registerCompletions() = {
        val node = getDispatcher().register(
            literal("dailyranking")
                .requires { s -> s.bukkitSender.hasPermission("dailyrankingboard.command") }
                .then(literal("games")
                    .then(literal("add")
                        .then(argument("id", StringArgumentType.word())
                            .then(argument("name", StringArgumentType.word())
                                .executes { context ->
                                    val id = StringArgumentType.getString(context, "id")
                                    val name = StringArgumentType.getString(context, "name")
                                    context.source.bukkitSender.sendMessage("${ChatColor.GREEN}id: $id, name: $name")
                                    return@executes 0
                                })
                        )
                    )
                )
                .then(literal("set")
                    .then(literal("board_location")
                        .executes { context ->
                            val loc = context.source.h().bukkitEntity.location
                            config["lobby_board_location"] = loc
                            PlayerArmorStandData.refreshAll()
                            context.source.bukkitSender.sendMessage("${ChatColor.GREEN}ボードの場所を設定しました。")
                            context.source.bukkitSender.sendMessage(loc.serialize().stringify())
                            return@executes 0
                        })
                    .then(literal("game_selector_location")
                        .executes { context ->
                            val loc = context.source.h().bukkitEntity.location
                            config["lobby_game_selector_location"] = loc
                            PlayerArmorStandData.refreshAll()
                            context.source.bukkitSender.sendMessage("${ChatColor.GREEN}ゲーム選択の場所を設定しました。")
                            context.source.bukkitSender.sendMessage(loc.serialize().stringify())
                            return@executes 0
                        })
                    .then(literal("map_selector_location")
                        .executes { context ->
                            val loc = context.source.h().bukkitEntity.location
                            config["lobby_map_selector_location"] = loc
                            PlayerArmorStandData.refreshAll()
                            context.source.bukkitSender.sendMessage("${ChatColor.GREEN}マップ選択の場所を設定しました。")
                            context.source.bukkitSender.sendMessage(loc.serialize().stringify())
                            return@executes 0
                        })
                )
        )
        (server as CraftServer).commandMap.register("dailyranking", "dailyrankingboard", VanillaCommandWrapper((server as CraftServer).handle.server.commandDispatcher, node))
        getDispatcher().register(literal("dr").redirect(node))
    }.schedule(1)

    private fun literal(name: String): LiteralArgumentBuilder<CommandListenerWrapper> =
        LiteralArgumentBuilder.literal(name)

    private fun <T> argument(name: String, type: ArgumentType<T>): RequiredArgumentBuilder<CommandListenerWrapper, T> =
        RequiredArgumentBuilder.argument(name, type)

    override fun getLogger(): Logger = super<JavaPlugin>.getLogger()
}
