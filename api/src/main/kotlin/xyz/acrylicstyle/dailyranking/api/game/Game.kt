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

    override fun getAsMap(): Map<*, *> = mapOf(
        "id" to id,
        "name" to name,
    )
}
