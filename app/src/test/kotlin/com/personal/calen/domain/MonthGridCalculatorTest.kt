package com.personal.calen.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class MonthGridCalculatorTest {

    @Test
    fun testNovember2026_startsSunday_mondayFirst_takes6Rows() {
        // Nov 1, 2026 is Sunday. With Monday as firstDayOfWeek, Nov 1 is the 7th item of week 1.
        // Nov has 30 days. Nov 30 is Monday. So it spans 6 weeks (42 cells).
        val yearMonth = YearMonth.of(2026, 11)
        val selected = LocalDate.of(2026, 11, 21)
        val today = LocalDate.of(2026, 11, 21)

        val grid = MonthGridCalculator.calculateMonthGrid(
            yearMonth = yearMonth,
            selectedDate = selected,
            today = today,
            firstDayOfWeek = DayOfWeek.MONDAY
        )

        assertEquals(42, grid.size)
        // First day in grid should be Monday Oct 26, 2026
        assertEquals(LocalDate.of(2026, 10, 26), grid.first().date)
        assertFalse(grid.first().isCurrentMonth)

        // Nov 1 is at index 6
        assertEquals(LocalDate.of(2026, 11, 1), grid[6].date)
        assertTrue(grid[6].isCurrentMonth)

        // Last day of Nov (Nov 30) is at index 35
        assertEquals(LocalDate.of(2026, 11, 30), grid[35].date)
        assertTrue(grid[35].isCurrentMonth)

        // Last day of grid is Sunday Dec 6, 2026
        assertEquals(LocalDate.of(2026, 12, 6), grid.last().date)
        assertFalse(grid.last().isCurrentMonth)

        // Selected and today check
        val selectedDay = grid.first { it.date == selected }
        assertTrue(selectedDay.isSelected)
        assertTrue(selectedDay.isToday)
    }

    @Test
    fun testNovember2026_sundayFirst_takes5Rows() {
        // Nov 1, 2026 is Sunday. With Sunday as first day, it starts right on cell 0.
        // Nov 30 is Monday (cell 29). Ends on Saturday Dec 5 (cell 34). Exactly 35 cells (5 rows).
        val yearMonth = YearMonth.of(2026, 11)
        val selected = LocalDate.of(2026, 11, 1)

        val grid = MonthGridCalculator.calculateMonthGrid(
            yearMonth = yearMonth,
            selectedDate = selected,
            today = LocalDate.of(2026, 11, 21),
            firstDayOfWeek = DayOfWeek.SUNDAY
        )

        assertEquals(35, grid.size)
        assertEquals(LocalDate.of(2026, 11, 1), grid.first().date)
        assertTrue(grid.first().isCurrentMonth)
        assertEquals(LocalDate.of(2026, 12, 5), grid.last().date)
    }

    @Test
    fun testFebruary2021_mondayFirst_takesExactly4Rows() {
        // Feb 1, 2021 was a Monday. Feb 2021 had 28 days.
        // It starts on Monday and ends on Sunday Feb 28. Exactly 28 cells (4 rows).
        val yearMonth = YearMonth.of(2021, 2)
        val grid = MonthGridCalculator.calculateMonthGrid(
            yearMonth = yearMonth,
            selectedDate = LocalDate.of(2021, 2, 10),
            today = LocalDate.of(2026, 11, 21),
            firstDayOfWeek = DayOfWeek.MONDAY
        )

        assertEquals(28, grid.size)
        assertEquals(LocalDate.of(2021, 2, 1), grid.first().date)
        assertEquals(LocalDate.of(2021, 2, 28), grid.last().date)
        assertTrue(grid.all { it.isCurrentMonth })
    }

    @Test
    fun testFebruaryLeapYear2024() {
        // 2024 is a leap year, Feb has 29 days
        val yearMonth = YearMonth.of(2024, 2)
        val grid = MonthGridCalculator.calculateMonthGrid(
            yearMonth = yearMonth,
            selectedDate = LocalDate.of(2024, 2, 29),
            today = LocalDate.of(2026, 11, 21),
            firstDayOfWeek = DayOfWeek.MONDAY
        )

        // Feb 1 was Thursday. Feb 29 was Thursday. Total 35 cells.
        assertEquals(35, grid.size)
        val leapDay = grid.firstOrNull { it.date == LocalDate.of(2024, 2, 29) }
        assertTrue(leapDay != null && leapDay.isCurrentMonth && leapDay.isSelected)
    }

    @Test
    fun testYearBoundary_DecemberToJanuary() {
        val yearMonth = YearMonth.of(2026, 12)
        val grid = MonthGridCalculator.calculateMonthGrid(
            yearMonth = yearMonth,
            selectedDate = LocalDate.of(2026, 12, 31),
            today = LocalDate.of(2026, 11, 21),
            firstDayOfWeek = DayOfWeek.MONDAY
        )

        // Last cell should extend into January 2027
        val jan2027Days = grid.filter { it.date.year == 2027 && it.date.monthValue == 1 }
        assertTrue(jan2027Days.isNotEmpty())
        assertFalse(jan2027Days.first().isCurrentMonth)
    }

    @Test
    fun testOrderedWeekDays() {
        val mondayStart = MonthGridCalculator.getOrderedWeekDays(DayOfWeek.MONDAY)
        assertEquals(7, mondayStart.size)
        assertEquals(DayOfWeek.MONDAY, mondayStart.first())
        assertEquals(DayOfWeek.SUNDAY, mondayStart.last())

        val sundayStart = MonthGridCalculator.getOrderedWeekDays(DayOfWeek.SUNDAY)
        assertEquals(7, sundayStart.size)
        assertEquals(DayOfWeek.SUNDAY, sundayStart.first())
        assertEquals(DayOfWeek.SATURDAY, sundayStart.last())
    }
}
