package com.example.memento.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memento.model.LifePhase
import com.example.memento.ui.theme.AppColors
import com.example.memento.ui.theme.LocalAppColors
import com.example.memento.viewmodel.TagViewModel
import com.example.memento.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val CellGap = 1.dp
private val YearLabelWidth = 20.dp
private val YearLabelGap = 6.dp
private val HorizontalPadding = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeGridScreen(
    viewModel: UserViewModel = hiltViewModel(),
    tagViewModel: TagViewModel = hiltViewModel(),
) {
    val c = LocalAppColors.current
    val today = remember { LocalDate.now() }

    val currentWeekIdx = remember(today) {
        if (viewModel.user.birthday == null) return@remember -1
        val days = ChronoUnit.DAYS.between(viewModel.user.birthday, today)
        if (days < 0) -1 else (days / 7).toInt()
    }
    val weeksRemaining = viewModel.user.lifeExpectancyYears * 52 - (currentWeekIdx + 1)
    val currentYear = (currentWeekIdx / 52).coerceAtLeast(0)

    var scale by remember { mutableFloatStateOf(1f) }
    var selectedWeek by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var phasesEnabled by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()
    val horizontalScrollState = rememberScrollState()

    val phases by viewModel.phases.collectAsState()
    val birthday = viewModel.user.birthday
    val taggedWeeks by tagViewModel.weeksWithTags.collectAsState()

    val phaseColorMap: Map<Int, Int> = remember(phases, birthday) {
        val bd = birthday ?: return@remember emptyMap()
        buildMap {
            phases.forEach { phase ->
                val startIdx = (ChronoUnit.DAYS.between(bd, LocalDate.ofEpochDay(phase.startEpochDay)) / 7)
                    .toInt().coerceAtLeast(0)
                val endIdx = (ChronoUnit.DAYS.between(bd, LocalDate.ofEpochDay(phase.endEpochDay)) / 7)
                    .toInt()
                for (idx in startIdx..endIdx) put(idx, phase.colorArgb)
            }
        }
    }

    LaunchedEffect(Unit) {
        listState.scrollToItem((currentYear - 3).coerceAtLeast(0))
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
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

        val activePhaseMap: Map<Int, Int> = if (phasesEnabled) phaseColorMap else emptyMap()

        Column(modifier = Modifier.fillMaxSize()) {
            GridHeader(currentWeekIdx, weeksRemaining)

            PhasesToolbar(phasesEnabled = phasesEnabled, onToggle = { phasesEnabled = !phasesEnabled })

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
                            phaseColorMap = activePhaseMap,
                            taggedWeekIndices = taggedWeeks,
                            appColors = c,
                            onWeekSelected = { week -> selectedWeek = Pair(year, week) }
                        )
                        Spacer(Modifier.height(CellGap))
                    }
                }
            }
        }

        selectedWeek?.let { (year, week) ->
            ModalBottomSheet(
                onDismissRequest = { selectedWeek = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = c.surface,
                contentColor = c.text,
                scrimColor = Color(0x8C000000),
            ) {
                WeekDetailContent(
                    year = year,
                    week = week,
                    birthday = birthday ?: LocalDate.now(),
                    currentWeekIdx = currentWeekIdx,
                    tagViewModel = tagViewModel,
                )
            }
        }
    }
}

@Composable
private fun PhasesToolbar(phasesEnabled: Boolean, onToggle: () -> Unit) {
    val c = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToggleChip(label = "Life Phases", enabled = phasesEnabled, onClick = onToggle)
    }
}

@Composable
private fun ToggleChip(label: String, enabled: Boolean, onClick: () -> Unit) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .background(
                color = if (enabled) c.accent else Color.Transparent,
                shape = RoundedCornerShape(50)
            )
            .border(1.dp, c.accent, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (enabled) "◉  $label" else "○  $label",
            color = if (enabled) Color.White else c.accentSoft,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun GridHeader(currentWeekIdx: Int, weeksRemaining: Int) {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface)
            .border(width = 1.dp, color = c.border)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(text = "Life in Weeks", color = c.text, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(text = "Week $currentWeekIdx · $weeksRemaining weeks remaining", color = c.muted, fontSize = 12.sp)
    }
}

