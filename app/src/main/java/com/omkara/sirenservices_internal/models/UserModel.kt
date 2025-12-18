package com.omkara.sirenservices_internal.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class UserModel(
    var id: String = "",
    var userId: String = "",

    var firstName: String = "",
    var middleName: String? = null,
    var lastName: String? = null,
    var dob: String? = null,

    var mobile: String = "",
    var email: String? = null,
    var address: String? = null,

  //  @get:Exclude
    var password: String? = null,   // NEVER deserialize password

    var role: String = "owner",
    var status: String = "active",

    var profilePhoto: String? = null,

    var driver: DriverData? = null,

    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null,

    /* ---------- UI ONLY ---------- */

    @get:Exclude var attendanceToday: Boolean = false,
    @get:Exclude var tripsToday: Int = 0,
    @get:Exclude var availability: String = "available",
    @get:Exclude var pendingSalary: Double = 0.0,
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
