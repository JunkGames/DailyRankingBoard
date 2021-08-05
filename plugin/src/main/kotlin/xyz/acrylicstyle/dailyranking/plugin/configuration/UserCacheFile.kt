package xyz.acrylicstyle.dailyranking.plugin.configuration

import util.yaml.YamlConfiguration
import util.yaml.YamlObject
import xyz.acrylicstyle.dailyranking.plugin.DailyRankingBoardPlugin
import java.io.File
import java.io.IOException
import java.util.UUID

object UserCacheFile {
    private val file = File(DailyRankingBoardPlugin.instance.dataFolder, "user_cache.yml")
    private val cache = YamlObject()

    private fun checkFile() {
        if (!file.exists()) {
            if (!file.createNewFile()) {
                DailyRankingBoardPlugin.instance.logger.warning("Failed to create user_cache.yml")
            } else {
                DailyRankingBoardPlugin.debug("Created user_cache.yml")
            }
        }
    }

    fun load() {
        checkFile()
        cache.rawData.clear()
        try {
            cache.rawData.putAll(YamlConfiguration(file).asObject().rawData)
        } catch (e: Exception) {
            DailyRankingBoardPlugin.instance.logger.warning("Failed to load user_cache.yml")
            e.printStackTrace()
        }
        DailyRankingBoardPlugin.instance.logger.info("Loaded ${cache.rawData.size} entries from user_cache.yml")
    }

    fun write() {
        checkFile()
        try {
            YamlConfiguration.saveTo(file, cache)
        } catch (e: IOException) {
            DailyRankingBoardPlugin.instance.logger.warning("Failed to save user_cache.yml")
            e.printStackTrace()
        }
    }

    fun getUsernames() = cache.rawData.values.filterIsInstance<String>()

    fun getUUIDByName(name: String): UUID? = cache.rawData.entries.find { entry -> entry.value == name }?.let { UUID.fromString(it.key) }

    operator fun set(uuid: UUID, name: String) {
        cache.rawData
            .entries
            .filter { entry -> entry.value == name }
            .forEach { cache.set(it.key, null) }
        cache.set(uuid.toString(), name)
    }

    operator fun get(uuid: UUID): String? = cache.getString(uuid.toString())

    fun clear() = cache.rawData.clear()
}
