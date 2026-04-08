package com.maiku.maikudoku.domain.model

data class CellState(
    val value: Int,
    val notes: List<Int> = emptyList(),
    val isFixed: Boolean,
    val isError: Boolean = false
)

data class GameSaveState(
    val difficulty: String,
    val playTimeSeconds: Long,
    val gridState: List<List<CellState>>
)

