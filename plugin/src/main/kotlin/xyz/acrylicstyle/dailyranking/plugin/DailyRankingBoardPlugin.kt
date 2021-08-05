package xyz.acrylicstyle.dailyranking.plugin

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.minecraft.server.v1_16_R3.ChatComponentText
import net.minecraft.server.v1_16_R3.CommandListenerWrapper
import net.minecraft.server.v1_16_R3.ICompletionProvider
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_16_R3.CraftServer
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import xyz.acrylicstyle.dailyranking.api.DailyRankingBoardAPI
import xyz.acrylicstyle.dailyranking.api.util.Util.stringify
import xyz.acrylicstyle.dailyranking.plugin.argument.GameArgument
import xyz.acrylicstyle.dailyranking.plugin.argument.MapArgument
import xyz.acrylicstyle.dailyranking.plugin.argument.OrderArgument
import xyz.acrylicstyle.dailyranking.plugin.configuration.GameConfigurationFile
import xyz.acrylicstyle.dailyranking.plugin.configuration.UserCacheFile
import xyz.acrylicstyle.dailyranking.plugin.game.GameManager
import xyz.acrylicstyle.dailyranking.plugin.game.SerializableGame
import xyz.acrylicstyle.dailyranking.plugin.game.SerializableMap
import xyz.acrylicstyle.dailyranking.plugin.listener.JoinLobbyListener
import xyz.acrylicstyle.dailyranking.plugin.listener.LeaderboardListener
import xyz.acrylicstyle.dailyranking.plugin.listener.ReregisterCommandsOnReloadListener
import xyz.acrylicstyle.dailyranking.plugin.packet.DailyRankingBoardPacketHandler
import xyz.acrylicstyle.dailyranking.plugin.packet.incoming.PacketPlayInUseEntityHandler
import xyz.acrylicstyle.dailyranking.plugin.util.CommandWrapper
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.addY
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.ejectPacketHandler
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.getArmorStandData
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.runOnMain
import xyz.acrylicstyle.dailyranking.plugin.util.InternalUtil.schedule
import xyz.acrylicstyle.dailyranking.plugin.util.PlayerArmorStandData
import java.io.File
import java.time.LocalDateTime
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import java.util.logging.Logger
import kotlin.math.max
import kotlin.math.min

@Suppress("unused")
class DailyRankingBoardPlugin: JavaPlugin(), DailyRankingBoardAPIImpl {
    companion object {
        lateinit var instance: DailyRankingBoardPlugin

        // Range: 0 - 99999
        //     0: off
        //     1: on
        //     2: + show noisy debug messages
        // 99999: + dump stacktrace with debug message
        var debugLevel = 0

        fun debug(message: String, minLevel: Int = 1) {
            if (debugLevel < minLevel) return
            instance.logger.info(message)
            if (debugLevel >= 99999) Throwable("Debug").printStackTrace()
        }
    }

    init {
        instance = this
    }

    private val listeners = listOf(
        ReregisterCommandsOnReloadListener,
        LeaderboardListener,
        JoinLobbyListener,
    )

    private val timer = Timer()
    var day = -1

