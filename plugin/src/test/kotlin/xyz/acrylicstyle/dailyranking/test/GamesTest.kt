package xyz.acrylicstyle.dailyranking.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.acrylicstyle.dailyranking.test.game.NotFunnyGame
import xyz.acrylicstyle.dailyranking.test.game.VeryFunnyGame

class GamesTest {
    private val api = DailyRankingBoardAPIDummyImpl

    init {
        api.addGame(VeryFunnyGame)
        api.addGame(NotFunnyGame)
        assert(api.getGames().size == 2)
    }

    @Test
    fun testThrowErrorOnDuplicate() {
        assertThrows<IllegalArgumentException> {
            api.addGame(VeryFunnyGame)
        }
    }
}
