package com.omkara.sirenservices_internal.models


data class AttendanceModel(
    var id: String = "",                      // Firestore doc ID (yyyy-MM-dd)
    val date: String = "",                    // yyyy-MM-dd
    val status: String = "absent",            // present, absent, leave, halfday, late, holiday
    val checkInTime: String? = null,          // "09:12 AM"
    val checkOutTime: String? = null,         // "06:32 PM"
    val locationIn: String? = null,           // "lat,lng"
    val locationOut: String? = null,
    val notes: String? = null,                // Optional remarks
    val isManual: Boolean = false,            // Manual modifications by admin
    val timestamp: Long = System.currentTimeMillis()
)
