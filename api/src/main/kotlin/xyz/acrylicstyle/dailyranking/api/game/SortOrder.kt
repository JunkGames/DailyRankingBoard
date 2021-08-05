package xyz.acrylicstyle.dailyranking.api.game

import java.util.UUID

enum class SortOrder(val sorter: Comparator<Map.Entry<UUID, Int>>) {
    ASC({ a, b -> a.value - b.value }),
    DESC({ a, b -> b.value - a.value }),
}
