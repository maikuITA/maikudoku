package com.maiku.maikudoku.domain.sudoku

import com.maiku.maikudoku.domain.model.Difficulty
import kotlin.random.Random

data class SudokuBoard(
    val cells: List<List<Int>>,
    val fixed: List<List<Boolean>>,
    val solution: List<List<Int>>
)

class SudokuGenerator(
    private val random: Random = Random.Default
) {

    fun generate(difficulty: Difficulty): SudokuBoard {
        val solvedBoard = buildSolvedBoard()
        val puzzleBoard = solvedBoard.map { it.toMutableList() }

        val allPositions = (0 until 81).shuffled(random)
        allPositions.take(difficulty.hiddenCells).forEach { index ->
            val row = index / 9
            val col = index % 9
            puzzleBoard[row][col] = 0
        }

        val fixed = puzzleBoard.map { row -> row.map { value -> value != 0 } }
        return SudokuBoard(
            cells = puzzleBoard.map { it.toList() },
            fixed = fixed,
            solution = solvedBoard
        )
    }

    private fun buildSolvedBoard(): List<List<Int>> {
        val baseDigits = (1..9).toList().shuffled(random)
        return List(9) { row ->
            List(9) { col ->
                val index = (row * 3 + row / 3 + col) % 9
                baseDigits[index]
            }
        }
    }
}

