package com.omkara.sirenservices_internal.models


data class AttendanceSummary(
    val present: Int = 0,
    val absent: Int = 0,
    val late: Int = 0,
    val half: Int = 0,
    val leave: Int = 0,
    val holiday: Int = 0,
    val totalDays: Int = 0
) {
    val presentPercent: Double
        get() = if (totalDays == 0) 0.0 else (present.toDouble() / totalDays) * 100.0
}
