package com.omkara.sirenservices_internal.models

data class SalaryEntry(
    val id: String = "",
    val type: String = "",         // "base", "allowance", "bonus", "overtime", "deduction", "fine"
    val amount: Double = 0.0,       // allowances & bonus = positive, deductions/fines = negative
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)
