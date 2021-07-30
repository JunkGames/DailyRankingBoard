package xyz.acrylicstyle.dailyranking.plugin.util

import com.google.common.base.Joiner
import com.mojang.brigadier.tree.CommandNode
import net.minecraft.server.v1_16_R3.CommandDispatcher
import net.minecraft.server.v1_16_R3.CommandListenerWrapper
import org.apache.commons.lang.Validate
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.ProxiedCommandSender
import org.bukkit.command.RemoteConsoleCommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.craftbukkit.v1_16_R3.CraftServer
import org.bukkit.craftbukkit.v1_16_R3.command.CraftBlockCommandSender
import org.bukkit.craftbukkit.v1_16_R3.command.ProxiedNativeCommandSender
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftMinecartCommand
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.entity.minecart.CommandMinecart

class CommandWrapper(
    private val dispatcher: CommandDispatcher,
    vanillaCommand: CommandNode<CommandListenerWrapper?>,
    aliases: List<String>,
) : BukkitCommand(
    vanillaCommand.name,
    "DailyRankingBoard command",
    vanillaCommand.usageText,
    aliases,
) {
    override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
        if (testPermission(sender)) {
            val icommandlistener = getListener(sender)
            dispatcher.a(icommandlistener, toDispatcher(args, name), toDispatcher(args, commandLabel), true)
        }
        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>, location: Location?): List<String> {
        Validate.notNull(sender, "Sender cannot be null")
        Validate.notNull(args, "Arguments cannot be null")
        Validate.notNull(alias, "Alias cannot be null")
        val icommandlistener = getListener(sender)
        val parsed = dispatcher.a().parse(toDispatcher(args, name), icommandlistener)
        val results = ArrayList<String>()
        dispatcher.a().getCompletionSuggestions(parsed).thenAccept { suggestions -> suggestions.list.forEach { s -> results.add(s.text) } }
        return results
    }

    private fun toDispatcher(args: Array<String>, name: String): String =
        name + if (args.isNotEmpty()) " " + Joiner.on(' ').join(args) else ""

    companion object {
        fun getListener(sender: CommandSender): CommandListenerWrapper {
            return when (sender) {
                is Player -> (sender as CraftPlayer).handle.commandListener
                is BlockCommandSender -> (sender as CraftBlockCommandSender).wrapper
                is CommandMinecart -> (sender as CraftMinecartCommand).handle.commandBlock.wrapper
                is RemoteConsoleCommandSender -> (Bukkit.getServer() as CraftServer).handle.server.remoteControlCommandListener.wrapper
                is ConsoleCommandSender -> (sender.getServer() as CraftServer).server.serverCommandListener
                is ProxiedCommandSender -> (sender as ProxiedNativeCommandSender).handle
                else -> throw IllegalArgumentException("Cannot make $sender a vanilla command listener")
            }
        }
    }

    override fun testPermissionSilent(target: CommandSender): Boolean =
        target is ConsoleCommandSender || target.hasPermission("dailyrankingboard.command")

    init {
        permission = "dailyrankingboard.command"
    }
}
