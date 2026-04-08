package com.maiku.maikudoku.ui.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maiku.maikudoku.domain.model.Difficulty
import com.maiku.maikudoku.domain.sudoku.SudokuBoard
import com.maiku.maikudoku.domain.sudoku.SudokuGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CellPosition(val row: Int, val col: Int)

data class SudokuUiState(
    val difficulty: Difficulty = Difficulty.EASY,
    val board: SudokuBoard? = null,
    val userBoard: List<List<Int>> = emptyList(),
    val selectedCell: CellPosition? = null,
    val invalidCells: Set<CellPosition> = emptySet(),
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false
)

class SudokuViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val generator = SudokuGenerator()
    private val selectedDifficulty = Difficulty.fromRoute(savedStateHandle.get<String>("difficulty"))

    private val _uiState = MutableStateFlow(
        SudokuUiState(difficulty = selectedDifficulty)
    )
    val uiState: StateFlow<SudokuUiState> = _uiState.asStateFlow()

    init {
        generateBoard()
    }

    fun generateBoard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val generatedBoard = generator.generate(selectedDifficulty)
            _uiState.update {
                it.copy(
                    board = generatedBoard,
                    userBoard = generatedBoard.cells,
                    selectedCell = null,
                    invalidCells = emptySet(),
                    isCompleted = false,
                    isLoading = false
                )
            }
        }
    }

    fun selectCell(row: Int, col: Int) {
        _uiState.update { currentState ->
            if (currentState.board == null) return@update currentState
            currentState.copy(selectedCell = CellPosition(row = row, col = col))
        }
    }

    fun setCellValue(value: Int) {
        if (value !in 1..9) return

        _uiState.update { currentState ->
            val board = currentState.board ?: return@update currentState
            val selectedCell = currentState.selectedCell ?: return@update currentState

            if (board.fixed[selectedCell.row][selectedCell.col]) {
                return@update currentState
            }

            val newBoard = currentState.userBoard.map { it.toMutableList() }
            newBoard[selectedCell.row][selectedCell.col] = value
            buildUpdatedState(currentState, newBoard)
        }
    }

    fun clearSelectedCell() {
        _uiState.update { currentState ->
            val board = currentState.board ?: return@update currentState
            val selectedCell = currentState.selectedCell ?: return@update currentState

            if (board.fixed[selectedCell.row][selectedCell.col]) {
                return@update currentState
            }

            val newBoard = currentState.userBoard.map { it.toMutableList() }
            newBoard[selectedCell.row][selectedCell.col] = 0
            buildUpdatedState(currentState, newBoard)
        }
    }

    private fun buildUpdatedState(
        currentState: SudokuUiState,
        boardData: List<MutableList<Int>>
    ): SudokuUiState {
        val updatedBoard = boardData.map { it.toList() }
        val invalidCells = computeInvalidCells(updatedBoard)
        val isCompleted = invalidCells.isEmpty() && updatedBoard.all { row -> row.all { it != 0 } }

        return currentState.copy(
            userBoard = updatedBoard,
            invalidCells = invalidCells,
            isCompleted = isCompleted
        )
    }

    private fun computeInvalidCells(
        current: List<List<Int>>
    ): Set<CellPosition> {
        val invalid = mutableSetOf<CellPosition>()

        for (row in 0 until 9) {
            val positionsByValue = mutableMapOf<Int, MutableList<CellPosition>>()
            for (col in 0 until 9) {
                val value = current[row][col]
                if (value != 0) {
                    positionsByValue.getOrPut(value) { mutableListOf() }
                        .add(CellPosition(row = row, col = col))
                }
            }
            positionsByValue.values.filter { it.size > 1 }.forEach { invalid.addAll(it) }
        }

        for (col in 0 until 9) {
            val positionsByValue = mutableMapOf<Int, MutableList<CellPosition>>()
            for (row in 0 until 9) {
                val value = current[row][col]
                if (value != 0) {
                    positionsByValue.getOrPut(value) { mutableListOf() }
                        .add(CellPosition(row = row, col = col))
                }
            }
            positionsByValue.values.filter { it.size > 1 }.forEach { invalid.addAll(it) }
        }

        for (blockRow in 0 until 3) {
            for (blockCol in 0 until 3) {
                val positionsByValue = mutableMapOf<Int, MutableList<CellPosition>>()
                val startRow = blockRow * 3
                val startCol = blockCol * 3

                for (row in startRow until startRow + 3) {
                    for (col in startCol until startCol + 3) {
                        val value = current[row][col]
                        if (value != 0) {
                            positionsByValue.getOrPut(value) { mutableListOf() }
                                .add(CellPosition(row = row, col = col))
                        }
                    }
                }

                positionsByValue.values.filter { it.size > 1 }.forEach { invalid.addAll(it) }
            }
        }

        return invalid
    }
}

