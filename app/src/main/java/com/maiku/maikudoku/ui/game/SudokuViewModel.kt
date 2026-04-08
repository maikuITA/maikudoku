package com.maiku.maikudoku.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.maiku.maikudoku.domain.model.CellState
import com.maiku.maikudoku.domain.model.Difficulty
import com.maiku.maikudoku.domain.sudoku.SudokuBoard
import com.maiku.maikudoku.domain.sudoku.SudokuGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val MAX_ERRORS = 4

data class CellPosition(val row: Int, val col: Int)

data class SudokuUiState(
    val difficulty: Difficulty = Difficulty.EASY,
    val board: SudokuBoard? = null,
    val gridState: List<List<CellState>> = emptyList(),
    val selectedCell: CellPosition? = null,
    val invalidCells: Set<CellPosition> = emptySet(),
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val playTimeSeconds: Long = 0L,
    val errorCount: Int = 0,
    val showGameOverDialog: Boolean = false
)

class SudokuViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val generator = SudokuGenerator()
    private val selectedDifficulty = Difficulty.fromRoute(savedStateHandle.get<String>("difficulty"))
    private var timerJob: Job? = null
    private val errorResetJobs = mutableMapOf<CellPosition, Job>()

    private val _uiState = MutableStateFlow(
        SudokuUiState(difficulty = selectedDifficulty)
    )
    val uiState: StateFlow<SudokuUiState> = _uiState.asStateFlow()
    private val _completedNumbers = MutableStateFlow<Set<Int>>(emptySet())
    val completedNumbers: StateFlow<Set<Int>> = _completedNumbers.asStateFlow()

    init {
        generateBoard()
        startTimer()
    }

    fun generateBoard() {
        viewModelScope.launch {
            cancelErrorResetJobs()
            _completedNumbers.value = emptySet()
            _uiState.update { it.copy(isLoading = true) }
            val generatedBoard = generator.generate(selectedDifficulty)
            val gridState = generatedBoard.cells.mapIndexed { rowIndex, row ->
                row.mapIndexed { colIndex, value ->
                    CellState(
                        value = value,
                        notes = emptyList(),
                        isFixed = generatedBoard.fixed[rowIndex][colIndex],
                        isError = false
                    )
                }
            }
            _uiState.update {
                it.copy(
                    board = generatedBoard,
                    gridState = gridState,
                    selectedCell = null,
                    invalidCells = emptySet(),
                    isCompleted = false,
                    isLoading = false,
                    playTimeSeconds = 0L,
                    errorCount = 0,
                    showGameOverDialog = false
                )
            }
            _completedNumbers.value = computeCompletedNumbers(gridState)
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

        var invalidSelection: CellPosition? = null
        var invalidValue: Int? = null

        _uiState.update { currentState ->
            if (currentState.showGameOverDialog || currentState.isCompleted) {
                return@update currentState
            }

            val selectedCell = currentState.selectedCell ?: return@update currentState

            val currentCell = currentState.gridState.getOrNull(selectedCell.row)?.getOrNull(selectedCell.col)
                ?: return@update currentState

            if (currentCell.isFixed) {
                return@update currentState
            }

            val isValidMove = isPlacementValid(currentState.gridState, selectedCell.row, selectedCell.col, value)
            val newGrid = currentState.gridState.map { it.toMutableList() }
            newGrid[selectedCell.row][selectedCell.col] = currentCell.copy(
                value = value,
                isError = !isValidMove
            )
            val updatedState = buildUpdatedState(currentState, newGrid)

            if (isValidMove) {
                updatedState
            } else {
                invalidSelection = selectedCell
                invalidValue = value
                val nextErrorCount = (currentState.errorCount + 1).coerceAtMost(MAX_ERRORS)
                updatedState.copy(
                    errorCount = nextErrorCount,
                    showGameOverDialog = nextErrorCount >= MAX_ERRORS
                )
            }
        }

        val targetCell = invalidSelection
        val targetValue = invalidValue
        if (targetCell != null && targetValue != null) {
            scheduleInvalidCellReset(targetCell, targetValue)
        }
        refreshCompletedNumbers()
    }

    fun clearSelectedCell() {
        _uiState.update { currentState ->
            if (currentState.showGameOverDialog) {
                return@update currentState
            }

            val selectedCell = currentState.selectedCell ?: return@update currentState

            val currentCell = currentState.gridState.getOrNull(selectedCell.row)?.getOrNull(selectedCell.col)
                ?: return@update currentState

            if (currentCell.isFixed) {
                return@update currentState
            }

            val newGrid = currentState.gridState.map { it.toMutableList() }
            newGrid[selectedCell.row][selectedCell.col] = currentCell.copy(
                value = 0,
                isError = false
            )
            buildUpdatedState(currentState, newGrid)
        }
        refreshCompletedNumbers()
    }

    private fun startTimer() {
        if (timerJob != null) return
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { currentState ->
                    if (
                        currentState.board == null ||
                        currentState.isCompleted ||
                        currentState.isLoading ||
                        currentState.showGameOverDialog
                    ) {
                        currentState
                    } else {
                        currentState.copy(playTimeSeconds = currentState.playTimeSeconds + 1)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        cancelErrorResetJobs()
    }

    private fun scheduleInvalidCellReset(position: CellPosition, invalidValue: Int) {
        errorResetJobs.remove(position)?.cancel()

        errorResetJobs[position] = viewModelScope.launch {
            delay(5000L)

            _uiState.update { currentState ->
                val row = currentState.gridState.getOrNull(position.row) ?: return@update currentState
                val currentCell = row.getOrNull(position.col) ?: return@update currentState

                if (!currentCell.isError || currentCell.value != invalidValue || currentCell.isFixed) {
                    return@update currentState
                }

                val newGrid = currentState.gridState.map { it.toMutableList() }
                newGrid[position.row][position.col] = currentCell.copy(
                    value = 0,
                    isError = false
                )

                buildUpdatedState(currentState, newGrid)
            }
            refreshCompletedNumbers()
            errorResetJobs.remove(position)
        }
    }

    private fun cancelErrorResetJobs() {
        errorResetJobs.values.forEach { it.cancel() }
        errorResetJobs.clear()
    }

    private fun refreshCompletedNumbers() {
        _completedNumbers.value = computeCompletedNumbers(_uiState.value.gridState)
    }


    private fun buildUpdatedState(
        currentState: SudokuUiState,
        gridData: List<MutableList<CellState>>
    ): SudokuUiState {
        val updatedGrid = gridData.map { it.toList() }
        val updatedBoard = updatedGrid.map { row -> row.map { it.value } }
        val invalidCells = computeInvalidCells(updatedBoard)
        val isCompleted = invalidCells.isEmpty() && updatedBoard.all { row -> row.all { it != 0 } }

        return currentState.copy(
            board = currentState.board?.copy(cells = updatedBoard),
            gridState = updatedGrid,
            invalidCells = invalidCells,
            isCompleted = isCompleted
        )
    }

    private fun isPlacementValid(
        grid: List<List<CellState>>,
        row: Int,
        col: Int,
        value: Int
    ): Boolean {
        if (value == 0) return true

        val rowHasConflict = grid[row].anyIndexed { currentCol, cell ->
            currentCol != col && cell.value == value
        }
        if (rowHasConflict) return false

        val colHasConflict = grid.anyIndexed { currentRow, rowCells ->
            currentRow != row && rowCells[col].value == value
        }
        if (colHasConflict) return false

        val startRow = (row / 3) * 3
        val startCol = (col / 3) * 3
        for (r in startRow until startRow + 3) {
            for (c in startCol until startCol + 3) {
                if ((r != row || c != col) && grid[r][c].value == value) return false
            }
        }

        return true
    }

    private inline fun <T> List<T>.anyIndexed(predicate: (index: Int, T) -> Boolean): Boolean {
        for (index in indices) {
            if (predicate(index, this[index])) return true
        }
        return false
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

    private fun computeCompletedNumbers(grid: List<List<CellState>>): Set<Int> {
        if (grid.isEmpty()) return emptySet()

        val counts = IntArray(10)
        grid.forEach { row ->
            row.forEach { cell ->
                val value = cell.value
                if (value in 1..9 && !cell.isError) {
                    counts[value]++
                }
            }
        }

        return (1..9)
            .filterTo(mutableSetOf()) { number -> counts[number] >= 9 }
    }
}

