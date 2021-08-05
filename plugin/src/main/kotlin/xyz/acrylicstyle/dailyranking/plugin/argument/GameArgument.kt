package xyz.acrylicstyle.dailyranking.plugin.argument

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.v1_16_R3.ChatComponentText
import net.minecraft.server.v1_16_R3.ICompletionProvider
import xyz.acrylicstyle.dailyranking.api.game.RegisteredGame
import xyz.acrylicstyle.dailyranking.plugin.game.GameManager
import xyz.acrylicstyle.dailyranking.plugin.game.SerializableGame
import java.util.concurrent.CompletableFuture

object GameArgument {
    private val INVALID_GAME_ID = DynamicCommandExceptionType { ChatComponentText("ゲームが見つかりません: $it") }

    fun gameId(): StringArgumentType = StringArgumentType.word()

    fun get(context: CommandContext<*>, name: String): RegisteredGame =
        StringArgumentType.getString(context, name).let { s ->
            GameManager.getGames().find { it.game.id.lowercase() == s.lowercase() } ?: throw INVALID_GAME_ID.create(s)
        }

    fun fillSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        ICompletionProvider.b(GameManager.getGames().map { it.id }, builder)
}
