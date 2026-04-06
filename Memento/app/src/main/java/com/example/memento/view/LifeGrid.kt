package com.example.memento.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.memento.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val ColorPast = Color(0xFF3D3D60)
private val ColorNow = Color(0xFF22C55E)
private val ColorSelected = Color(0xFF7C3AED)
private val ColorFuture = Color(0xFF181828)
private val ColorFutureBorder = Color(0xFF222238)
private val ColorMuted = Color(0xFF5A5A80)
private val ColorSurface = Color(0xFF16162A)
private val ColorBg = Color(0xFF0D0D1A)
private val ColorText = Color(0xFFE8E8F5)
private val ColorBorder = Color(0xFF2A2A48)

private val CellGap = 1.dp
private val YearLabelWidth = 20.dp
private val YearLabelGap = 6.dp
private val HorizontalPadding = 16.dp

@Composable
fun LifeGridScreen(
    viewModel: UserViewModel = hiltViewModel()
) {

    val today = remember { LocalDate.now() }

    // Weeks are indexed flat from birth: weekIdx = year * 52 + weekOfYear
    val currentWeekIdx = remember(today) {
        if (viewModel.user.birthday == null) {
            return@remember -1
        }
        val days = ChronoUnit.DAYS.between(viewModel.user.birthday, today)
        if (days < 0) -1 else (days / 7).toInt()
    }
    val weeksRemaining = viewModel.user.lifeExpectancyYears * 52 - (currentWeekIdx + 1)
    val currentYear = (currentWeekIdx / 52).coerceAtLeast(0)

    var scale by remember { mutableFloatStateOf(1f) }
    var selectedWeek by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val listState = rememberLazyListState()
    val horizontalScrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        listState.scrollToItem((currentYear - 3).coerceAtLeast(0))
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBg)
            .pointerInput(Unit) {
                awaitEachGesture {
                    var event = awaitPointerEvent(PointerEventPass.Initial)
                    while (event.changes.any { it.pressed }) {
                        val zoomChange = event.calculateZoom()
                        if (zoomChange != 1f) {
                            scale = (scale * zoomChange).coerceIn(1f, 5f)
                            event.changes.forEach { it.consume() }
                        }
                        event = awaitPointerEvent(PointerEventPass.Initial)
                    }
                }
            }
    ) {
        val baseCellSize = ((maxWidth - HorizontalPadding * 2 - YearLabelWidth - YearLabelGap - CellGap * 51) / 52)
            .coerceAtLeast(4.dp)
        val cellSize = baseCellSize * scale
        val contentWidth = HorizontalPadding * 2 + YearLabelWidth + YearLabelGap + cellSize * 52 + CellGap * 51

        Column(modifier = Modifier.fillMaxSize()) {
            GridHeader(currentWeekIdx, weeksRemaining)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .width(contentWidth)
                        .padding(horizontal = HorizontalPadding)
                ) {
                    items(viewModel.user.lifeExpectancyYears, key = { it }) { year ->
                        YearRow(
                            year = year,
                            currentWeekIdx = currentWeekIdx,
                            cellSize = cellSize,
                            selectedWeek = selectedWeek,
                            onWeekSelected = { week -> selectedWeek = Pair(year, week) }
                        )
                        Spacer(Modifier.height(CellGap))
                    }
                }
            }
        }
    }
}

@Composable
private fun GridHeader(currentWeekIdx: Int, weeksRemaining: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorSurface)
            .border(width = 1.dp, color = ColorBorder)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = "Life in Weeks",
            color = ColorText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Week $currentWeekIdx · $weeksRemaining weeks remaining",
            color = ColorMuted,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun YearRow(
    year: Int,
    currentWeekIdx: Int,
    cellSize: Dp,
    selectedWeek: Pair<Int, Int>?,
    onWeekSelected: (week: Int) -> Unit
) {
    val density = LocalDensity.current
    val cellSizePx = remember(cellSize, density) { with(density) { cellSize.toPx() } }
    val gapPx = remember(density) { with(density) { CellGap.toPx() } }
    val cornerRadiusPx = remember(density) { with(density) { 1.dp.toPx() } }
    val borderStrokePx = remember(density) { with(density) { 0.5.dp.toPx() } }

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Year label — zero layout height so it never inflates the row
        Box(
            modifier = Modifier
                .width(YearLabelWidth)
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(
                        constraints.copy(minHeight = 0, maxHeight = Int.MAX_VALUE)
                    )
                    layout(placeable.width, 0) {
                        placeable.place(0, -placeable.height / 2)
                    }
                },
            contentAlignment = Alignment.CenterEnd
        ) {
            if (year % 5 == 0) {
                Text(text = "$year", color = ColorMuted, fontSize = 6.sp)
            }
        }

        Spacer(Modifier.width(YearLabelGap))

        // All 52 cells drawn in a single Canvas pass — no per-cell composables
        Canvas(
            modifier = Modifier
                .width(cellSize * 52 + CellGap * 51)
                .height(cellSize)
                .pointerInput(year, cellSizePx, gapPx) {
                    detectTapGestures { offset ->
                        val week = (offset.x / (cellSizePx + gapPx)).toInt().coerceIn(0, 51)
                        onWeekSelected(week)
                    }
                }
        ) {
            val cr = CornerRadius(cornerRadiusPx)
            for (week in 0 until 52) {
                val weekIdx = year * 52 + week
                val isSelected = selectedWeek?.first == year && selectedWeek.second == week
                val x = week * (cellSizePx + gapPx)
                val topLeft = Offset(x, 0f)
                val cellRect = Size(cellSizePx, cellSizePx)

                when {
                    isSelected -> drawRoundRect(
                        color = ColorSelected,
                        topLeft = topLeft,
                        size = cellRect,
                        cornerRadius = cr
                    )
                    weekIdx == currentWeekIdx -> drawRoundRect(
                        color = ColorNow,
                        topLeft = topLeft,
                        size = cellRect,
                        cornerRadius = cr
                    )
                    weekIdx < currentWeekIdx -> drawRoundRect(
                        color = ColorPast,
                        topLeft = topLeft,
                        size = cellRect,
                        cornerRadius = cr
                    )
                    else -> {
                        drawRoundRect(
                            color = ColorFuture,
                            topLeft = topLeft,
                            size = cellRect,
                            cornerRadius = cr
                        )
                        drawRoundRect(
                            color = ColorFutureBorder,
                            topLeft = topLeft,
                            size = cellRect,
                            cornerRadius = cr,
                            style = Stroke(width = borderStrokePx)
                        )
                    }
                }
            }
        }
    }
}