@Composable
private fun YearRow(
    year: Int,
    currentWeekIdx: Int,
    cellSize: Dp,
    selectedWeek: Pair<Int, Int>?,
    phaseColorMap: Map<Int, Int>,
    taggedWeekIndices: Set<Int>,
    appColors: AppColors,
    onWeekSelected: (week: Int) -> Unit,
) {
    val density = LocalDensity.current
    val cellSizePx = remember(cellSize, density) { with(density) { cellSize.toPx() } }
    val gapPx = remember(density) { with(density) { CellGap.toPx() } }
    val cornerRadiusPx = remember(density) { with(density) { 1.dp.toPx() } }
    val borderStrokePx = remember(density) { with(density) { 0.5.dp.toPx() } }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(YearLabelWidth)
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(
                        constraints.copy(minHeight = 0, maxHeight = Int.MAX_VALUE)
                    )
                    layout(placeable.width, 0) { placeable.place(0, -placeable.height / 2) }
                },
            contentAlignment = Alignment.CenterEnd
        ) {
            if (year % 5 == 0) {
                Text(text = "$year", color = appColors.muted, fontSize = 6.sp)
            }
        }

        Spacer(Modifier.width(YearLabelGap))

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
                val phaseColor = phaseColorMap[weekIdx]

                when {
                    isSelected -> drawRoundRect(color = appColors.accent, topLeft = topLeft, size = cellRect, cornerRadius = cr)
                    weekIdx == currentWeekIdx -> drawRoundRect(color = appColors.green, topLeft = topLeft, size = cellRect, cornerRadius = cr)
                    phaseColor != null && weekIdx < currentWeekIdx -> drawRoundRect(
                        color = Color(phaseColor).copy(alpha = 0.85f), topLeft = topLeft, size = cellRect, cornerRadius = cr
                    )
                    phaseColor != null -> {
                        drawRoundRect(color = Color(phaseColor).copy(alpha = 0.35f), topLeft = topLeft, size = cellRect, cornerRadius = cr)
                        drawRoundRect(color = Color(phaseColor).copy(alpha = 0.6f), topLeft = topLeft, size = cellRect, cornerRadius = cr, style = Stroke(width = borderStrokePx))
                    }
                    weekIdx < currentWeekIdx -> drawRoundRect(color = appColors.past, topLeft = topLeft, size = cellRect, cornerRadius = cr)
                    else -> {
                        drawRoundRect(color = appColors.future, topLeft = topLeft, size = cellRect, cornerRadius = cr)
                        drawRoundRect(color = appColors.futureBorder, topLeft = topLeft, size = cellRect, cornerRadius = cr, style = Stroke(width = borderStrokePx))
                    }
                }

                // Tag dot indicator
                if (weekIdx in taggedWeekIndices) {
                    val dotR = cellSizePx * 0.15f
                    drawCircle(
                        color = appColors.accentSoft,
                        radius = dotR,
                        center = Offset(x + cellSizePx - dotR - 1.dp.toPx(), dotR + 1.dp.toPx())
                    )
                }
            }
        }
    }
}

