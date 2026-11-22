package com.personal.calen.ui.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.personal.calen.model.Event
import com.personal.calen.model.RecurrenceRule
import com.personal.calen.model.ReminderOption
import com.personal.calen.ui.theme.CalenTheme
import java.time.format.DateTimeFormatter

@Composable
fun EventDetailsDialog(
    event: Event,
    onEditClick: (Event) -> Unit,
    onDeleteClick: (Event) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = CalenTheme.colors
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val timeLabel = when {
        event.isAllDay -> "All day"
        event.startTime != null && event.endTime != null ->
            "${event.startTime.format(timeFormatter)} – ${event.endTime.format(timeFormatter)}"
        event.startTime != null -> event.startTime.format(timeFormatter)
        else -> ""
    }

    val reminderLabel = ReminderOption.fromMinutes(event.reminderOffsetMinutes).label
    val repeatLabel = event.recurrenceRule.label

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Event title
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date & Time
                Text(
                    text = event.startDate.format(dateFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = colors.divider.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                // Reminder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Reminder",
                            modifier = Modifier.size(16.dp),
                            tint = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reminder",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                    Text(
                        text = reminderLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Repeat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repeat",
                            modifier = Modifier.size(16.dp),
                            tint = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Repeat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                    Text(
                        text = repeatLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }

                // Description
                if (!event.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = colors.divider.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showDeleteConfirmDialog = true }) {
                        Text(
                            text = "Delete",
                            color = colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(onClick = { onEditClick(event) }) {
                        Text(
                            text = "Edit",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Close",
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        val isRecurring = event.recurrenceRule != RecurrenceRule.NONE
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete event?",
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = if (isRecurring) {
                        "This is a repeating event. Deleting it will remove all occurrences across the series."
                    } else {
                        "Are you sure you want to delete \"${event.title}\"?"
                    },
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteClick(event)
                        onDismiss()
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = colors.textSecondary
                    )
                }
            },
            containerColor = colors.surface
        )
    }
}
