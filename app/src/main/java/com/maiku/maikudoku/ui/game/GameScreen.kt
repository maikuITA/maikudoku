package com.maiku.maikudoku.ui.game

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.maiku.maikudoku.R
import com.maiku.maikudoku.domain.model.CellState
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GameScreen(
    modifier: Modifier = Modifier,
    onNavigateHome: () -> Unit = {},
    viewModel: SudokuViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED")
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.persistCurrentGame()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_delete),
                            contentDescription = stringResource(id = R.string.game_home_button),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.game_difficulty_label),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = uiState.difficulty.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(
                    id = R.string.game_timer_label,
                    formatPlayTime(uiState.playTimeSeconds)
                ),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.gridState.isNotEmpty() -> {
                    SudokuGrid(
                        gridState = uiState.gridState,
                        selectedCell = uiState.selectedCell,
                        onCellClick = viewModel::selectCell,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    NumberPad(
                        onValueSelected = viewModel::setCellValue
                    )

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
            onDismissRequest = { },
            title = { Text(text = stringResource(id = R.string.game_exit_dialog_title), fontWeight = FontWeight.Bold) },
            text = { Text(text = stringResource(id = R.string.game_exit_dialog_message), fontWeight = FontWeight.Bold) },
            containerColor = MaterialTheme.colorScheme.background,
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED")
                        showExitDialog = false
                    }) {
                        Text(
                            text = stringResource(id = R.string.game_exit_dialog_cancel),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = {
                        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED")
                        run { showExitDialog = false }
                        viewModel.persistCurrentGame()
                        onNavigateHome()
                    }) {
                        Text(
                            text = stringResource(id = R.string.game_exit_dialog_confirm),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun SudokuGrid(
    gridState: List<List<CellState>>,
    selectedCell: CellPosition?,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridLineColor = Color.Black
    val selectedValue = selectedCell?.let { gridState[it.row][it.col].value } ?: 0

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .drawWithContent {
                drawContent()

                val cellSize = size.width / 9f
                val thin = 1.dp.toPx()
                val thick = 2.dp.toPx()

                for (i in 0..9) {
                    val stroke = if (i % 3 == 0) thick else thin
                    val position = i * cellSize

                    drawLine(
                        color = gridLineColor,
                        start = Offset(position, 0f),
                        end = Offset(position, size.height),
                        strokeWidth = stroke
                    )
                    drawLine(
                        color = gridLineColor,
                        start = Offset(0f, position),
                        end = Offset(size.width, position),
                        strokeWidth = stroke
                    )
                }
            }
    ) {
        gridState.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                row.forEachIndexed { colIndex, cell ->
                    val position = CellPosition(row = rowIndex, col = colIndex)
                    SudokuCell(
                        cell = cell,
                        isSelected = selectedCell == position,
                        isInSelectedBlock = isInSelectedBlock(selectedCell, position),
                        isInSelectedRowOrColumn = isInSelectedRowOrColumn(selectedCell, position),
                        hasSameValueAsSelected = selectedValue != 0 && cell.value == selectedValue,
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
    cell: CellState,
    isSelected: Boolean,
    isInSelectedBlock: Boolean,
    isInSelectedRowOrColumn: Boolean,
    hasSameValueAsSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.surfaceVariant
        hasSameValueAsSelected -> MaterialTheme.colorScheme.tertiary
        isInSelectedBlock -> MaterialTheme.colorScheme.tertiary
        isInSelectedRowOrColumn -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.background
    }

    val textColor = if (cell.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(color = backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (cell.value == 0) "" else cell.value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Normal,
            color = textColor
        )
    }
}

private fun isInSelectedBlock(
    selectedCell: CellPosition?,
    currentCell: CellPosition
): Boolean {
    if (selectedCell == null || selectedCell == currentCell) return false

    return (selectedCell.row / 3 == currentCell.row / 3) &&
        (selectedCell.col / 3 == currentCell.col / 3)
}

private fun isInSelectedRowOrColumn(
    selectedCell: CellPosition?,
    currentCell: CellPosition
): Boolean {
    if (selectedCell == null || selectedCell == currentCell) return false

    return selectedCell.row == currentCell.row || selectedCell.col == currentCell.col
}

private fun formatPlayTime(totalSeconds: Long): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0L)
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

@Composable
private fun NumberPad(
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            (1..9).forEach { value ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(MaterialTheme.colorScheme.background)
                        .clickable { onValueSelected(value) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
