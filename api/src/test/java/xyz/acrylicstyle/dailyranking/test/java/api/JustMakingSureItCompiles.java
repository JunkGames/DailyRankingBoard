package xyz.acrylicstyle.dailyranking.test.java.api;

import xyz.acrylicstyle.dailyranking.api.DailyRankingBoardAPI;
import xyz.acrylicstyle.dailyranking.test.java.api.game.GameImpl;

@SuppressWarnings("unused")
public class JustMakingSureItCompiles {
    private static void doSomething() {
        DailyRankingBoardAPI api = DailyRankingBoardAPI.getInstance();
        api.addGame(new GameImpl());
        api.getGames();
    }
}
