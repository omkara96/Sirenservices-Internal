package com.omkara.sirenservices_internal.models

import android.os.Parcel
import android.os.Parcelable
import androidx.versionedparcelable.VersionedParcelize
import com.google.firebase.Timestamp

data class ComplianceHistory(
    val provider: String? = null,
    val number: String? = null,
    val valid_from: String? = null,
    val valid_till: String? = null,
    val premium: Double? = null,
    val type: String? = null,
    val certificate_no: String? = null,
    val updated_at: Timestamp? = null
)

data class ComplianceCurrent(
    val provider: String? = null,
    val number: String? = null,
    val valid_from: String? = null,
    val valid_till: String? = null,
    val premium: Double? = null,
    val type: String? = null,
    val certificate_no: String? = null,
    val updated_at: Timestamp? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Timestamp::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(provider)
        parcel.writeString(number)
        parcel.writeString(valid_from)
        parcel.writeString(valid_till)
        parcel.writeValue(premium)
        parcel.writeString(type)
        parcel.writeString(certificate_no)
        parcel.writeParcelable(updated_at, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ComplianceCurrent> {
        override fun createFromParcel(parcel: Parcel): ComplianceCurrent {
            return ComplianceCurrent(parcel)
        }

        override fun newArray(size: Int): Array<ComplianceCurrent?> {
            return arrayOfNulls(size)
        }
    }
}

fun ComplianceCurrent.toMap(): Map<String, Any?> {
    return mapOf(
        "provider" to provider,
        "certificate_no" to certificate_no,
        "number" to number,
        "type" to type,
        "premium" to premium,
        "valid_from" to valid_from,
        "valid_till" to valid_till,
        "updated_at" to updated_at
    )
}

object ComplianceParser {

    fun toCurrent(map: Map<*, *>?): ComplianceCurrent? {
        if (map == null) return null

        return ComplianceCurrent(
            provider = map["provider"] as? String,
            certificate_no = map["certificate_no"] as? String,
            number = map["number"] as? String,
            type = map["type"] as? String,
            premium = (map["premium"] as? Number)?.toDouble(),
            valid_from = map["valid_from"] as? String,
            valid_till = map["valid_till"] as? String,
            updated_at = map["updated_at"] as? com.google.firebase.Timestamp
        )
    }

    fun toHistory(list: List<*>?): List<ComplianceCurrent> {
        if (list == null) return emptyList()
        return list.mapNotNull { item ->
            toCurrent(item as? Map<*, *>)
        }
    }

    fun fromFirestore(map: Map<*, *>): ComplianceModel {
        return ComplianceModel(
            insurance = ComplianceSection(
                current = toCurrent(map["insurance"]?.let { (it as Map<*, *>)["current"] as? Map<*, *> }),
                history = toHistory(map["insurance"]?.let { (it as Map<*, *>)["history"] as? List<*> })
            ),
            puc = ComplianceSection(
                current = toCurrent(map["puc"]?.let { (it as Map<*, *>)["current"] as? Map<*, *> }),
                history = toHistory(map["puc"]?.let { (it as Map<*, *>)["history"] as? List<*> })
            ),
            permit = ComplianceSection(
                current = toCurrent(map["permit"]?.let { (it as Map<*, *>)["current"] as? Map<*, *> }),
                history = toHistory(map["permit"]?.let { (it as Map<*, *>)["history"] as? List<*> })
            ),
            fitness = ComplianceSection(
                current = toCurrent(map["fitness"]?.let { (it as Map<*, *>)["current"] as? Map<*, *> }),
                history = toHistory(map["fitness"]?.let { (it as Map<*, *>)["history"] as? List<*> })
            )
        )
    }
}



data class ComplianceSection(
    val current: ComplianceCurrent? = null,
    val history: List<ComplianceCurrent> = emptyList()
)

data class ComplianceModel(
    val insurance: ComplianceSection = ComplianceSection(),
    val puc: ComplianceSection = ComplianceSection(),
    val permit: ComplianceSection = ComplianceSection(),
    val fitness: ComplianceSection = ComplianceSection()
)
