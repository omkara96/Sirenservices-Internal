package com.omkara.sirenservices_internal.models

data class ServiceRecord(
    val serviceType: String = "",
    val serviceCost: Double = 0.0,
    val serviceDate: String = "",
    val odometer_km: Double = 0.0,
    val notes: String = "",
    val createdAt: com.google.firebase.Timestamp? = null
)

