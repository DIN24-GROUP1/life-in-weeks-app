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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.memento.model.CountryData
import com.example.memento.model.LifePhase
import com.example.memento.model.PhaseColorPresets
import com.example.memento.model.allCountries
import com.example.memento.ui.theme.AppColors
import com.example.memento.ui.theme.LocalAppColors
import com.example.memento.ui.theme.ThemeMode
import com.example.memento.viewmodel.AuthViewModel
import com.example.memento.viewmodel.UserViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(viewModel: UserViewModel, authViewModel: AuthViewModel) {
    val c = LocalAppColors.current
    val phases by viewModel.phases.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }
    var editingPhase by remember { mutableStateOf<LifePhase?>(null) }

    var newName by remember { mutableStateOf("") }
    var newColorIdx by remember { mutableIntStateOf(0) }
    var newStartEpochDay by remember { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var newEndEpochDay by remember { mutableLongStateOf(LocalDate.now().plusDays(30).toEpochDay()) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val startPickerState = rememberDatePickerState(initialSelectedDateMillis = newStartEpochDay * 86_400_000L)
    val endPickerState = rememberDatePickerState(initialSelectedDateMillis = newEndEpochDay * 86_400_000L)

    val dateFmt = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }
    val context = LocalContext.current

    LazyColumn(modifier = Modifier.fillMaxSize().background(c.bg)) {

        // Account section
        item { AccountSection(authViewModel = authViewModel, onGoogleSignIn = { authViewModel.signInWithGoogle(context) }) }
        item { HorizontalDivider(color = c.border, thickness = 1.dp) }

        // Profile section
        item { ProfileSection(viewModel = viewModel) }
        item { HorizontalDivider(color = c.border, thickness = 1.dp) }

        // Appearance section
        item { AppearanceSection(viewModel = viewModel) }
        item { HorizontalDivider(color = c.border, thickness = 1.dp) }

        // Life Phases header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.surface)
                    .border(width = 1.dp, color = c.border)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Life Phases",
                    color = c.text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showAddForm = !showAddForm }) {
                    Icon(Icons.Default.Add, contentDescription = "Add phase", tint = c.accentSoft)
                }
            }
        }

        // Phase rows
        items(phases, key = { it.id }) { phase ->
            PhaseRow(
                phase = phase,
                dateFmt = dateFmt,
                onEdit = { editingPhase = phase },
                onDelete = { viewModel.deletePhase(phase) }
            )
            HorizontalDivider(color = c.border, thickness = 0.5.dp)
        }

        // Empty state
        if (phases.isEmpty() && !showAddForm) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No phases yet. Tap + to add one.", color = c.muted, fontSize = 14.sp)
                }
            }
        }

        // Add form
        if (showAddForm) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.surface2)
                        .padding(16.dp)
                ) {
                    Text("NEW PHASE", color = c.muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(12.dp))

                    PhaseFormContent(
                        name = newName,
                        onNameChange = { newName = it },
                        colorIdx = newColorIdx,
                        onColorChange = { newColorIdx = it },
                        startEpochDay = newStartEpochDay,
                        endEpochDay = newEndEpochDay,
                        showStartPicker = showStartPicker,
                        showEndPicker = showEndPicker,
                        onShowStartPicker = { showStartPicker = it },
                        onShowEndPicker = { showEndPicker = it },
                        startPickerState = startPickerState,
                        endPickerState = endPickerState,
                        onStartConfirm = { ms ->
                            newStartEpochDay = Instant.ofEpochMilli(ms)
                                .atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
                        },
                        onEndConfirm = { ms ->
                            newEndEpochDay = Instant.ofEpochMilli(ms)
                                .atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
                        },
                        dateFmt = dateFmt,
                    )

                    Spacer(Modifier.height(16.dp))

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
                                newName = ""
                                newColorIdx = 0
                                newStartEpochDay = LocalDate.now().toEpochDay()
                                newEndEpochDay = LocalDate.now().plusDays(30).toEpochDay()
                                showAddForm = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = c.accent),
                    ) {
                        Text("Save Phase", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }

                    TextButton(onClick = { showAddForm = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel", color = c.muted)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }

    editingPhase?.let { phase ->
        EditPhaseDialog(
            phase = phase,
            dateFmt = dateFmt,
            onSave = { updated ->
                viewModel.updatePhase(updated)
                editingPhase = null
            },
            onDismiss = { editingPhase = null }
        )
    }
}

// ── Appearance section ────────────────────────────────────────────────────────

@Composable
private fun AppearanceSection(viewModel: UserViewModel) {
    val c = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "APPEARANCE",
            color = c.muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(12.dp))

        Text(text = "Theme", color = c.text, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                val selected = viewModel.themeMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (selected) c.accent else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, if (selected) c.accent else c.border, RoundedCornerShape(8.dp))
                        .clickable { viewModel.updateThemeMode(mode) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = mode.name,
                        color = if (selected) Color.White else c.muted,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

// ── Edit phase dialog ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPhaseDialog(
    phase: LifePhase,
    dateFmt: DateTimeFormatter,
    onSave: (LifePhase) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = LocalAppColors.current
    var name by remember(phase.id) { mutableStateOf(phase.name) }
    var colorIdx by remember(phase.id) {
        mutableIntStateOf(PhaseColorPresets.indexOf(phase.colorArgb).takeIf { it >= 0 } ?: 0)
    }
    var startEpochDay by remember(phase.id) { mutableLongStateOf(phase.startEpochDay) }
    var endEpochDay by remember(phase.id) { mutableLongStateOf(phase.endEpochDay) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val startPickerState = rememberDatePickerState(initialSelectedDateMillis = startEpochDay * 86_400_000L)
    val endPickerState = rememberDatePickerState(initialSelectedDateMillis = endEpochDay * 86_400_000L)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        titleContentColor = c.text,
        title = { Text("Edit Phase", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.text) },
        text = {
            PhaseFormContent(
                name = name,
                onNameChange = { name = it },
                colorIdx = colorIdx,
                onColorChange = { colorIdx = it },
                startEpochDay = startEpochDay,
                endEpochDay = endEpochDay,
                showStartPicker = showStartPicker,
                showEndPicker = showEndPicker,
                onShowStartPicker = { showStartPicker = it },
                onShowEndPicker = { showEndPicker = it },
                startPickerState = startPickerState,
                endPickerState = endPickerState,
                onStartConfirm = { ms ->
                    startEpochDay = Instant.ofEpochMilli(ms)
                        .atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
                },
                onEndConfirm = { ms ->
                    endEpochDay = Instant.ofEpochMilli(ms)
                        .atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
                },
                dateFmt = dateFmt,
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(phase.copy(
                        name = name.trim(),
                        colorArgb = PhaseColorPresets[colorIdx],
                        startEpochDay = startEpochDay,
                        endEpochDay = endEpochDay,
                    ))
                }
            }) {
                Text("Save", color = c.accentSoft, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = c.muted) }
        },
    )
}

