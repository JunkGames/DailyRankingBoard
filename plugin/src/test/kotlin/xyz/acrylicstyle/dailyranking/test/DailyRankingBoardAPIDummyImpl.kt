package xyz.acrylicstyle.dailyranking.test

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import xyz.acrylicstyle.dailyranking.plugin.DailyRankingBoardAPIImpl

object DailyRankingBoardAPIDummyImpl: DailyRankingBoardAPIImpl {
    override fun onTabComplete(p0: CommandSender, p1: Command, p2: String, p3: Array<String>) = throw UnsupportedOperationException()
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<String>) = throw UnsupportedOperationException()
    override fun getDataFolder() = throw UnsupportedOperationException()
    override fun getDescription() = throw UnsupportedOperationException()
    override fun getConfig() = throw UnsupportedOperationException()
    override fun getResource(p0: String) = throw UnsupportedOperationException()
    override fun saveConfig() = throw UnsupportedOperationException()
    override fun saveDefaultConfig() = throw UnsupportedOperationException()
    override fun saveResource(p0: String, p1: Boolean) = throw UnsupportedOperationException()
    override fun reloadConfig() = throw UnsupportedOperationException()
    override fun getPluginLoader() = throw UnsupportedOperationException()
    override fun getServer() = throw UnsupportedOperationException()
    override fun isEnabled() = throw UnsupportedOperationException()
    override fun onDisable() = throw UnsupportedOperationException()
    override fun onLoad() = throw UnsupportedOperationException()
    override fun onEnable() = throw UnsupportedOperationException()
    override fun isNaggable() = throw UnsupportedOperationException()
    override fun setNaggable(p0: Boolean) = throw UnsupportedOperationException()
    override fun getDefaultWorldGenerator(p0: String, p1: String?) = throw UnsupportedOperationException()
    override fun getName() = throw UnsupportedOperationException()
}
