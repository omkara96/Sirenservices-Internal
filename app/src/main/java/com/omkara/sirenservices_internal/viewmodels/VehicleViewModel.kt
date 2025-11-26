package com.omkara.sirenservices_internal.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VehicleViewModel : ViewModel() {

    // -------------------------------
    //  VEHICLE INFO (STEP 1)
    // -------------------------------
    val vehicleNumber = MutableLiveData<String>()
    val rcNumber = MutableLiveData<String>()
    val make = MutableLiveData<String>()
    val model = MutableLiveData<String>()
    val manufactureYear = MutableLiveData<Int?>()
    val seatingCapacity = MutableLiveData<Int?>()

    // Odometer captured ONCE during registration
    var odometerAtRegistration: Double? = null
    val photos = MutableLiveData<MutableMap<String, String>>(mutableMapOf())
    // -------------------------------
    //  COMPLIANCE DATA (STEP 2)
    // -------------------------------
    val insuranceProvider = MutableLiveData<String>()
    val insuranceNumber = MutableLiveData<String>()
    val insuranceStartDate = MutableLiveData<String?>()
    val insuranceEndDate = MutableLiveData<String?>()

    val pucNumber = MutableLiveData<String>()
    val pucStartDate = MutableLiveData<String?>()
    val pucEndDate = MutableLiveData<String?>()

    val rcExpiryDate = MutableLiveData<String?>()
    val permitNumber = MutableLiveData<String?>()
    val permitExpiry = MutableLiveData<String?>()
    val fitnessExpiry = MutableLiveData<String?>()


    // -------------------------------
    //  SERVICE INFO (STEP 3)
    // -------------------------------
    var lastServiceDate: String? = null
    var lastServiceOdometer: Double? = null
    var lastServiceWorkshop: String? = null
    var nextServiceDueKm: Double? = null


    // -------------------------------
    // FUNCTIONS TO UPDATE STEP 1
    // -------------------------------
    fun updateVehicleInfo(data: Map<String, Any?>) {
        vehicleNumber.value = data["vehicle_number"] as? String
        rcNumber.value = data["rc_number"] as? String
        make.value = data["make"] as? String
        model.value = data["model"] as? String
        manufactureYear.value = data["manufacture_year"] as? Int
        seatingCapacity.value = data["seating_capacity"] as? Int
        odometerAtRegistration = data["odometer_at_registration"] as? Double
    }


    // -------------------------------
    // FUNCTIONS TO UPDATE STEP 2
    // -------------------------------
    fun updateComplianceInfo(data: Map<String, Any?>) {
        insuranceProvider.value = data["insurance_provider"] as? String
        insuranceNumber.value = data["insurance_number"] as? String
        insuranceStartDate.value = data["insurance_start"] as? String
        insuranceEndDate.value = data["insurance_end"] as? String

        pucNumber.value = data["puc_number"] as? String
        pucStartDate.value = data["puc_start"] as? String
        pucEndDate.value = data["puc_end"] as? String

        rcExpiryDate.value = data["rc_expiry"] as? String
        permitNumber.value = data["permit_number"] as? String
        permitExpiry.value = data["permit_expiry"] as? String
        fitnessExpiry.value = data["fitness_expiry"] as? String
    }


    // -------------------------------
    // FUNCTIONS TO UPDATE STEP 3
    // -------------------------------
    fun updateServiceInfo(data: Map<String, Any?>) {
        lastServiceDate = data["last_service_date"] as? String
        lastServiceOdometer = data["last_service_odometer"] as? Double
        lastServiceWorkshop = data["last_service_workshop"] as? String
        nextServiceDueKm = data["next_service_due_km"] as? Double
    }


    // -------------------------------
    // FINAL PAYLOAD FOR FIRESTORE
    // -------------------------------
    fun getFinalPayload(): Map<String, Any?> {

        val payload = HashMap<String, Any?>()

        // Vehicle Info
        payload["vehicle_number"] = vehicleNumber.value
        payload["rc_number"] = rcNumber.value
        payload["make"] = make.value
        payload["model"] = model.value
        payload["manufacture_year"] = manufactureYear.value
        payload["seating_capacity"] = seatingCapacity.value
        payload["odometer_at_registration"] = odometerAtRegistration

        // Compliance
        payload["insurance_provider"] = insuranceProvider.value
        payload["insurance_number"] = insuranceNumber.value
        payload["insurance_start"] = insuranceStartDate.value
        payload["insurance_end"] = insuranceEndDate.value

        payload["puc_number"] = pucNumber.value
        payload["puc_start"] = pucStartDate.value
        payload["puc_end"] = pucEndDate.value

        payload["rc_expiry"] = rcExpiryDate.value
        payload["permit_number"] = permitNumber.value
        payload["permit_expiry"] = permitExpiry.value
        payload["fitness_expiry"] = fitnessExpiry.value

        // Service
        payload["last_service_date"] = lastServiceDate
        payload["last_service_odometer"] = lastServiceOdometer
        payload["last_service_workshop"] = lastServiceWorkshop
        payload["next_service_due_km"] = nextServiceDueKm

        payload["created_at"] = System.currentTimeMillis()

        return payload
    }

    // -------------------------------
    // helper methods for photos
    // -------------------------------
    fun setPhoto(key: String, uri: String) {
        val map = photos.value ?: mutableMapOf()
        map[key] = uri
        photos.postValue(map)
    }

    fun getPhoto(key: String): String? {
        return photos.value?.get(key)
    }

    fun clearPhotos() {
        photos.postValue(mutableMapOf())
    }

    fun putAllPhotos(newMap: Map<String, String>) {
        val map = photos.value ?: mutableMapOf()
        map.clear()
        map.putAll(newMap)
        photos.postValue(map)
    }
}
