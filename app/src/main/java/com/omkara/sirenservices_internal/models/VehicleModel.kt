package com.omkara.sirenservices_internal.models

import com.google.firebase.Timestamp

data class VehicleModel(

    val vehicle_number: String = "",
    val vehicle_type: String = "",
    val make: String = "",
    val model: String = "",
    val manufacture_year: Int? = null,
    val seating_capacity: Int? = null,
    val odometer_at_registration: Double? = null,
    val fuel_type: String = "",
    val transmission: String = "",
    val status: String = "ACTIVE",
    val owner_type: String = "OWN",
    val owner_user_id: String? = null,
    val chassis_number: String = "",
    val engine_number: String = "",

    // New unified compliance structure
    val compliance: ComplianceModel = ComplianceModel(),

    // service
    val last_service_date: String? = null,
    val last_service_odometer: Double? = null,
    val last_service_workshop: String? = null,
    val last_service_notes: String? = null,
    val next_service_due_km: Double? = null,

    val created_at: Timestamp? = null,
    val updated_at: Timestamp? = null
)
