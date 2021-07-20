package xyz.acrylicstyle.dailyranking.plugin.game

import xyz.acrylicstyle.dailyranking.api.game.Game

data class SerializableGame(override val id: String, override val name: String): Game
