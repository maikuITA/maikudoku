package com.maiku.maikudoku

import com.maiku.maikudoku.domain.model.Difficulty
import com.maiku.maikudoku.domain.sudoku.SudokuGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuGeneratorTest {

    private val generator = SudokuGenerator()

    @Test
    fun generate_returns9x9Board() {
        val board = generator.generate(Difficulty.EASY)

        assertEquals(9, board.cells.size)
        assertTrue(board.cells.all { it.size == 9 })
        assertEquals(9, board.fixed.size)
        assertTrue(board.fixed.all { it.size == 9 })
    }

    @Test
    fun generate_hard_hasMoreEmptyCellsThanEasy() {
        val easy = generator.generate(Difficulty.EASY)
        val hard = generator.generate(Difficulty.HARD)

        val easyEmpty = easy.cells.sumOf { row -> row.count { it == 0 } }
        val hardEmpty = hard.cells.sumOf { row -> row.count { it == 0 } }

        assertTrue(hardEmpty > easyEmpty)
    }
}

