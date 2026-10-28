package com.sliit.se3092.GanuDenu.ui.goals

data class GoalFormUiState(
    val goalName: String = "",
    val targetAmount: String = "",
    val targetDate: String = "",
    val currentSaved: String = "",
    val showSuccessSnackbar: Boolean = false
)