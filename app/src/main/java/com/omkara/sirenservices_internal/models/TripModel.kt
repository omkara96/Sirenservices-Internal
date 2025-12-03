package com.omkara.sirenservices_internal.models

import com.google.firebase.Timestamp

data class TripModel(
    var id: String = "",

    // Basic Trip Info
    var trip_number: String = "",
    var trip_date: String = "",

    // Vehicle Info
    var vehicle_id: String? = null,
    var vehicle_number: String? = null,
    var vehicle_display: String? = null,

    // Driver Info
    var driver_id: String? = null,
    var driver_name: String? = null,
    var driver_display: String? = null,

    // Locations
    var pickup: String? = null,
    var intermediate_stops: MutableList<String> = mutableListOf(),
    var final_drop: String? = null,

    // GPS (Optional but future ready)
    var pickup_gps: String? = null,
    var drop_gps: String? = null,

    // Odometer Readings
    var start_odometer: Int? = null,   // Trip Start
    var end_odometer: Int? = null,     // Trip End

    // Distance & Duration
    var total_km: Double? = null,
    var standby_hours: Double? = null,
    var total_time_minutes: Int? = null,

    // Trip Costing
    var trip_cost: Double = 0.0,
    var payment_mode: String? = null,
    var pending_amount: Double = 0.0,
    var pending_with: String? = null,
    var head_office_deposit: Double = 0.0,

    // Vehicle Expenses
    var fuel_cost: Double = 0.0,
    var fuel_liters: Double = 0.0,
    var servicing_cost: Double = 0.0,
    var servicing_km: Int? = null,
    var mechanic_name: String? = null,
    var mechanic_cost: Double = 0.0,

    // Patient / Customer Info
    var patient_name: String? = null,
    var patient_number: String? = null,
    var attender_name: String? = null,
    var attender_number: String? = null,
    var call_source: String? = null, // Direct / Third Party

    // District / Area (helps analytics)
    var district: String? = null,

    // Billing
    var bill_id: String? = null,

    // Audit Info
    var created_by: String? = null,
    var created_at: Timestamp? = null,

    // Trip Status
    var status: String = "ASSIGNED",
    // ASSIGNED / STARTED / COMPLETED / CANCELLED

    // Lifecycle Times
    var start_timestamp: Timestamp? = null,
    var end_timestamp: Timestamp? = null,
    var closed_at: Timestamp? = null,
    var closed_by: String? = null,
    var closing_notes: String? = null
)
