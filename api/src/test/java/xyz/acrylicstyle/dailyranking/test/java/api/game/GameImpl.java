package xyz.acrylicstyle.dailyranking.test.java.api.game;

import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.dailyranking.api.game.Game;

import java.util.Map;

public class GameImpl implements Game {
    @NotNull
    @Override
    public String getId() {
        return "test game";
    }

    @NotNull
    @Override
    public String getName() {
        return "Test game";
    }

    @NotNull
    @Override
    public Map<?, ?> getAsMap() {
        return Game.super.getAsMap();
    }
}
