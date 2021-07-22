package xyz.acrylicstyle.dailyranking.plugin.game

import util.yaml.YamlObject
import xyz.acrylicstyle.dailyranking.api.game.Game

data class SerializableGame(override val id: String, override var name: String, var format: String = "%d"): Game {
    companion object {
        fun deserialize(obj: YamlObject): SerializableGame {
            val id = obj.getString("id") ?: error("id is not defined")
            val name = obj.getString("name") ?: error("name is not defined")
            val format = obj.getString("format", "%d")
            return SerializableGame(id, name, format)
        }
    }

    override fun getAsMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "format" to format,
    )

    override fun getValueToStringFunction(value: Int): String = String.format(format, value)
}
