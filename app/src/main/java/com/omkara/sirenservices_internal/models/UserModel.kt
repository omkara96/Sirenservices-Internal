package com.omkara.sirenservices_internal.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class UserModel(
    var id: String = "",
    var userId: String = "",
    val firstName: String = "",
    val middleName: String? = null,
    val lastName: String? = null,
    val dob: String? = null,
    val mobile: String = "",
    val email: String? = null,
    val address: String? = null,
    val password: String? = "",
    val role: String = "owner",      // owner/driver/staff/operator
    val status: String = "active",   // active/inactive

    val profilePhoto: String? = null,

    val driver: DriverData? = null,

    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,

    /* ---------- NON-SAVED / RUNTIME FIELDS FOR UI ---------- */

    // Attendance today (loaded from /attendance/{date})
    @get:Exclude var attendanceToday: Boolean = false,

    // Trips done today
    @get:Exclude var tripsToday: Int = 0,

    // For all roles (not just drivers) - available / occupied / busy
    @get:Exclude var availability: String = "available",

    // Future feature: salary pending
    @get:Exclude var pendingSalary: Double = 0.0,

    // Future: outstanding advance
    @get:Exclude var advanceOutstanding: Double = 0.0
)

data class DriverData(
    val licenseNo: String? = null,
    val aadharNo: String? = null,
    val panNo: String? = null,
    val accountNo: String? = null,
    val ifsc: String? = null,
    val bankName: String? = null,
    val branchName: String? = null,

    val documents: Map<String, String>? = null, // aadharFront, aadharBack, license, etc.

    val availability: String = "available" // available | occupied
)
