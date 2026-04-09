package com.example.memento.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.memento.model.LifePhase
import com.example.memento.model.PhaseColorPresets
import com.example.memento.viewmodel.UserViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Reuse colors from the app palette
private val SBg = Color(0xFF0D0D1A)
private val SSurface = Color(0xFF16162A)
private val SSurface2 = Color(0xFF1E1E35)
private val SText = Color(0xFFE8E8F5)
private val SMuted = Color(0xFF5A5A80)
private val SBorder = Color(0xFF2A2A48)
private val SAccent = Color(0xFF7C3AED)
private val SAccentSoft = Color(0xFFA78BFA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(viewModel: UserViewModel) {
    val phases by viewModel.phases.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }

    // Add-form state
    var newName by remember { mutableStateOf("") }
    var newColorIdx by remember { mutableIntStateOf(0) }
    var newStartEpochDay by remember { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var newEndEpochDay by remember { mutableLongStateOf(LocalDate.now().plusDays(30).toEpochDay()) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val startPickerState = rememberDatePickerState(
        initialSelectedDateMillis = newStartEpochDay * 86_400_000L
    )
    val endPickerState = rememberDatePickerState(
        initialSelectedDateMillis = newEndEpochDay * 86_400_000L
    )

    val dateFmt = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SBg)
    ) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SSurface)
                .border(width = 1.dp, color = SBorder)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Life Phases",
                color = SText,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showAddForm = !showAddForm }) {
                Icon(Icons.Default.Add, contentDescription = "Add phase", tint = SAccentSoft)
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(phases, key = { it.id }) { phase ->
                PhaseRow(
                    phase = phase,
                    dateFmt = dateFmt,
                    onDelete = { viewModel.deletePhase(phase) }
                )
                HorizontalDivider(color = SBorder, thickness = 0.5.dp)
            }

            if (phases.isEmpty() && !showAddForm) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No phases yet. Tap + to add one.",
                            color = SMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (showAddForm) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SSurface2)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "NEW PHASE",
                            color = SMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                        )

                        Spacer(Modifier.height(12.dp))

                        // Name
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Name", color = SMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SAccent,
                                unfocusedBorderColor = SBorder,
                                focusedTextColor = SText,
                                unfocusedTextColor = SText,
                                cursorColor = SAccentSoft,
                                focusedContainerColor = SSurface,
                                unfocusedContainerColor = SSurface,
                            ),
                        )

                        Spacer(Modifier.height(12.dp))

                        // Color picker
                        Text(text = "Color", color = SMuted, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PhaseColorPresets.forEachIndexed { idx, argb ->
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(argb))
                                        .then(
                                            if (idx == newColorIdx)
                                                Modifier.border(2.dp, Color.White, CircleShape)
                                            else Modifier
                                        )
                                        .clickable { newColorIdx = idx }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Start date
                        Text(text = "Start date", color = SMuted, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Box {
                            OutlinedTextField(
                                value = LocalDate.ofEpochDay(newStartEpochDay).format(dateFmt),
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                trailingIcon = {
                                    IconButton(onClick = { showStartPicker = true }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Pick start date", tint = SMuted)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SAccent,
                                    unfocusedBorderColor = SBorder,
                                    focusedTextColor = SText,
                                    unfocusedTextColor = SText,
                                    focusedContainerColor = SSurface,
                                    unfocusedContainerColor = SSurface,
                                ),
                            )
                            if (showStartPicker) {
                                Popup(
                                    onDismissRequest = { showStartPicker = false },
                                    alignment = Alignment.TopStart,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset(y = 4.dp)
                                            .shadow(8.dp)
                                            .background(SSurface, RoundedCornerShape(12.dp))
                                            .padding(8.dp)
                                    ) {
                                        Column {
                                            DatePicker(
                                                state = startPickerState,
                                                showModeToggle = false,
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(onClick = { showStartPicker = false }) {
                                                    Text("Cancel", color = SMuted)
                                                }
                                                TextButton(onClick = {
                                                    startPickerState.selectedDateMillis?.let { ms ->
                                                        newStartEpochDay = Instant.ofEpochMilli(ms)
                                                            .atZone(ZoneId.systemDefault())
                                                            .toLocalDate()
                                                            .toEpochDay()
                                                    }
                                                    showStartPicker = false
                                                }) {
                                                    Text("OK", color = SAccentSoft)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // End date
                        Text(text = "End date", color = SMuted, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Box {
                            OutlinedTextField(
                                value = LocalDate.ofEpochDay(newEndEpochDay).format(dateFmt),
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                trailingIcon = {
                                    IconButton(onClick = { showEndPicker = true }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Pick end date", tint = SMuted)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SAccent,
                                    unfocusedBorderColor = SBorder,
                                    focusedTextColor = SText,
                                    unfocusedTextColor = SText,
                                    focusedContainerColor = SSurface,
                                    unfocusedContainerColor = SSurface,
                                ),
                            )
                            if (showEndPicker) {
                                Popup(
                                    onDismissRequest = { showEndPicker = false },
                                    alignment = Alignment.TopStart,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset(y = 4.dp)
                                            .shadow(8.dp)
                                            .background(SSurface, RoundedCornerShape(12.dp))
                                            .padding(8.dp)
                                    ) {
                                        Column {
                                            DatePicker(
                                                state = endPickerState,
                                                showModeToggle = false,
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(onClick = { showEndPicker = false }) {
                                                    Text("Cancel", color = SMuted)
                                                }
                                                TextButton(onClick = {
                                                    endPickerState.selectedDateMillis?.let { ms ->
                                                        newEndEpochDay = Instant.ofEpochMilli(ms)
                                                            .atZone(ZoneId.systemDefault())
                                                            .toLocalDate()
                                                            .toEpochDay()
                                                    }
                                                    showEndPicker = false
                                                }) {
                                                    Text("OK", color = SAccentSoft)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Save button
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    viewModel.addPhase(
                                        LifePhase(
                                            name = newName.trim(),
                                            colorArgb = PhaseColorPresets[newColorIdx],
                                            startEpochDay = newStartEpochDay,
                                            endEpochDay = newEndEpochDay,
                                        )
                                    )
                                    // Reset form
                                    newName = ""
                                    newColorIdx = 0
                                    newStartEpochDay = LocalDate.now().toEpochDay()
                                    newEndEpochDay = LocalDate.now().plusDays(30).toEpochDay()
                                    showAddForm = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SAccent),
                        ) {
                            Text("Save Phase", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }

                        TextButton(
                            onClick = { showAddForm = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel", color = SMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhaseRow(
    phase: LifePhase,
    dateFmt: DateTimeFormatter,
    onDelete: () -> Unit,
) {
    val start = LocalDate.ofEpochDay(phase.startEpochDay).format(dateFmt)
    val end = LocalDate.ofEpochDay(phase.endEpochDay).format(dateFmt)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SSurface)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Color dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(phase.colorArgb))
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = phase.name, color = SText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = "$start – $end", color = SMuted, fontSize = 12.sp)
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete phase", tint = SMuted)
        }
    }
}
