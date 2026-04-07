package com.maiku.maikudoku.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maiku.maikudoku.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GameScreen(
    modifier: Modifier = Modifier,
    onNavigateHome: () -> Unit = {},
    viewModel: SudokuViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val board = uiState.board
    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    TextButton(onClick = { showExitDialog = true }) {
                        Text(text = stringResource(id = R.string.game_home_button))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(
                    id = R.string.game_difficulty_label,
                    uiState.difficulty.displayName
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> CircularProgressIndicator()
                board != null && uiState.userBoard.isNotEmpty() -> {
                    SudokuGrid(
                        board = uiState.userBoard,
                        fixed = board.fixed,
                        selectedCell = uiState.selectedCell,
                        invalidCells = uiState.invalidCells,
                        onCellClick = viewModel::selectCell
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    NumberPad(
                        onValueSelected = viewModel::setCellValue,
                        onClear = viewModel::clearSelectedCell
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = viewModel::generateBoard) {
                        Text(text = stringResource(id = R.string.game_new_grid_button))
                    }

                    if (uiState.isCompleted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(id = R.string.game_completed_message),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else if (uiState.invalidCells.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(id = R.string.game_invalid_moves_message),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> Text(text = stringResource(id = R.string.game_grid_generation_error))
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(text = stringResource(id = R.string.game_exit_dialog_title)) },
            text = { Text(text = stringResource(id = R.string.game_exit_dialog_message)) },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(text = stringResource(id = R.string.game_exit_dialog_cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onNavigateHome()
                }) {
                    Text(text = stringResource(id = R.string.game_exit_dialog_confirm))
                }
            }
        )
    }
}

@Composable
private fun SudokuGrid(
    board: List<List<Int>>,
    fixed: List<List<Boolean>>,
    selectedCell: CellPosition?,
    invalidCells: Set<CellPosition>,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(width = 2.dp, color = MaterialTheme.colorScheme.primary)
            .padding(4.dp)
    ) {
        board.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                row.forEachIndexed { colIndex, value ->
                    val isFixed = fixed[rowIndex][colIndex]
                    val position = CellPosition(row = rowIndex, col = colIndex)
                    SudokuCell(
                        value = value,
                        isFixed = isFixed,
                        isSelected = selectedCell == position,
                        isRelatedToSelection = isRelatedToSelection(selectedCell, position),
                        isInvalid = position in invalidCells,
                        onClick = { onCellClick(rowIndex, colIndex) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SudokuCell(
    value: Int,
    isFixed: Boolean,
    isSelected: Boolean,
    isRelatedToSelection: Boolean,
    isInvalid: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isInvalid -> Color(0xFFFFD7D7)
        isSelected -> Color(0xFFDDEBFF)
        isRelatedToSelection -> Color(0xFFF3F7FF)
        isFixed -> Color(0xFFEAEAEA)
        else -> Color.White
    }

    Box(
        modifier = modifier
            .padding(1.dp)
            .aspectRatio(1f)
            .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(2.dp))
            .background(color = backgroundColor, shape = RoundedCornerShape(2.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (value == 0) "" else value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isFixed) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun isRelatedToSelection(
    selectedCell: CellPosition?,
    currentCell: CellPosition
): Boolean {
    if (selectedCell == null || selectedCell == currentCell) return false

    val sameRow = selectedCell.row == currentCell.row
    val sameColumn = selectedCell.col == currentCell.col
    val sameBlock = (selectedCell.row / 3 == currentCell.row / 3) &&
        (selectedCell.col / 3 == currentCell.col / 3)

    return sameRow || sameColumn || sameBlock
}

@Composable
private fun NumberPad(
    onValueSelected: (Int) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        (1..9).chunked(3).forEach { rowValues ->
            Row(horizontalArrangement = Arrangement.Center) {
                rowValues.forEach { value ->
                    Button(
                        onClick = { onValueSelected(value) },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(text = value.toString())
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = onClear,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(text = stringResource(id = R.string.game_clear_button))
            }
        }
    }
}

