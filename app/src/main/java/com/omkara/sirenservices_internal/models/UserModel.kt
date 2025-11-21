package com.omkara.sirenservices_internal.models

import com.google.firebase.Timestamp

data class UserModel(
    var id: String = "",
    var firstName: String = "",
    var middleName: String? = null,
    var lastName: String = "",
    var dob: String? = null,               // store as "dd/MM/yyyy" string
    var mobile: String = "",
    var email: String? = null,
    var address: String? = null,
    var role: String = "User",
    var status: String = "ACTIVE",         // ACTIVE / INACTIVE / OCCUPIED
    var created_at: Timestamp? = null,
    var updated_at: Timestamp? = null
) {
    fun fullName(): String = listOfNotNull(firstName, middleName, lastName).joinToString(" ").trim()
}
