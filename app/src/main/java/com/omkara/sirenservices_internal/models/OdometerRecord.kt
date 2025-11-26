package com.omkara.sirenservices_internal.models

data class OdometerRecord(
    var value: Double = 0.0,
    var timestamp: Long = System.currentTimeMillis(),
    var note: String? = null // like "manual update" or "trip end"
)
