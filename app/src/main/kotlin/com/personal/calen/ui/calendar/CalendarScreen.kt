package com.personal.calen.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.calen.domain.MonthGridCalculator
import com.personal.calen.model.CalendarDay
import com.personal.calen.model.Event
import com.personal.calen.model.RecurrenceRule
import com.personal.calen.ui.theme.CalenTheme
import com.personal.calen.ui.theme.CalenWordmarkStyle
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateEvent: (LocalDate) -> Unit,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedMonth by viewModel.displayedMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val monthGrid by viewModel.monthGrid.collectAsState()
    val selectedEvents by viewModel.selectedDateEvents.collectAsState()
    val weekStartOption by viewModel.weekStart.collectAsState()

    val colors = CalenTheme.colors
    val firstDayOfWeek = viewModel.resolveFirstDayOfWeek(weekStartOption)
    val orderedWeekDays = remember(firstDayOfWeek) {
        MonthGridCalculator.getOrderedWeekDays(firstDayOfWeek)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calen",
                        style = CalenWordmarkStyle,
                        color = colors.textPrimary
                    )
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.semantics { contentDescription = "Settings" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary,
                    actionIconContentColor = colors.textPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToCreateEvent(selectedDate) },
                containerColor = colors.selectedDateFill,
                contentColor = colors.selectedDateText,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .semantics { contentDescription = "Add event for selected date" }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add event",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Month navigation header
            MonthNavigationHeader(
                displayedMonth = displayedMonth,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onTodayClick = { viewModel.goToToday() }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Weekday initials row (7 columns)
            WeekdayHeaderRow(orderedWeekDays = orderedWeekDays)

            Spacer(modifier = Modifier.height(4.dp))

            // 7-column Month Grid with swipe support
            MonthGrid(
                days = monthGrid,
                onDateSelected = { day -> viewModel.selectDate(day.date) },
                onSwipeLeft = { viewModel.nextMonth() },
                onSwipeRight = { viewModel.previousMonth() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = colors.divider.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // Agenda for selected date
            SelectedDateAgenda(
                selectedDate = selectedDate,
                events = selectedEvents,
                onEventClick = onEventClick,
                onAddEventClick = { onNavigateToCreateEvent(selectedDate) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MonthNavigationHeader(
    displayedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit
) {
    val colors = CalenTheme.colors
    val monthTitle = remember(displayedMonth) {
        val monthName = displayedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        "$monthName ${displayedMonth.year}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = monthTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TextButton(
                onClick = onTodayClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary
                )
            }

            IconButton(
                onClick = onPreviousMonth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous month",
                    tint = colors.textPrimary
                )
            }

            IconButton(
                onClick = onNextMonth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next month",
                    tint = colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun WeekdayHeaderRow(
    orderedWeekDays: List<DayOfWeek>
) {
    val colors = CalenTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (dayOfWeek in orderedWeekDays) {
            val label = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2)
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MonthGrid(
    days: List<CalendarDay>,
    onDateSelected: (CalendarDay) -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    val weeks = remember(days) { days.chunked(7) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .pointerInput(days) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAccumulator < -80f) {
                            onSwipeLeft()
                        } else if (dragAccumulator > 80f) {
                            onSwipeRight()
                        }
                        dragAccumulator = 0f
                    },
                    onDragCancel = {
                        dragAccumulator = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragAccumulator += dragAmount
                    }
                )
            },
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (week in weeks) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (day in week) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { onDateSelected(day) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CalendarDayCell(day = day)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay
) {
    val colors = CalenTheme.colors
    val date = day.date

    val textColor = when {
        day.isSelected -> colors.selectedDateText
        day.isCurrentMonth -> colors.textPrimary
        else -> colors.textSubdued
    }

    val cellBackgroundModifier = when {
        day.isSelected -> Modifier.background(colors.selectedDateFill, CircleShape)
        day.isToday -> Modifier.border(1.dp, colors.todayIndicator, CircleShape)
        else -> Modifier
    }

    Column(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .then(cellBackgroundModifier)
            .semantics {
                contentDescription = "${date.dayOfMonth} ${date.month.name.lowercase()}, ${date.year}"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${date.dayOfMonth}",
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (day.isSelected || day.isToday) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )

        // Event indicator dot
        if (day.hasEvents) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        color = if (day.isSelected) colors.selectedDateText else colors.eventDot,
                        shape = CircleShape
                    )
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun SelectedDateAgenda(
    selectedDate: LocalDate,
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    onAddEventClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CalenTheme.colors
    val dateHeaderFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy") }
    val formattedDate = remember(selectedDate) { selectedDate.format(dateHeaderFormatter) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Nothing scheduled",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onAddEventClick) {
                        Text(
                            text = "+ Add event",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    AgendaEventItem(
                        event = event,
                        onClick = { onEventClick(event) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AgendaEventItem(
    event: Event,
    onClick: () -> Unit
) {
    val colors = CalenTheme.colors
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val timeLabel = when {
        event.isAllDay -> "All day"
        event.startTime != null && event.endTime != null ->
            "${event.startTime.format(timeFormatter)} – ${event.endTime.format(timeFormatter)}"
        event.startTime != null -> event.startTime.format(timeFormatter)
        else -> ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical indicator accent
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(36.dp)
                .background(colors.textPrimary, RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (timeLabel.isNotEmpty()) {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }

                if (event.recurrenceRule != RecurrenceRule.NONE) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repeats",
                            modifier = Modifier.size(13.dp),
                            tint = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = event.recurrenceRule.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }

                if (event.reminderOffsetMinutes != null) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Reminder enabled",
                        modifier = Modifier.size(13.dp),
                        tint = colors.textSecondary
                    )
                }
            }
        }
    }
}