// ── Week detail bottom sheet ────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekDetailContent(
    year: Int,
    week: Int,
    birthday: LocalDate,
    currentWeekIdx: Int,
    tagViewModel: TagViewModel,
) {
    val c = LocalAppColors.current
    val weekIdx = year * 52 + week
    val weekStart = remember(birthday, weekIdx) { birthday.plusDays(weekIdx * 7L) }
    val weekEnd = remember(weekStart) { weekStart.plusDays(6) }

    val shortFmt = remember { DateTimeFormatter.ofPattern("MMM d") }
    val longFmt = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }
    val dateRange = remember(weekStart, weekEnd) {
        "${weekStart.format(shortFmt)} – ${weekEnd.format(longFmt)}"
    }

    var noteText by remember { mutableStateOf("") }
    var showTagPicker by remember { mutableStateOf(false) }

    val tags by tagViewModel.tagsForWeek(weekIdx).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        WeekBadge(weekIdx = weekIdx, currentWeekIdx = currentWeekIdx)

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Year $year · Week ${week + 1}",
            color = c.text,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.4).sp,
        )

        Spacer(Modifier.height(4.dp))

        Text(text = dateRange, color = c.muted, fontSize = 12.sp)
        Text(text = "Age $year", color = c.muted, fontSize = 12.sp)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "NOTE",
            color = c.muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            placeholder = { Text("What happened this week?", color = c.muted, fontSize = 14.sp) },
            shape = RoundedCornerShape(13.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.accent,
                unfocusedBorderColor = c.border,
                focusedTextColor = c.text,
                unfocusedTextColor = c.text,
                cursorColor = c.accentSoft,
                focusedContainerColor = c.surface2,
                unfocusedContainerColor = c.surface2,
            ),
            maxLines = 8,
        )

        Spacer(Modifier.height(24.dp))

        // Tags section
        Text(
            text = "TAGS",
            color = ColorMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        )

        Spacer(Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tags.forEach { tag ->
                TagChip(tag = tag, onRemove = { tagViewModel.removeTag(weekIdx, tag) })
            }
            Box(
                modifier = Modifier
                    .background(Color.Transparent, RoundedCornerShape(50))
                    .border(1.dp, ColorBorder, RoundedCornerShape(50))
                    .clickable { showTagPicker = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("+ Add tag", color = ColorAccentSoft, fontSize = 13.sp)
            }
        }
    }

    if (showTagPicker) {
        TagPickerDialog(
            weekIdx = weekIdx,
            activeTags = tags,
            tagViewModel = tagViewModel,
            onDismiss = { showTagPicker = false },
        )
    }
}

@Composable
private fun TagChip(tag: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .background(ColorAccent.copy(alpha = 0.18f), RoundedCornerShape(50))
            .border(1.dp, ColorAccent.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(tag, color = ColorAccentSoft, fontSize = 13.sp)
        Box(
            modifier = Modifier
                .clickable(onClick = onRemove)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("×", color = ColorAccentSoft, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagPickerDialog(
    weekIdx: Int,
    activeTags: List<String>,
    tagViewModel: TagViewModel,
    onDismiss: () -> Unit,
) {
    val allUsedTagNames by tagViewModel.allUsedTagNames.collectAsState()
    val allTags = remember(allUsedTagNames) {
        (tagViewModel.PREDEFINED_TAGS + allUsedTagNames).distinct()
    }
    var customTagInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurface,
        titleContentColor = ColorText,
        title = { Text("Add Tags", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    allTags.forEach { tag ->
                        val isActive = tag in activeTags
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isActive) ColorAccent else ColorSurface2,
                                    RoundedCornerShape(50)
                                )
                                .border(
                                    1.dp,
                                    if (isActive) ColorAccent else ColorBorder,
                                    RoundedCornerShape(50)
                                )
                                .clickable {
                                    if (isActive) tagViewModel.removeTag(weekIdx, tag)
                                    else tagViewModel.addTag(weekIdx, tag)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tag,
                                color = if (isActive) Color.White else ColorAccentSoft,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = customTagInput,
                        onValueChange = { customTagInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Custom tag…", color = ColorMuted, fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorAccent,
                            unfocusedBorderColor = ColorBorder,
                            focusedTextColor = ColorText,
                            unfocusedTextColor = ColorText,
                            cursorColor = ColorAccentSoft,
                            focusedContainerColor = ColorSurface2,
                            unfocusedContainerColor = ColorSurface2,
                        ),
                    )
                    Box(
                        modifier = Modifier
                            .background(ColorAccent, RoundedCornerShape(10.dp))
                            .clickable {
                                val trimmed = customTagInput.trim()
                                if (trimmed.isNotEmpty()) {
                                    tagViewModel.addTag(weekIdx, trimmed)
                                    customTagInput = ""
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Add", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = ColorAccentSoft, fontWeight = FontWeight.SemiBold)
            }
        },
    )
}

@Composable
private fun WeekBadge(weekIdx: Int, currentWeekIdx: Int) {
    val c = LocalAppColors.current
    val (label, textColor, bgColor) = when {
        weekIdx == currentWeekIdx -> Triple("NOW",    c.green,      c.green.copy(alpha = 0.15f))
        weekIdx < currentWeekIdx  -> Triple("PAST",   c.muted,      c.muted.copy(alpha = 0.20f))
        else                      -> Triple("FUTURE", c.accentSoft, c.accent.copy(alpha = 0.15f))
    }
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = label, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}