// ── Shared phase form ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhaseFormContent(
    name: String,
    onNameChange: (String) -> Unit,
    colorIdx: Int,
    onColorChange: (Int) -> Unit,
    startEpochDay: Long,
    endEpochDay: Long,
    showStartPicker: Boolean,
    showEndPicker: Boolean,
    onShowStartPicker: (Boolean) -> Unit,
    onShowEndPicker: (Boolean) -> Unit,
    startPickerState: androidx.compose.material3.DatePickerState,
    endPickerState: androidx.compose.material3.DatePickerState,
    onStartConfirm: (Long) -> Unit,
    onEndConfirm: (Long) -> Unit,
    dateFmt: DateTimeFormatter,
) {
    val c = LocalAppColors.current

    Column {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name", color = c.muted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = fieldColors(c),
        )

        Spacer(Modifier.height(12.dp))

        Text(text = "Color", color = c.muted, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PhaseColorPresets.forEachIndexed { idx, argb ->
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(argb))
                        .then(if (idx == colorIdx) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                        .clickable { onColorChange(idx) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(text = "Start date", color = c.muted, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        DateFieldWithPicker(
            value = LocalDate.ofEpochDay(startEpochDay).format(dateFmt),
            showPicker = showStartPicker,
            onShowPicker = onShowStartPicker,
            pickerState = startPickerState,
            onConfirm = onStartConfirm,
        )

        Spacer(Modifier.height(12.dp))

        Text(text = "End date", color = c.muted, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        DateFieldWithPicker(
            value = LocalDate.ofEpochDay(endEpochDay).format(dateFmt),
            showPicker = showEndPicker,
            onShowPicker = onShowEndPicker,
            pickerState = endPickerState,
            onConfirm = onEndConfirm,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFieldWithPicker(
    value: String,
    showPicker: Boolean,
    onShowPicker: (Boolean) -> Unit,
    pickerState: androidx.compose.material3.DatePickerState,
    onConfirm: (Long) -> Unit,
) {
    val c = LocalAppColors.current
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            trailingIcon = {
                IconButton(onClick = { onShowPicker(true) }) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = c.muted)
                }
            },
            colors = fieldColors(c),
        )
        if (showPicker) {
            Popup(onDismissRequest = { onShowPicker(false) }, alignment = Alignment.TopStart) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 4.dp)
                        .shadow(8.dp)
                        .background(c.surface, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        DatePicker(state = pickerState, showModeToggle = false)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { onShowPicker(false) }) { Text("Cancel", color = c.muted) }
                            TextButton(onClick = {
                                pickerState.selectedDateMillis?.let(onConfirm)
                                onShowPicker(false)
                            }) { Text("OK", color = c.accentSoft) }
                        }
                    }
                }
            }
        }
    }
}

