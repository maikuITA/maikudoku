package com.maiku.maikudoku.domain.model

enum class Difficulty(
    val routeValue: String,
    val hiddenCells: Int,
    val displayName: String
) {
    EASY(routeValue = "easy", hiddenCells = 35, displayName = "Easy"),
    INTERMEDIATE(routeValue = "intermediate", hiddenCells = 45, displayName = "Medium"),
    HARD(routeValue = "hard", hiddenCells = 55, displayName = "Hard"),
    HARD2(routeValue = "hard2", hiddenCells = 65, displayName = "Really Hard"),
    HARD3(routeValue = "hard3", hiddenCells = 75, displayName = "Super Hard");

    companion object {
        fun fromRoute(value: String?): Difficulty {
            return entries.firstOrNull { it.routeValue == value } ?: EASY
        }
    }
}

