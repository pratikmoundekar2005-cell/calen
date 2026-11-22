package com.personal.calen.ui.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personal.calen.model.RecurrenceRule
import com.personal.calen.model.ReminderOption
import com.personal.calen.ui.theme.CalenTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    viewModel: EventEditViewModel,
    isEditMode: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = CalenTheme.colors

    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val isAllDay by viewModel.isAllDay.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val startTime by viewModel.startTime.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val endTime by viewModel.endTime.collectAsState()
    val reminderOption by viewModel.reminderOption.collectAsState()
    val recurrenceRule by viewModel.recurrenceRule.collectAsState()
    val isSaveSuccess by viewModel.isSaveSuccess.collectAsState()

    var showDiscardDialog by remember { mutableStateOf(false) }
    var reminderDropdownExpanded by remember { mutableStateOf(false) }
    var repeatDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(isSaveSuccess) {
        if (isSaveSuccess) {
            onNavigateBack()
        }
    }

    val handleBack = {
        if (viewModel.isModified) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(onBack = handleBack)

    val is24Hour = remember(context) { android.text.format.DateFormat.is24HourFormat(context) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, d MMM yyyy") }
    val timeFormatter = remember(is24Hour) {
        if (is24Hour) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("h:mm a")
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit event" else "New event",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = handleBack,
                        modifier = Modifier.semantics { contentDescription = "Cancel and go back" }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveEvent() },
                        enabled = viewModel.isSaveValid,
                        modifier = Modifier.semantics { contentDescription = "Save event" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = if (viewModel.isSaveValid) colors.textPrimary else colors.textSubdued
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Event title
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.title.value = it },
                label = { Text("Title") },
                placeholder = { Text("Event title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Event title input" },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.textPrimary,
                    unfocusedBorderColor = colors.divider,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedLabelColor = colors.textPrimary,
                    unfocusedLabelColor = colors.textSecondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val picked = LocalDate.of(y, m + 1, d)
                                viewModel.startDate.value = picked
                                if (picked.isAfter(viewModel.endDate.value)) {
                                    viewModel.endDate.value = picked
                                }
                            },
                            startDate.year,
                            startDate.monthValue - 1,
                            startDate.dayOfMonth
                        ).show()
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary
                )
                Text(
                    text = startDate.format(dateFormatter),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
            }

            HorizontalDivider(color = colors.divider.copy(alpha = 0.3f))

            // All-day switch row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "All-day",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary
                )
                Switch(
                    checked = isAllDay,
                    onCheckedChange = { viewModel.isAllDay.value = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.selectedDateText,
                        checkedTrackColor = colors.selectedDateFill,
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.background,
                        uncheckedBorderColor = colors.divider
                    )
                )
            }

            // Timed fields (only if not all day)
            if (!isAllDay) {
                HorizontalDivider(color = colors.divider.copy(alpha = 0.3f))

                // Time picker row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Time",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textPrimary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = startTime.format(timeFormatter),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, min ->
                                            val newStart = LocalTime.of(hour, min)
                                            viewModel.startTime.value = newStart
                                            if (!viewModel.endTime.value.isAfter(newStart)) {
                                                viewModel.endTime.value = newStart.plusHours(1)
                                            }
                                        },
                                        startTime.hour,
                                        startTime.minute,
                                        is24Hour
                                    ).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        Text(
                            text = " – ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary
                        )

                        Text(
                            text = endTime.format(timeFormatter),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, min ->
                                            viewModel.endTime.value = LocalTime.of(hour, min)
                                        },
                                        endTime.hour,
                                        endTime.minute,
                                        is24Hour
                                    ).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Validation note if end is before start
                if (!endTime.isAfter(startTime)) {
                    Text(
                        text = "End time must be after start time",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            HorizontalDivider(color = colors.divider.copy(alpha = 0.3f))

            // Reminder selector row
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { reminderDropdownExpanded = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Reminder",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textPrimary
                        )
                        if (isAllDay && reminderOption != ReminderOption.NO_REMINDER) {
                            Text(
                                text = "Delivered at 9:00 AM",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                    Text(
                        text = reminderOption.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }

                DropdownMenu(
                    expanded = reminderDropdownExpanded,
                    onDismissRequest = { reminderDropdownExpanded = false },
                    modifier = Modifier.background(colors.surface)
                ) {
                    for (option in ReminderOption.entries) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    color = colors.textPrimary,
                                    fontWeight = if (option == reminderOption) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                viewModel.reminderOption.value = option
                                reminderDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = colors.divider.copy(alpha = 0.3f))

            // Repeat selector row
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { repeatDropdownExpanded = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Repeat",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textPrimary
                    )
                    Text(
                        text = recurrenceRule.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }

                DropdownMenu(
                    expanded = repeatDropdownExpanded,
                    onDismissRequest = { repeatDropdownExpanded = false },
                    modifier = Modifier.background(colors.surface)
                ) {
                    for (rule in RecurrenceRule.entries) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = rule.label,
                                    color = colors.textPrimary,
                                    fontWeight = if (rule == recurrenceRule) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                viewModel.recurrenceRule.value = rule
                                repeatDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Scope warning if repeating event in edit mode
            if (isEditMode && recurrenceRule != RecurrenceRule.NONE) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: Changes will apply to all occurrences in this series.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes / Description
            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.description.value = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("Add notes or description") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Optional event notes" },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.textPrimary,
                    unfocusedBorderColor = colors.divider,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedLabelColor = colors.textPrimary,
                    unfocusedLabelColor = colors.textSecondary
                )
            )
        }
    }

    // Discard unsaved changes confirmation dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                Text(
                    text = "Discard unsaved changes?",
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "If you go back now, your changes will be lost.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(
                        text = "Discard",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(
                        text = "Keep editing",
                        color = colors.textSecondary
                    )
                }
            },
            containerColor = colors.surface
        )
    }
}