// ── Profile section ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileSection(viewModel: UserViewModel) {
    val c = LocalAppColors.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var countrySearch by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("PROFILE", color = c.muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        Spacer(Modifier.height(12.dp))

        Box {
            OutlinedTextField(
                value = viewModel.birthdayText.ifBlank { "Not set" },
                onValueChange = {},
                readOnly = true,
                label = { Text("Date of Birth", color = c.muted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pick date", tint = c.muted)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = c.accent,
                    unfocusedBorderColor = c.border,
                    focusedTextColor = c.text,
                    unfocusedTextColor = if (viewModel.birthdayText.isBlank()) c.muted else c.text,
                    focusedContainerColor = c.surface,
                    unfocusedContainerColor = c.surface,
                ),
            )
            if (showDatePicker) {
                Popup(onDismissRequest = { showDatePicker = false }, alignment = Alignment.TopStart) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = 4.dp)
                            .shadow(8.dp)
                            .background(c.surface, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            DatePicker(state = datePickerState, showModeToggle = false)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = c.muted) }
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { viewModel.convertMillisToDate(it) }
                                    showDatePicker = false
                                }) { Text("OK", color = c.accentSoft) }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(text = "Gender", color = c.muted, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Male", fontSize = 12.sp, color = c.muted)
            Spacer(Modifier.width(8.dp))
            Slider(
                value = viewModel.genderSliderPosition,
                onValueChange = { viewModel.updateGenderSlider(it) },
                valueRange = 0f..9f,
                steps = 8,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = c.accentSoft,
                    activeTrackColor = c.accent,
                    inactiveTrackColor = c.muted.copy(alpha = 0.3f),
                )
            )
            Spacer(Modifier.width(8.dp))
            Text("Female", fontSize = 12.sp, color = c.muted)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.selectedCountry?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Country of Origin", color = c.muted) },
            placeholder = { Text("Select a country", color = c.muted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().clickable { showCountryPicker = true },
            enabled = false,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = c.text,
                disabledBorderColor = c.border,
                disabledLabelColor = c.muted,
                disabledPlaceholderColor = c.muted,
                disabledContainerColor = c.surface,
            ),
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.lifeExpectancyText,
            onValueChange = viewModel::updateLifeExpectancy,
            label = { Text("Life expectancy (years)", color = c.muted) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("90", color = c.muted) },
            supportingText = if (viewModel.selectedCountry != null) {
                { Text("Auto-calculated from country & gender", color = c.accentSoft, fontSize = 11.sp) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = fieldColors(c),
        )
    }

    if (showCountryPicker) {
        SettingsCountryPickerDialog(
            search = countrySearch,
            onSearchChange = { countrySearch = it },
            onSelect = { country ->
                viewModel.updateCountry(country)
                showCountryPicker = false
                countrySearch = ""
            },
            onDismiss = {
                showCountryPicker = false
                countrySearch = ""
            }
        )
    }
}