    override fun onEnable() {
        if (InternalUtil.isReload()) error("Reload detected. Please restart the server.\nLearn more why you should never reload the plugin: https://madelinemiller.dev/blog/problem-with-reload/")
        debugLevel = max(0, min(99999, config.getInt("debugLevel", 0)))
        UserCacheFile.load()
        GameConfigurationFile.loadAll()
        Bukkit.getServicesManager().register(DailyRankingBoardAPI::class.java, this, this, ServicePriority.Normal)
        listeners.forEach { server.pluginManager.registerEvents(it, this) }
        registerCommands()
        preloadClasses()
        registerPacketHandlers()
        if (debugLevel >= 9999 && InternalUtil.isPaper()) {
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
        Bukkit.getOnlinePlayers().forEach { JoinLobbyListener.checkWorld(it, true) }
        day = config.getInt("lastDay", -1)
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (day != LocalDateTime.now().dayOfYear) {
                    day = LocalDateTime.now().dayOfYear
                    config["lastDay"] = day
                    resetLeaderboard()
                    refreshLeaderboard()
                    Bukkit.broadcastMessage("${ChatColor.GRAY}[${ChatColor.GOLD}DailyRanking${ChatColor.GRAY}] ${ChatColor.YELLOW}デイリーランキングがリセットされました！")
                }
            }
        }, 10000L, 10000L)
    }

    private fun resetLeaderboard() {
        GameManager.getGames().forEach {
            it.maps.forEach { map -> map.clearLeaderboardEntries() }
        }
        UserCacheFile.clear()
        UserCacheFile.write()
    }

    override fun onDisable() {
        UserCacheFile.write()
        GameConfigurationFile.saveAll()
        Bukkit.getOnlinePlayers().forEach {
            it.getArmorStandData().destroyAll(it)
            it.ejectPacketHandler()
        }
        saveConfig()
    }

    private fun registerPacketHandlers() {
        DailyRankingBoardPacketHandler.registerIncomingPacketHandler(PacketPlayInUseEntityHandler)
    }

    private fun preloadClasses() {
        InternalUtil.isPaper()
        "xyz.acrylicstyle.dailyranking.api.util.ReadonlyList".tryLoadClass()
        "xyz.acrylicstyle.dailyranking.api.util.SingletonReadonlyList".tryLoadClass()
        "xyz.acrylicstyle.dailyranking.api.util.SingletonIterator".tryLoadClass()
        "xyz.acrylicstyle.dailyranking.plugin.libs.kotlin.Pair".tryLoadClass()
        "xyz.acrylicstyle.dailyranking.plugin.libs.kotlin.TuplesKt".tryLoadClass()
    }

    private fun String.tryLoadClass() {
        try {
            Class.forName(this)
        } catch (ignored: ClassNotFoundException) {}
    }

    private fun getDispatcher(): CommandDispatcher<CommandListenerWrapper> =
        (server as CraftServer).handle.server.commandDispatcher.a()

    internal fun registerCommands() {
        pleaseRegisterCommands();
        { pleaseRegisterCommands() }.schedule(5)
    }

    private fun pleaseRegisterCommands() = {
        val node = getDispatcher().register(
            literal("dailyranking")
                .requires { s -> s.bukkitSender.hasPermission("dailyrankingboard.command") }
                .then(literal("debug")
                    .then(argument("level", IntegerArgumentType.integer(0, 99999))
                        .executes { context ->
                            val level = IntegerArgumentType.getInteger(context, "level")
                            debugLevel = level
                            config["debugLevel"] = level
                            return@executes 0
                        }
                    )
                )
                .then(literal("games")
                    .then(literal("add")
                        .requires { s -> s.bukkitSender.hasPermission("dailyrankingboard.games.add") }
                        .then(argument("id", StringArgumentType.word())
                            .then(argument("name", StringArgumentType.greedyString())
                                .executes { context ->
                                    val id = StringArgumentType.getString(context, "id")
                                    val name = StringArgumentType.getString(context, "name")
                                    try {
                                        addGame(SerializableGame(id, name))
                                    } catch (e: IllegalArgumentException) {
                                        context.source.sendFailureMessage(ChatComponentText("このIDのゲームはすでに追加されています。"))
                                        return@executes 0
                                    }
                                    context.source.sendMessage(ChatComponentText("${ChatColor.GREEN}ゲーム「${ChatColor.YELLOW}$name${ChatColor.GREEN}」(ID: $id)を追加しました。"), true)
                                    return@executes 0
                                }
                            )
                        )
                    )
                )
                .then(literal("game")
                    .then(argument("game", GameArgument.gameId())
                        .suggests { _, builder -> GameArgument.fillSuggestions(builder) }
                        .then(literal("remove")
                            .requires { s -> s.bukkitSender.hasPermission("dailyrankingboard.games.remove") }
                            .executes { context ->
                                val game = GameArgument.get(context, "game")
                                if (game.game !is SerializableGame) {
                                    context.source.sendFailureMessage(ChatComponentText("${ChatColor.RED}削除できないゲームです。"))
                                } else {
                                    removeGame(game.game)
                                    context.source.sendMessage(ChatComponentText("${ChatColor.GREEN}ゲーム「${ChatColor.YELLOW}${game.name}${ChatColor.GREEN}」(ID: ${game.id})を削除しました。"), true)
                                }
                                return@executes 0
                            }
                        )
                        .then(literal("delete")
                            .requires { s -> s.bukkitSender.hasPermission("dailyrankingboard.games.delete") }
                            .executes { context ->
                                val game = GameArgument.get(context, "game")
                                if (game.game !is SerializableGame) {
                                    context.source.sendFailureMessage(ChatComponentText("${ChatColor.RED}削除できないゲームです。"))
                                } else {
                                    removeGame(game.game)
                                    File(GameConfigurationFile.GAMES_DIR, "games/${game.id}.yml").delete()
                                    context.source.sendMessage(ChatComponentText("${ChatColor.GREEN}ゲーム「${ChatColor.YELLOW}${game.name}${ChatColor.GREEN}」(ID: ${game.id})を削除しました。"), true)
                                }
                                return@executes 0
                            }
                        )
                        .then(literal("rename")
                            .requires { s -> s.bukkitSender.hasPermission("dailyrankingboard.games.rename") }
                            .then(argument("new-name", StringArgumentType.greedyString())
                                .executes { context ->
                                    val game = GameArgument.get(context, "game")
                                    if (game.game !is SerializableGame) {
                                        context.source.sendFailureMessage(ChatComponentText("変更できないゲームです。"))
                                    } else {
                                        (game.game as SerializableGame).name = StringArgumentType.getString(context, "new-name")
                                        context.source.sendMessage(ChatComponentText("${ChatColor.GREEN}ゲームの名前を「${ChatColor.YELLOW}${game.name}${ChatColor.GREEN}」に変更しました。"), true)
                                    }
                                    return@executes 0
                                }
                            )
                        )
                        .then(literal("setformat")
                            .requires { s -> s.bukkitSender.hasPermission("dailyrankingboard.games.setformat") }
                            .then(argument("format", StringArgumentType.string())
                                .executes { context ->
                                    val game = GameArgument.get(context, "game")
                                    if (game.game !is SerializableGame) {
                                        context.source.sendFailureMessage(ChatComponentText("変更できないゲームです。"))
                                    } else {
                                        val serializableGame = game.game as SerializableGame
                                        val format = StringArgumentType.getString(context, "format")
                                        try {
                                            String.format(format, 123456789)
                                        } catch (e: RuntimeException) {
                                            context.source.sendFailureMessage(ChatComponentText("表示形式の解析中にエラーが発生しました"))
                                            return@executes 0
                                        }
                                        serializableGame.format = format
                                        context.source.sendMessage(ChatComponentText("${ChatColor.GREEN}デイリーランキングボードの表示形式を変更しました。"), true)
                                        context.source.sendMessage(ChatComponentText("${ChatColor.YELLOW}表示例: ${ChatColor.RESET}${String.format(format, 123456789)}"), true)
                                        context.source.sendMessage(ChatComponentText("${ChatColor.YELLOW}初期設定に戻す場合は${ChatColor.AQUA}/dr game ${game.id} setformat \"%d\"${ChatColor.YELLOW}と入力してください。"), true)
                                    }
                                    return@executes 0
                                }
                            )
                        )
                        .then(literal("setorder")
                            .requires { s -> s.bukkitSender.hasPermission("dailyrankingboard.games.setorder") }
                            .then(argument("order", OrderArgument.order())
                                .suggests { _, builder -> OrderArgument.fillSuggestions(builder) }
                                .executes { context ->
                                    val game = GameArgument.get(context, "game")
                                    if (game.game !is SerializableGame) {
                                        context.source.sendFailureMessage(ChatComponentText("変更できないゲームです。"))
                                    } else {
                                        val serializableGame = game.game as SerializableGame
                                        val order = OrderArgument.get(context, "order")
                                        serializableGame.order = order
                                        context.source.sendMessage(ChatComponentText("${ChatColor.GREEN}ランキングボードの値の表示順序を変更しました。"), true)
                                    }
                                    return@executes 0
                                }
                            )
                        )
                        .then(literal("maps")
                            .then(literal("add")
                                .requires { s -> s.bukkitSender.hasPermission("dailyrankingboard.maps.add") }
                                .then(argument("id", StringArgumentType.word())
                                    .then(argument("name", StringArgumentType.greedyString())
                                        .executes { context ->
                                            val game = GameArgument.get(context, "game")
                                            val id = StringArgumentType.getString(context, "id")
                                            val name = StringArgumentType.getString(context, "name")
                                            try {
                                                game.registerMap(SerializableMap(id, name, mutableMapOf()))
                                            } catch (e: IllegalArgumentException) {
                                                context.source.sendFailureMessage(ChatComponentText("このIDのマップはすでに追加されています。"))
                                                return@executes 0
                                            }
                                            context.source.sendMessage(ChatComponentText("${ChatColor.GREEN}マップ「${ChatColor.YELLOW}$name${ChatColor.GREEN}」(ID: $id)を追加しました。"), true)
                                            return@executes 0
                                        }
                                    )
                                )
                            )
                        )
                        .then(literal("map")
                            .then(argument("map", StringArgumentType.word())
                                // TODO: fix MapArgument suggestions
                                // for some reason it doesn't work, maybe context passed for fillSuggestions doesn't have any arguments defined?
                                // the error is: java.lang.IllegalArgumentException: No such argument 'game' exists on this command
                                //.suggests { context, builder -> MapArgument.fillSuggestions(context, builder) }
                                .then(literal("remove")
                                    .requires { s -> s.bukkitSender.hasPermission("dailyrankingboard.maps.remove") }
                                    .executes { context ->
                                        val game = GameArgument.get(context, "game")
                                        val map = MapArgument.get(game, context, "map")
                                        if (map !is SerializableMap) {
                                            context.source.sendFailureMessage(ChatComponentText("${ChatColor.RED}削除できないマップです。"))
                                        } else {
                                            game.maps.remove(map)
                                            context.source.sendMessage(ChatComponentText("${ChatColor.GREEN}マップ「${ChatColor.YELLOW}${map.name}${ChatColor.GREEN}」(ID: ${map.id})を削除しました。"), true)
                                        }
                                        return@executes 0
                                    }
                                )
                                .then(literal("rename")
                                    .requires { s -> s.bukkitSender.hasPermission("dailyrankingboard.maps.rename") }
                                    .then(argument("new-name", StringArgumentType.greedyString())
                                        .executes { context ->
                                            val game = GameArgument.get(context, "game")
                                            val map = MapArgument.get(game, context, "map")
                                            if (map !is SerializableMap) {
                                                context.source.sendFailureMessage(ChatComponentText("変更できないゲームです。"))
                                            } else {
                                                map.name = StringArgumentType.getString(context, "new-name")
                                                context.source.sendMessage(ChatComponentText("${ChatColor.GREEN}マップの名前を「${ChatColor.YELLOW}${map.name}${ChatColor.GREEN}」に変更しました。"), true)
                                            }
                                            return@executes 0
                                        }
                                    )
                                )
                                .then(literal("leaderboard")
                                    .then(literal("resetall")
                                        .requires { s -> s.bukkitSender.hasPermission("dailyrankingboard.maps.leaderboard.resetall") }
                                        .executes { context ->
                                            val game = GameArgument.get(context, "game")
                                            val map = MapArgument.get(game, context, "map")
                                            val m = map.getLeaderboardEntries()
                                            if (m !is MutableMap<UUID, Int>) {
                                                context.source.sendFailureMessage(ChatComponentText("このマップは操作できません。"))
                                                return@executes 0
                                            }
                                            refreshLeaderboard()
                                            context.source.sendMessage(ChatComponentText("${ChatColor.GREEN}${game.id} - ${map.id}のデイリーランキングをリセットしました。"), true)
                                            return@executes 0
                                        }
                                    )
                                    .then(literal("add")
                                        .requires { s -> s.bukkitSender.hasPermission("dailyrankingboard.maps.leaderboard.add") }
                                        .then(argument("player", StringArgumentType.word())
                                            .suggests { _, builder -> ICompletionProvider.b(UserCacheFile.getUsernames(), builder) }
                                            .then(argument("value", IntegerArgumentType.integer())
                                                .executes { context ->
                                                    val game = GameArgument.get(context, "game")
                                                    val map = MapArgument.get(game, context, "map")
                                                    val id = StringArgumentType.getString(context, "player")
                                                    val value = IntegerArgumentType.getInteger(context, "value")
                                                    val uuid = UserCacheFile.getUUIDByName(id)
                                                    if (uuid == null) {
                                                        context.source.sendFailureMessage(ChatComponentText("プレイヤーが見つかりません"))
                                                    } else {
                                                        val result = map.addLeaderboardEntry(uuid, value)
                                                        if (result == -1) {
                                                            context.source.sendFailureMessage(ChatComponentText("このマップは操作できません。"))
                                                        } else {
                                                            refreshLeaderboard()
                                                        }
                                                        return@executes result
                                                    }
                                                    return@executes 0
                                                }
                                            )
                                        )
                                    )
                                    .then(literal("remove")
                                        .requires { s -> s.bukkitSender.hasPermission("dailyrankingboard.maps.leaderboard.remove") }
                                        .then(argument("player", StringArgumentType.word())
                                            .suggests { _, builder -> ICompletionProvider.b(UserCacheFile.getUsernames(), builder) }
                                            .executes { context ->
                                                val game = GameArgument.get(context, "game")
                                                val map = MapArgument.get(game, context, "map")
                                                val id = StringArgumentType.getString(context, "player")
                                                val uuid = UserCacheFile.getUUIDByName(id)
                                                if (uuid == null) {
                                                    context.source.sendFailureMessage(ChatComponentText("プレイヤーが見つかりません"))
                                                } else {
                                                    map.removeLeaderboardEntry(uuid)
                                                    refreshLeaderboard()
                                                    context.source.sendMessage(ChatComponentText("${ChatColor.GOLD}${id}${ChatColor.GREEN}の記録をすべて削除しました。"), true)
                                                }
                                                return@executes 0
                                            }
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
                .then(literal("set")
                    .then(literal("board_location")
                        .executes { context ->
                            val loc = context.source.h().bukkitEntity.location.clone();
                            {
                                config["lobby_board_location"] = loc.addY(2.8)
                                saveConfig()
                            }.runOnMain()
                            PlayerArmorStandData.refreshAll()
                            context.source.bukkitSender.sendMessage("${ChatColor.GREEN}ボードの場所を設定しました。")
                            context.source.bukkitSender.sendMessage(loc.serialize().stringify())
                            return@executes 0
                        }
                    )
                    .then(literal("game_selector_location")
                        .executes { context ->
                            val loc = context.source.h().bukkitEntity.location.clone();
                            {
                                config.set("lobby_game_selector_location", loc)
                                saveConfig()
                            }.runOnMain()
                            PlayerArmorStandData.refreshAll()
                            context.source.bukkitSender.sendMessage("${ChatColor.GREEN}ゲーム選択の場所を設定しました。")
                            context.source.bukkitSender.sendMessage(loc.serialize().stringify())
                            return@executes 0
                        }
                    )
                    .then(literal("map_selector_location")
                        .executes { context ->
                            val loc = context.source.h().bukkitEntity.location.clone();
                            {
                                config["lobby_map_selector_location"] = loc
                                saveConfig()
                            }.runOnMain()
                            PlayerArmorStandData.refreshAll()
                            context.source.bukkitSender.sendMessage("${ChatColor.GREEN}マップ選択の場所を設定しました。")
                            context.source.bukkitSender.sendMessage(loc.serialize().stringify())
                            return@executes 0
                        }
                    )
                )
        )
        (server as CraftServer).commandMap.register("dailyranking", "dailyrankingboard", CommandWrapper((server as CraftServer).handle.server.commandDispatcher, node, listOf("drb", "dr", "dailyrankingboard")))
        getDispatcher().register(literal("dr").redirect(node))
        getDispatcher().register(literal("drb").redirect(node))
        getDispatcher().register(literal("dailyrankingboard").redirect(node))
    }.schedule(1)

    private fun literal(name: String): LiteralArgumentBuilder<CommandListenerWrapper> =
        LiteralArgumentBuilder.literal(name)

    private fun <T> argument(name: String, type: ArgumentType<T>): RequiredArgumentBuilder<CommandListenerWrapper, T> =
        RequiredArgumentBuilder.argument(name, type)

    private fun <T> argument(name: String, type: ((StringReader) -> T)): RequiredArgumentBuilder<CommandListenerWrapper, T> =
        RequiredArgumentBuilder.argument(name, type)

    override fun getLogger(): Logger = super<JavaPlugin>.getLogger()
}
