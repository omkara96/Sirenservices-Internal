package com.omkara.sirenservices_internal.fragments


import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class VehicleInfoFragment : Fragment() {

    private lateinit var edtVehicleNumber: TextInputEditText
    private lateinit var autoVehicleType: AutoCompleteTextView
    private lateinit var edtMake: TextInputEditText
    private lateinit var edtModel: TextInputEditText
    private lateinit var edtYear: TextInputEditText
    private lateinit var edtSeats: TextInputEditText
    private lateinit var edtOdometerAtReg: TextInputEditText
    private lateinit var rgOwnership: RadioGroup
    private lateinit var rbOwn: RadioButton
    private lateinit var rbThirdParty: RadioButton
    private lateinit var lytOwnerSelect: TextInputLayout
    private lateinit var autoOwnerList: AutoCompleteTextView
    private lateinit var autoFuelType: AutoCompleteTextView
    private lateinit var autoTransmission: AutoCompleteTextView
    private lateinit var autoStatus: AutoCompleteTextView
    private lateinit var edtChassis: TextInputEditText
    private lateinit var edtEngine: TextInputEditText

    private val firestore = FirebaseFirestore.getInstance()
    private val userMap = HashMap<String, String>()
    private var selectedOwnerId: String? = FirebaseAuth.getInstance().uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_vehicle_info, container, false)
        bindViews(v)
        setupDropdowns()
        setupOwnership()
        return v
    }

    private fun bindViews(v: View) {
        edtVehicleNumber = v.findViewById(R.id.edtVehicleNumber)
        autoVehicleType = v.findViewById(R.id.autoVehicleType)
        edtMake = v.findViewById(R.id.edtMake)
        edtModel = v.findViewById(R.id.edtModel)
        edtYear = v.findViewById(R.id.edtYear)
        edtSeats = v.findViewById(R.id.edtSeats)
        edtOdometerAtReg = v.findViewById(R.id.edtOdometerAtReg)
        rgOwnership = v.findViewById(R.id.rgOwnership)
        rbOwn = v.findViewById(R.id.rbOwn)
        rbThirdParty = v.findViewById(R.id.rbThirdParty)
        lytOwnerSelect = v.findViewById(R.id.lytOwnerSelect)
        autoOwnerList = v.findViewById(R.id.autoOwnerList)
        autoFuelType = v.findViewById(R.id.autoFuelType)
        autoTransmission = v.findViewById(R.id.autoTransmission)
        autoStatus = v.findViewById(R.id.autoStatus)
        edtChassis = v.findViewById(R.id.edtChassis)
        edtEngine = v.findViewById(R.id.edtEngine)
    }

    private fun setupDropdowns() {
        autoVehicleType.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1,
            listOf("BASIC","ICU","ALS","BLS","VAN")))

        autoFuelType.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1,
            listOf("Petrol","Diesel","CNG","EV")))

        autoTransmission.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1,
            listOf("Manual","Automatic")))

        autoStatus.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1,
            listOf("ACTIVE","UNDER_MAINTENANCE","DECOMMISSIONED")))
    }

    private fun setupOwnership() {
        rbOwn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lytOwnerSelect.visibility = View.GONE
                selectedOwnerId = FirebaseAuth.getInstance().uid
            }
        }
        rbThirdParty.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lytOwnerSelect.visibility = View.VISIBLE
                loadUsers()
            }
        }
    }

    private fun loadUsers() {
        // load users from Firestore
        firestore.collection("users").get()
            .addOnSuccessListener { snap ->
                val names = ArrayList<String>()
                userMap.clear()
                for (doc in snap.documents) {
                    val name = doc.getString("full_name") ?: doc.id
                    names.add(name)
                    userMap[name] = doc.id
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)
                autoOwnerList.setAdapter(adapter)
                autoOwnerList.setOnItemClickListener { _, _, position, _ ->
                    val name = names[position]
                    selectedOwnerId = userMap[name]
                }
            }
            .addOnFailureListener {
                // ignore, will require user manual entry
            }
    }

    // ---------------- VALIDATION & DATA ----------------
    fun validate(): Boolean {
        val veh = edtVehicleNumber.text.toString().trim().uppercase()
        val regex = Regex("^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}\$")
        if (veh.isEmpty()) {
            edtVehicleNumber.error = "Vehicle number required"
            return false
        }
        if (!regex.matches(veh)) {
            edtVehicleNumber.error = "Invalid format (eg MH12AB1234)"
            return false
        }
        if (autoVehicleType.text.toString().trim().isEmpty()) {
            autoVehicleType.error = "Select vehicle type"
            return false
        }
        if (edtMake.text.toString().trim().isEmpty()) {
            edtMake.error = "Enter make"
            return false
        }
        if (edtModel.text.toString().trim().isEmpty()) {
            edtModel.error = "Enter model"
            return false
        }
        val yearStr = edtYear.text.toString().trim()
        if (yearStr.isEmpty()) {
            edtYear.error = "Enter year"
            return false
        }
        val year = yearStr.toIntOrNull() ?: -1
        if (year !in 1950..Calendar.getInstance().get(Calendar.YEAR)) {
            edtYear.error = "Year must be 1950–${Calendar.getInstance().get(Calendar.YEAR)}"
            return false
        }
        val seats = edtSeats.text.toString().trim().toIntOrNull()
        if (seats == null || seats <= 0) {
            edtSeats.error = "Enter seats"
            return false
        }
        val odo = edtOdometerAtReg.text.toString().trim().toDoubleOrNull()
        if (odo == null || odo < 0) {
            edtOdometerAtReg.error = "Enter valid odometer"
            return false
        }
        if (rbThirdParty.isChecked && selectedOwnerId == null) {
            autoOwnerList.error = "Select owner"
            return false
        }
        if (autoFuelType.text.toString().trim().isEmpty()) {
            autoFuelType.error = "Select fuel type"
            return false
        }
        if (autoTransmission.text.toString().trim().isEmpty()) {
            autoTransmission.error = "Select transmission"
            return false
        }
        if (autoStatus.text.toString().trim().isEmpty()) {
            autoStatus.error = "Select status"
            return false
        }
        if (edtChassis.text.toString().trim().isEmpty()) {
            edtChassis.error = "Enter chassis"
            return false
        }
        if (edtEngine.text.toString().trim().isEmpty()) {
            edtEngine.error = "Enter engine no"
            return false
        }
        return true
    }

    fun getData(): Map<String, Any?> {
        val map = HashMap<String, Any?>()
        map["vehicle_number"] = edtVehicleNumber.text.toString().trim().uppercase()
        map["vehicle_type"] = autoVehicleType.text.toString().trim()
        map["make"] = edtMake.text.toString().trim()
        map["model"] = edtModel.text.toString().trim()
        map["manufacture_year"] = edtYear.text.toString().trim().toIntOrNull()
        map["seating_capacity"] = edtSeats.text.toString().trim().toIntOrNull()
        map["odometer_at_registration"] = edtOdometerAtReg.text.toString().trim().toDoubleOrNull()
        map["owner_type"] = if (rbOwn.isChecked) "OWN" else "THIRD_PARTY"
        map["owner_user_id"] = if (rbOwn.isChecked) FirebaseAuth.getInstance().uid else selectedOwnerId
        map["fuel_type"] = autoFuelType.text.toString().trim()
        map["transmission"] = autoTransmission.text.toString().trim()
        map["status"] = autoStatus.text.toString().trim()
        map["chassis_number"] = edtChassis.text.toString().trim()
        map["engine_number"] = edtEngine.text.toString().trim()
        return map
    }
}
