package com.omkara.sirenservices_internal.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.omkara.sirenservices_internal.models.*
import com.google.firebase.Timestamp

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
    val chassis_number = MutableLiveData<String>()
    val engine_number = MutableLiveData<String>()
    val fuelType = MutableLiveData<String>()
    val transmissionType = MutableLiveData<String>()
    val vehicleOwnership = MutableLiveData<String>()

    var odometerAtRegistration: Double? = null

    val photos = MutableLiveData<MutableMap<String, String>>(mutableMapOf())


    // -------------------------------
    //  COMPLIANCE DATA (NEW STRUCTURE)
    // -------------------------------
    private val _compliance = MutableLiveData<ComplianceModel>()
    val compliance: MutableLiveData<ComplianceModel> get() = _compliance


    fun updateComplianceInfo(model: ComplianceModel) {
        _compliance.value = model
    }


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
        chassis_number.value = data["chassis_number"] as? String
        engine_number.value = data["engine_number"] as? String
        fuelType.value = data["fuel_type"] as? String
        transmissionType.value = data["transmission"] as? String
        vehicleOwnership.value = data["owner_type"] as? String
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
    // FINAL FIRESTORE PAYLOAD
    // -------------------------------
    fun getFinalPayload(): Map<String, Any?> {

        val payload = HashMap<String, Any?>()

        // Basic Vehicle Info
        payload["vehicle_number"] = vehicleNumber.value
        payload["rc_number"] = rcNumber.value
        payload["make"] = make.value
        payload["model"] = model.value
        payload["manufacture_year"] = manufactureYear.value
        payload["seating_capacity"] = seatingCapacity.value
        payload["odometer_at_registration"] = odometerAtRegistration
        payload["chassis_number"] = chassis_number.value
        payload["engine_number"] = engine_number.value
        payload["transmission"] = transmissionType.value
        payload["fuel_type"] = fuelType.value
        payload["owner_type"] = vehicleOwnership.value

        // PHOTOS
        payload["photos"] = photos.value

        // COMPLIANCE (full new model)
        payload["compliance"] = _compliance.value ?: ComplianceModel()

        // SERVICE INFO
        payload["last_service_date"] = lastServiceDate
        payload["last_service_odometer"] = lastServiceOdometer
        payload["last_service_workshop"] = lastServiceWorkshop
        payload["next_service_due_km"] = nextServiceDueKm

        payload["created_at"] = Timestamp.now()
        payload["updated_at"] = Timestamp.now()

        return payload
    }


    // -------------------------------
    // PHOTO helpers
    // -------------------------------
    fun setPhoto(key: String, url: String) {
        val updated = photos.value?.toMutableMap() ?: mutableMapOf()
        updated[key] = url
        photos.postValue(updated)
    }

    fun getPhoto(key: String): String? = photos.value?.get(key)

    fun clearPhotos() {
        photos.postValue(mutableMapOf())
    }

    fun putAllPhotos(newMap: Map<String, String>) {
        val map = photos.value ?: mutableMapOf()
        map.clear()
        map.putAll(newMap)
        photos.postValue(map)
    }

    // Add inside VehicleViewModel

    val complianceDocuments = MutableLiveData<MutableMap<String, String>>(mutableMapOf())

    fun setComplianceDocument(key: String, url: String) {
        val map = complianceDocuments.value ?: mutableMapOf()
        map[key] = url
        complianceDocuments.postValue(map)
    }

    fun getComplianceDocument(key: String): String? {
        return complianceDocuments.value?.get(key)
    }

}
