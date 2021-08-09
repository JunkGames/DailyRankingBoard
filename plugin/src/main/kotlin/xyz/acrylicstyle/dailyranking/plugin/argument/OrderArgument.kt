package xyz.acrylicstyle.dailyranking.plugin.argument

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.ICompletionProvider
import net.minecraft.network.chat.ChatComponentText
import xyz.acrylicstyle.dailyranking.api.game.SortOrder
import java.util.concurrent.CompletableFuture

object OrderArgument {
    private val INVALID_ORDER = DynamicCommandExceptionType { ChatComponentText("Unknown sort order: '$it'") }

    fun order(): StringArgumentType = StringArgumentType.word()

    fun get(context: CommandContext<*>, name: String): SortOrder =
        StringArgumentType.getString(context, name).let { s ->
            SortOrder.values().find { it.name == s } ?: throw INVALID_ORDER.create(s)
        }

    fun fillSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        ICompletionProvider.b(SortOrder.values().map { it.name }, builder)
}
