package com.omkara.sirenservices_internal.models

import com.google.firebase.Timestamp

data class TripModel(
    var id: String = "",
    var trip_number: String = "",
    var trip_date: String = "",
    var vehicle_id: String? = null,
    var vehicle_number: String? = null,
    var driver_id: String? = null,
    var driver_name: String? = null,
    var pickup: String? = null,
    var intermediate_stops: MutableList<String> = mutableListOf(),
    var final_drop: String? = null,
    var trip_cost: Double = 0.0,
    var payment_mode: String? = null,
    var pending_amount: Double = 0.0,
    var pending_with: String? = null,
    var fuel_cost: Double = 0.0,
    var fuel_liters: Double = 0.0,
    var servicing_cost: Double = 0.0,
    var servicing_km: Int? = null,
    var mechanic_name: String? = null,
    var mechanic_cost: Double = 0.0,
    var patient_name: String? = null,
    var patient_number: String? = null,
    var call_source: String? = null,
    var head_office_deposit: Double = 0.0,
    var created_by: String? = null,
    var created_at: Timestamp? = null,
    var status: String = "ASSIGNED",
    var bill_id: String? = null,
    var driver_display: String? = null,
    var vehicle_display: String? = null
)
