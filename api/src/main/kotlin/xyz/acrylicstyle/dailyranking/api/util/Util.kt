package xyz.acrylicstyle.dailyranking.api.util

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta

object Util {
    fun <T> T?.or(value: T) = this ?: value

    private fun Any?.stringifyKey() = ChatColor.LIGHT_PURPLE.toString() + when (this) {
        is String -> "${ChatColor.AQUA}$this"
        is Player -> "<Player: ${this.name}>"
        is ConsoleCommandSender -> "<ConsoleCommandSender>"
        is BlockCommandSender -> "<BlockCommandSender>"
        is Entity -> "<Entity: ${this.name}/${this.uniqueId}>"
        is Location -> "<Location: ${formatMap(this.serialize())}>"
        is Block -> "<Block: ${formatMap(this.location.serialize())}>"
        else -> this
    }

    fun Any?.stringify(stripColor: Boolean) = if (stripColor) ChatColor.stripColor(this.stringify()) else this.stringify()

    fun Any?.stringify() = when (this) {
        is String -> "${ChatColor.WHITE}\"${ChatColor.GREEN}$this${ChatColor.WHITE}\""
        is Int -> "${ChatColor.GOLD}${this.toString(10)}"
        is Long -> "${ChatColor.GOLD}$this${ChatColor.RED}L"
        is Short -> "${ChatColor.GOLD}$this${ChatColor.RED}s"
        is Boolean -> "${ChatColor.GOLD}$this"
        is Byte -> "${ChatColor.BOLD}$this${ChatColor.RED}b"
        is Double -> "${ChatColor.GOLD}${if (this.toString().contains(".")) this else "$this.0"}${ChatColor.RED}d"
        is Float -> "${ChatColor.GOLD}${if (this.toString().contains(".")) this else "$this.0"}${ChatColor.RED}f"
        is Char -> "${ChatColor.WHITE}'${ChatColor.GOLD}$this${ChatColor.WHITE}'"
        is Iterable<*> -> formatList(this.toList())
        is IntArray -> formatList(this.toList())
        is ShortArray -> formatList(this.toList())
        is LongArray -> formatList(this.toList())
        is ByteArray -> formatList(this.toList())
        is FloatArray -> formatList(this.toList())
        is DoubleArray -> formatList(this.toList())
        is BooleanArray -> formatList(this.toList())
        is CharArray -> formatList(this.toList())
        is Array<*> -> formatList(this.toList())
        is Map<*, *> -> formatMap(this)
        is MapSerializable -> formatMap(this.getAsMap())
        is Entity -> "${ChatColor.BLUE}${this.uniqueId}"
        is ItemMeta -> formatMap(this.serialize())
        null -> "${ChatColor.GRAY}null"
        else -> "${ChatColor.YELLOW}$this"
    }

    private fun formatMap(map: Map<*, *>): String {
        val sb = StringBuilder().append("${ChatColor.WHITE}{")
        var notFirst = false
        map.forEach { (key, value) ->
            if (notFirst) {
                sb.append("${ChatColor.WHITE}, ")
            }
            sb.append("${ChatColor.AQUA}${key.stringifyKey()}${ChatColor.WHITE}: ${value.stringify()}")
            notFirst = true
        }
        return sb.append("${ChatColor.WHITE}}").toString()
    }

    private fun formatList(map: List<*>): String {
        val sb = StringBuilder().append("${ChatColor.WHITE}[")
        var notFirst = false
        map.forEach { value ->
            if (notFirst) {
                sb.append("${ChatColor.WHITE}, ")
            }
            sb.append(value.stringify())
            notFirst = true
        }
        return sb.append("${ChatColor.WHITE}]").toString()
    }

    /**
     * Ensure the [CommandSender] is a [Player].
     * @return [Player] if [CommandSender] was player, null otherwise.
     */
    fun CommandSender.ensurePlayer(): Player? {
        if (this !is Player) {
            this.sendMessage(ChatColor.RED.toString() + "This command cannot be invoked from console.")
            return null
        }
        return this
    }

    fun getFirstWorld(): World = Bukkit.getWorlds()[0]
}
