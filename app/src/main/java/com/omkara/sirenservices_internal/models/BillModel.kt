package com.omkara.sirenservices_internal.models

data class BillModel(
    var bill_id: String = "",
    var trip_id: String = "",
    var base_trip_cost: Double = 0.0,
    var components: List<BillComponent> = emptyList(),
    var gst_enabled: Boolean = false,
    var gst_percent: Double = 0.0,
    var gst_amount: Double = 0.0,
    var total_amount: Double = 0.0,
    var created_at: Long = System.currentTimeMillis(),
    var pdf_path: String? = null
)
