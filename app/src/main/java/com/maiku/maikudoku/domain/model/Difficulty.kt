package com.maiku.maikudoku.domain.model

enum class Difficulty(
    val routeValue: String,
    val hiddenCells: Int,
    val displayName: String
) {
    EASY(routeValue = "easy", hiddenCells = 35, displayName = "Facile"),
    INTERMEDIATE(routeValue = "intermediate", hiddenCells = 45, displayName = "Intermedio"),
    HARD(routeValue = "hard", hiddenCells = 55, displayName = "Difficile");

    companion object {
        fun fromRoute(value: String?): Difficulty {
            return entries.firstOrNull { it.routeValue == value } ?: EASY
        }
    }
}

