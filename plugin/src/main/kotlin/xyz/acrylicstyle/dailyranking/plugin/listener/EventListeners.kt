package xyz.acrylicstyle.dailyranking.plugin.listener

import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

interface EventListener<T: Event>: Listener {
    fun handle(e: T)

    @EventHandler
    fun doHandle(e: T) = handle(e)
}

interface EventListener2<T1: Event, T2: Event>: Listener {
    fun handle1(e: T1)
    fun handle2(e: T2)

    @EventHandler
    fun doHandle1(e: T1) = handle1(e)

    @EventHandler
    fun doHandle2(e: T2) = handle2(e)
}
