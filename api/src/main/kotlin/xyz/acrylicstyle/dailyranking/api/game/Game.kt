package xyz.acrylicstyle.dailyranking.api.game

import xyz.acrylicstyle.dailyranking.api.util.MapSerializable

interface Game: MapSerializable {
    /**
     * @return the game id used internally to manage games.
     */
    val id: String

    /**
     * @return the game name that would be displayed to the player.
     */
    val name: String

    fun getValueToStringFunction(value: Int): String = value.toString()

    override fun getAsMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
    )
}
