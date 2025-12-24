package com.omkara.sirenservices_internal.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity
import com.omkara.sirenservices_internal.models.ServiceRecord
import com.omkara.sirenservices_internal.viewmodels.VehicleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class RegisterVehicleServiceNSaveFragment : Fragment() {

    private val vm: VehicleViewModel by activityViewModels()

    private lateinit var autoServiceType: AutoCompleteTextView
    private lateinit var edtLastServiceDate: TextInputEditText
    private lateinit var edtLastServiceOdometer: TextInputEditText
    private lateinit var edtWorkshop: TextInputEditText
    private lateinit var edtNextServiceKm: TextInputEditText

    private lateinit var btnPrev: FloatingActionButton
    private lateinit var btnNext: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_service_info, container, false)

        bind(v)
        setupServiceTypeDropdown()
        fixFabInsets(v)
        setupDatePicker()
        setupNavigation()
        prefillIfAvailable()

        return v
    }

    private fun bind(v: View) {
        autoServiceType = v.findViewById(R.id.autoServiceType)
        edtLastServiceDate = v.findViewById(R.id.edtLastServiceDate)
        edtLastServiceOdometer = v.findViewById(R.id.edtLastServiceOdometer)
        edtWorkshop = v.findViewById(R.id.edtWorkshop)
        edtNextServiceKm = v.findViewById(R.id.edtNextServiceKm)

        btnPrev = v.findViewById(R.id.btnPrev)
        btnNext = v.findViewById(R.id.btnNext)
    }

    private fun setupServiceTypeDropdown() {
        val types = listOf("General Service", "Major Service", "Accidental Repair", "Emergency Repair", "Oil Change", "Brake Service", "Other")
        autoServiceType.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, types))
    }

    private fun fixFabInsets(v: View) {
        ViewCompat.setOnApplyWindowInsetsListener(v) { _, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            btnNext.translationY = -bottomInset.toFloat()
            btnPrev.translationY = -bottomInset.toFloat()
            insets
        }
    }

    private fun setupDatePicker() {
        edtLastServiceDate.isFocusable = false
        edtLastServiceDate.setOnClickListener { showDatePicker(edtLastServiceDate) }
    }

    private fun showDatePicker(target: TextInputEditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(),
            { _, y, m, d ->
                target.setText(String.format("%02d-%02d-%04d", d, m + 1, y))
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
    }

    private fun setupNavigation() {
        btnPrev.setOnClickListener { (activity as? VehicleRegistrationActivity)?.prevStep() }

        btnNext.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener
            saveServiceRecordAndFullVehicle()
        }
    }

    private fun prefillIfAvailable() {
        // If ViewModel already has last service info, prefill UI
        vm.lastServiceDate?.let { edtLastServiceDate.setText(it) }
        vm.lastServiceOdometer?.let { edtLastServiceOdometer.setText(it.toString()) }
        vm.lastServiceWorkshop?.let { edtWorkshop.setText(it) }
        vm.nextServiceDueKm?.let { edtNextServiceKm.setText(it.toString()) }
    }

    // simple validation: allow skip if all empty
    private fun validateInputs(): Boolean {
        val allEmpty = autoServiceType.text.isNullOrBlank() &&
                edtLastServiceDate.text.isNullOrBlank() &&
                edtLastServiceOdometer.text.isNullOrBlank() &&
                edtWorkshop.text.isNullOrBlank() &&
                edtNextServiceKm.text.isNullOrBlank()

        if (allEmpty) {
            // user chose to skip service entry — still perform final save with other data
            return true
        }

        if (autoServiceType.text.isNullOrBlank()) {
            autoServiceType.error = "Select service type"
            return false
        }

        if (edtLastServiceDate.text.isNullOrBlank()) {
            edtLastServiceDate.error = "Required"
            return false
        }

        val odo = edtLastServiceOdometer.text.toString().trim().toDoubleOrNull()
        if (odo == null) {
            edtLastServiceOdometer.error = "Enter valid KM"
            return false
        }

        if (edtWorkshop.text.isNullOrBlank()) {
            edtWorkshop.error = "Required"
            return false
        }

        // next service KM is optional
        return true
    }

    private fun saveServiceRecordAndFullVehicle() {

        val vehicleId = vm.vehicleNumber.value?.trim()
        if (vehicleId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Vehicle number missing", Toast.LENGTH_LONG).show()
            return
        }

        // Build ServiceRecord (mapping as agreed)
        val record = ServiceRecord(
            serviceType = autoServiceType.text.toString().trim(),
            serviceCost = 0.0, // not present in UI, default 0.0
            serviceDate = edtLastServiceDate.text.toString().trim().ifEmpty { "" },
            odometer_km = edtLastServiceOdometer.text.toString().trim().toDoubleOrNull() ?: 0.0,
            notes = edtWorkshop.text.toString().trim().ifEmpty { "" },
            createdAt = Timestamp.now()
        )

        val dialog = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setMessage("Saving vehicle…")
            .create()
        dialog.show()

        val db = FirebaseFirestore.getInstance()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1) Merge basic info / photos / compliance / documents / service.current
                val basicInfo = hashMapOf<String, Any?>(
                    "vehicle_number" to vm.vehicleNumber.value,
                    "rc_number" to vm.rcNumber.value,
                    "make" to vm.make.value,
                    "model" to vm.model.value,
                    "manufacture_year" to vm.manufactureYear.value,
                    "seating_capacity" to vm.seatingCapacity.value,
                    "odometer_at_registration" to vm.odometerAtRegistration,
                    "chassis_number" to vm.chassis_number.value,
                    "engine_number" to vm.engine_number.value,
                    "fuel_type" to vm.fuelType.value,
                    "transmission" to vm.transmissionType.value,
                    "owner_type" to vm.vehicleOwnership.value,
                    "status" to "ACTIVE"
                )

                // compliance & documents from ViewModel if present
                val complianceObj = vm.compliance.value ?: null
                val docs = vm.complianceDocuments.value ?: emptyMap<String, String>()
                val photos = vm.photos.value ?: emptyMap<String, String>()

                // Build service.current map
                val serviceCurrent = hashMapOf<String, Any?>(
                    "serviceType" to record.serviceType,
                    "serviceCost" to record.serviceCost,
                    "serviceDate" to record.serviceDate,
                    "odometer_km" to record.odometer_km,
                    "notes" to record.notes,
                    "createdAt" to record.createdAt
                )

                // final merged payload (merge mode used below)
                val merged = hashMapOf<String, Any?>(
                    "vehicle_info" to basicInfo,
                    "photos" to photos,
                    "compliance" to complianceObj,
                    "documents" to docs,
                    "service" to hashMapOf(
                        "current" to serviceCurrent
                        // history appended separately
                    ),
                    "updated_at" to Timestamp.now()
                )

                // If document doesn't have created_at yet, set it (read first)
                val docRef = db.collection("vehicles").document(vehicleId)
                val snapshot = docRef.get().await()
                if (!snapshot.exists() || snapshot.get("created_at") == null) {
                    merged["created_at"] = Timestamp.now()
                }

                // 2) write merged payload with merge option
                docRef.set(merged, SetOptions.merge()).await()

                // 3) append service record to history array
                // convert record to a simple map for Firestore arrayUnion
                val recordMap = hashMapOf<String, Any?>(
                    "serviceType" to record.serviceType,
                    "serviceCost" to record.serviceCost,
                    "serviceDate" to record.serviceDate,
                    "odometer_km" to record.odometer_km,
                    "notes" to record.notes,
                    "createdAt" to record.createdAt
                )

                docRef.update("service.history", FieldValue.arrayUnion(recordMap)).await()

                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    AlertDialog.Builder(requireContext())
                        .setTitle("Success")
                        .setMessage("Vehicle saved and service recorded.")
                        .setPositiveButton("OK") { _, _ -> requireActivity().finish() }
                        .show()
                }

            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Save failed: ${ex.message}")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }
}
