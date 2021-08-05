package xyz.acrylicstyle.dailyranking.plugin.game

import util.yaml.YamlObject
import xyz.acrylicstyle.dailyranking.api.game.Game
import xyz.acrylicstyle.dailyranking.api.game.SortOrder

data class SerializableGame(
    override val id: String,
    override var name: String,
    var format: String = "%d",
    override var order: SortOrder = SortOrder.ASC,
): Game {
    companion object {
        fun deserialize(obj: YamlObject): SerializableGame {
            val id = obj.getString("id") ?: error("id is not defined")
            val name = obj.getString("name") ?: error("name is not defined")
            val format = obj.getString("format", "%d")
            val order = SortOrder.valueOf(obj.getString("order", "ASC"))
            return SerializableGame(id, name, format, order)
        }
    }

    override fun getAsMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "format" to format,
        "order" to order.name,
    )

    override fun getValueToStringFunction(value: Int): String = String.format(format, value)
}
