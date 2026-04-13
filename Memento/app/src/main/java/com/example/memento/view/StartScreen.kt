package com.example.memento.view

import StartTimelineButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import com.example.memento.model.CountryData
import com.example.memento.model.allCountries
import com.example.memento.viewmodel.UserViewModel

private val Accent = Color(0xFF7C3AED)
private val AccentSoft = Color(0xFFA78BFA)
private val Muted = Color(0xFF5A5A80)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    navController: NavController,
    viewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var countrySearch by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState()

    val birthdayText = viewModel.birthdayText
    val lifeExpectancyText = viewModel.lifeExpectancyText
    val genderSliderPosition = viewModel.genderSliderPosition
    val selectedCountry = viewModel.selectedCountry

    // Skip this screen if birthday is already set
    LaunchedEffect(viewModel.isProfileLoaded) {
        if (viewModel.isProfileLoaded && viewModel.birthdayText.isNotBlank()) {
            navController.navigate(com.example.memento.LifeGridRoute) {
                popUpTo(com.example.memento.StartRoute) { inclusive = true }
            }
        }
    }

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            viewModel.convertMillisToDate(millis)
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp)
        ) {
            Text(
                text = "Your Life in Weeks",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
                fontSize = 30.sp,
                style = TextStyle(lineBreak = LineBreak.Simple)
            )
            Text(
                text = "Every square is one week of your life.",
                fontSize = 15.sp,
                color = Muted,
            )

            Spacer(Modifier.height(24.dp))

            // Birthday picker
            OutlinedTextField(
                value = birthdayText,
                onValueChange = {},
                label = { Text("Date of Birth") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select date")
                    }
                },
            )

            Spacer(Modifier.height(16.dp))

            // Gender slider
            Text(
                text = "Gender",
                fontSize = 13.sp,
                color = Muted,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Male", fontSize = 13.sp, color = Muted)
                Spacer(Modifier.width(8.dp))
                Slider(
                    value = genderSliderPosition,
                    onValueChange = { viewModel.updateGenderSlider(it) },
                    valueRange = 0f..9f,
                    steps = 8,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = AccentSoft,
                        activeTrackColor = Accent,
                        inactiveTrackColor = Muted.copy(alpha = 0.3f),
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text("Female", fontSize = 13.sp, color = Muted)
            }

            Spacer(Modifier.height(16.dp))

            // Country selector
            OutlinedTextField(
                value = selectedCountry?.name ?: "",
                onValueChange = {},
                label = { Text("Country of Origin") },
                readOnly = true,
                placeholder = { Text("Select a country", color = Muted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCountryPicker = true },
                enabled = false,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = Muted,
                ),
            )

            Spacer(Modifier.height(16.dp))

            // Life expectancy (auto-filled but editable)
            OutlinedTextField(
                value = lifeExpectancyText,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = viewModel::updateLifeExpectancy,
                label = { Text("Life expectancy in years") },
                placeholder = { Text("90", color = Muted) },
                supportingText = if (selectedCountry != null) {
                    { Text("Auto-calculated from country & gender", color = AccentSoft, fontSize = 11.sp) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            val canNavigate = birthdayText.isNotBlank()
            StartTimelineButton(navController, enabled = canNavigate)
        }

        // Date picker popup
        if (showDatePicker) {
            Popup(
                onDismissRequest = { showDatePicker = false },
                alignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 64.dp)
                        .shadow(4.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    DatePicker(state = datePickerState, showModeToggle = false)
                }
            }
        }
    }

    // Country picker dialog
    if (showCountryPicker) {
        CountryPickerDialog(
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
private fun CountryPickerDialog(
    search: String,
    onSearchChange: (String) -> Unit,
    onSelect: (CountryData) -> Unit,
    onDismiss: () -> Unit,
) {
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
                            Text(country.name, fontSize = 14.sp)
                            Text(
                                "♂ ${country.maleLE}y  ♀ ${country.femaleLE}y",
                                fontSize = 11.sp,
                                color = Muted,
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
