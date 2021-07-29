package xyz.acrylicstyle.dailyranking.plugin.configuration

import util.yaml.YamlConfiguration
import util.yaml.YamlObject
import xyz.acrylicstyle.dailyranking.api.game.RegisteredGame
import xyz.acrylicstyle.dailyranking.plugin.DailyRankingBoardPlugin
import xyz.acrylicstyle.dailyranking.plugin.DailyRankingBoardPlugin.Companion.debug
import xyz.acrylicstyle.dailyranking.plugin.game.GameManager
import xyz.acrylicstyle.dailyranking.plugin.game.SerializableGame
import xyz.acrylicstyle.dailyranking.plugin.game.SerializableMap
import java.io.File

object GameConfigurationFile {
    private val MAPS_DIR = File(DailyRankingBoardPlugin.instance.dataFolder, "maps")

    init {
        if (MAPS_DIR.exists() && MAPS_DIR.isFile) {
            error("${MAPS_DIR.absolutePath} exists but is a file. Please delete the file and try again.")
        }
        if (!MAPS_DIR.exists() && !MAPS_DIR.mkdirs()) {
            error("Failed to create maps directory. Please check for file permissions.")
        }
    }

    fun loadAll() {
        MAPS_DIR.listFiles { f -> f.extension == "yml" }?.forEach { file ->
            try {
                debug("Loading map ${file.name}")
                loadFromObject(YamlConfiguration(file).asObject())
            } catch (e: Exception) {
                DailyRankingBoardPlugin.instance.logger.severe("Error loading game ${file.path}")
                e.printStackTrace()
            }
        }
    }

    private fun loadFromObject(obj: YamlObject): RegisteredGame {
        val serializableGame = SerializableGame.deserialize(obj)
        val registeredGame = DailyRankingBoardPlugin.instance.addGame(serializableGame)
        obj.getArray("maps").forEachAsType<Map<String, Any>> {
            val o = YamlObject(it)
            registeredGame.registerMap(SerializableMap.deserialize(o))
        }
        return registeredGame
    }

    fun saveAll() {
        GameManager.getGames().filter { it.game is SerializableGame }.forEach { game ->
            val obj = try {
                serializeGame(game)
            } catch (e: Exception) {
                DailyRankingBoardPlugin.instance.logger.severe("Failed to serialize game ${game.id}")
                return@forEach
            }
            try {
                obj.save(File(MAPS_DIR, "${game.id}.yml"))
            } catch (e: Exception) {
                DailyRankingBoardPlugin.instance.logger.severe("Error saving game ${game.id}")
                DailyRankingBoardPlugin.instance.logger.severe("File contents:\n${obj.dump()}")
                e.printStackTrace()
            }
        }
    }

    private fun serializeGame(registeredGame: RegisteredGame): YamlObject {
        if (registeredGame.game !is SerializableGame) throw IllegalArgumentException("not serializable")
        val obj = YamlObject(registeredGame.game.getAsMap())
        obj.set("maps", registeredGame.maps.filterIsInstance<SerializableMap>().map { it.getAsMap() })
        return obj
    }
}
