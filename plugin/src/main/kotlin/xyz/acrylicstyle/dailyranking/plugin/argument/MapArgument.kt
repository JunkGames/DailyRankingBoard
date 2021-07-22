package xyz.acrylicstyle.dailyranking.plugin.argument

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.v1_16_R3.ChatComponentText
import net.minecraft.server.v1_16_R3.ICompletionProvider
import xyz.acrylicstyle.dailyranking.api.game.RegisteredGame
import xyz.acrylicstyle.dailyranking.api.map.GameMap
import xyz.acrylicstyle.dailyranking.plugin.game.SerializableMap
import java.util.concurrent.CompletableFuture

object MapArgument {
    private val INVALID_MAP_ID = DynamicCommandExceptionType { ChatComponentText("マップが見つかりません: $it") }

    fun get(game: RegisteredGame, context: CommandContext<*>, name: String): GameMap =
        StringArgumentType.getString(context, name).let { s ->
            game.maps.find { it.id.lowercase() == s.lowercase() } ?: throw INVALID_MAP_ID.create(s)
        }

    fun fillSuggestions(context: CommandContext<*>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        ICompletionProvider.b(GameArgument.get(context, "game").maps.filterIsInstance<SerializableMap>().map { it.id }, builder)
}
