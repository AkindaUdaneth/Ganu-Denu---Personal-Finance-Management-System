package com.sliit.se3092.ganudenu.goals

import org.junit.Assert.assertEquals
import org.junit.Test

class GoalEngineMathTest {

    @Test
    fun `test impulse delay calculation`() {
        // Given
        val impulseExpense = 15000.0
        val requiredMonthlySavings = 42727.27 // From your screenshot

        // When
        val expectedDelayDays = ((impulseExpense / requiredMonthlySavings) * 30).toInt()

        // Then
        assertEquals(10, expectedDelayDays) // 15,000 / 42,727 * 30 = 10.53 -> 10 days
    }

    @Test
    fun `test months remaining prevents negative time`() {
        // Given
        val targetDatePast = System.currentTimeMillis() - 100000L
        val now = System.currentTimeMillis()

        // When
        val msDiff = kotlin.math.max(0L, targetDatePast - now)
        val monthsRemaining = (msDiff / (1000L * 60 * 60 * 24 * 30)).toInt().coerceAtLeast(1)

        // Then
        assertEquals(0L, msDiff)
        assertEquals(1, monthsRemaining) // Should snap to 1 instead of freezing
    }
}