package com.omkara.sirenservices_internal.models

data class SalarySummary(
    val month: String = "",
    val totalBase: Double = 0.0,
    val totalAllowances: Double = 0.0,
    val totalBonus: Double = 0.0,
    val totalOvertime: Double = 0.0,
    val totalDeductions: Double = 0.0,
    val netSalary: Double = 0.0,
    val isPaid: Boolean = false,
    val paidOn: Long? = null,
)
