# DailyRankingBoard

## How to build
`./gradlew shadowJar publishToMavenLocal shadowJar`

## How to add game using API
1. Create a class/object that implements `xyz.acrylicstyle.dailyranking.api.game.Game`
2. `val game = DailyRankingBoardAPI.getInstance().addGame(<that class/object>)`
3. `game.registerMap(<a class that implements xyz.acrylicstyle.dailyranking.api.map.GameMap>)`
