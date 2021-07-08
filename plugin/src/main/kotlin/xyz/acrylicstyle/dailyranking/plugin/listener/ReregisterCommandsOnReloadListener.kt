package xyz.acrylicstyle.dailyranking.plugin.listener

import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import xyz.acrylicstyle.dailyranking.plugin.DailyRankingBoardPlugin

object ReregisterCommandsOnReloadListener: EventListener2<PluginEnableEvent, PluginDisableEvent> {
    override fun handle1(e: PluginEnableEvent) {
        DailyRankingBoardPlugin.instance.registerCompletions()
    }

    override fun handle2(e: PluginDisableEvent) {
        DailyRankingBoardPlugin.instance.registerCompletions()
    }
}
