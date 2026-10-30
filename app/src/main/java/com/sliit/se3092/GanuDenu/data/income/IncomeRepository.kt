package com.sliit.se3092.GanuDenu.data.income

import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

class IncomeSignInRequiredException : IllegalStateException("Please sign in to view and save income.")

interface IncomeRepository {
    fun observeMonth(month: YearMonth): Flow<IncomeSnapshot>
    suspend fun save(entry: IncomeEntry)
}