@Composable
private fun SettingsCountryPickerDialog(
    search: String,
    onSearchChange: (String) -> Unit,
    onSelect: (CountryData) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = LocalAppColors.current
    val filtered = remember(search) {
        if (search.isBlank()) allCountries
        else allCountries.filter { it.name.contains(search, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Country") },
        text = {
            Column {
                OutlinedTextField(
                    value = search,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(320.dp)) {
                    items(filtered) { country ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(country) }
                                .padding(horizontal = 4.dp, vertical = 10.dp)
                        ) {
                            Text(country.name, fontSize = 14.sp, color = c.text)
                            Text("♂ ${country.maleLE}y  ♀ ${country.femaleLE}y", fontSize = 11.sp, color = c.muted)
                        }
                        HorizontalDivider(color = c.border)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = c.surface,
        titleContentColor = c.text,
    )
}

// ── Account section ───────────────────────────────────────────────────────────

@Composable
private fun AccountSection(authViewModel: AuthViewModel, onGoogleSignIn: () -> Unit) {
    val c = LocalAppColors.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isSignInMode by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("ACCOUNT", color = c.muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        Spacer(Modifier.height(12.dp))

        if (authViewModel.isSignedIn) {
            authViewModel.displayName?.let { Text(it, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.Medium) }
            authViewModel.email?.let { Text(it, color = c.muted, fontSize = 13.sp) }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { authViewModel.signOut() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.surface2),
            ) {
                Text("Sign out", color = c.text, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Text("Sign in to sync your data across devices.", color = c.muted, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))

            TabRow(selectedTabIndex = selectedTab, containerColor = c.surface2, contentColor = c.accentSoft) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Google", modifier = Modifier.padding(vertical = 10.dp), color = if (selectedTab == 0) c.accentSoft else c.muted)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Email", modifier = Modifier.padding(vertical = 10.dp), color = if (selectedTab == 1) c.accentSoft else c.muted)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (selectedTab == 0) {
                Button(
                    onClick = onGoogleSignIn,
                    enabled = !authViewModel.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.accent),
                ) {
                    Text(
                        text = if (authViewModel.isLoading) "Signing in…" else "Sign in with Google",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = { isSignInMode = true }) { Text("Sign in", color = if (isSignInMode) c.accentSoft else c.muted) }
                    TextButton(onClick = { isSignInMode = false }) { Text("Register", color = if (!isSignInMode) c.accentSoft else c.muted) }
                }

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email", color = c.muted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = fieldColors(c),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Password", color = c.muted) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = fieldColors(c),
                )
                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (isSignInMode) authViewModel.signInWithEmail(emailInput, passwordInput)
                        else authViewModel.registerWithEmail(emailInput, passwordInput)
                    },
                    enabled = !authViewModel.isLoading && emailInput.isNotBlank() && passwordInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.accent),
                ) {
                    Text(
                        text = when {
                            authViewModel.isLoading -> "Please wait…"
                            isSignInMode -> "Sign in"
                            else -> "Register"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        authViewModel.errorMessage?.let { msg ->
            Spacer(Modifier.height(8.dp))
            Text(msg, color = Color(0xFFEF4444), fontSize = 12.sp)
        }
    }
}

// ── Phase row ─────────────────────────────────────────────────────────────────

@Composable
private fun PhaseRow(
    phase: LifePhase,
    dateFmt: DateTimeFormatter,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = LocalAppColors.current
    val start = LocalDate.ofEpochDay(phase.startEpochDay).format(dateFmt)
    val end = LocalDate.ofEpochDay(phase.endEpochDay).format(dateFmt)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(phase.colorArgb)))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = phase.name, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = "$start – $end", color = c.muted, fontSize = 12.sp)
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Edit phase", tint = c.accentSoft)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete phase", tint = c.muted)
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun fieldColors(c: AppColors) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = c.accent,
    unfocusedBorderColor = c.border,
    focusedTextColor = c.text,
    unfocusedTextColor = c.text,
    cursorColor = c.accentSoft,
    focusedContainerColor = c.surface,
    unfocusedContainerColor = c.surface,
)
