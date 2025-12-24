package com.omkara.sirenservices_internal.models

enum class AlertType {
    BILL_PENDING,
    COMPLIANCE_EXPIRED,
    COMPLIANCE_EXPIRING
}

data class DashboardAlert(
    val type: AlertType,
    val title: String,
    val description: String,
    val refId: String,        // trip_id or vehicle_id
    val severity: Int         // 1 = low, 2 = medium, 3 = high
)
