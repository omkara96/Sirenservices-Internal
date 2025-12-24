package com.omkara.sirenservices_internal.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.TripModel

class EditTripActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var tripId: String
    private lateinit var tripData: TripModel

    /* -------- READ ONLY -------- */
    private lateinit var tvTripNumber: TextView
    private lateinit var tvPickup: TextView
    private lateinit var tvDrop: TextView
    private lateinit var tvVehicle: TextView
    private lateinit var tvDriver: TextView

    /* -------- EDITABLE -------- */
    private lateinit var dropdownTripStatus: MaterialAutoCompleteTextView

    private lateinit var etStartOdometer: TextInputEditText
    private lateinit var etEndOdometer: TextInputEditText
    private lateinit var etFuelLiters: TextInputEditText

    private lateinit var etPatientName: TextInputEditText
    private lateinit var etPatientContact: TextInputEditText
    private lateinit var etCallSource: TextInputEditText

    private lateinit var etMechanicName: TextInputEditText
    private lateinit var etServicingKm: TextInputEditText
    private lateinit var etMechanicCost: TextInputEditText

    private lateinit var etFuelCost: TextInputEditText
    private lateinit var etServicingCost: TextInputEditText
    private lateinit var etPendingAmount: TextInputEditText
    private lateinit var etHeadOfficeDeposit: TextInputEditText

    private lateinit var layoutStops: LinearLayout
    private lateinit var etAddStop: TextInputEditText
    private lateinit var btnAddStop: ImageButton
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_trip)

        tripId = intent.getStringExtra("trip_id") ?: run {
            finish(); return
        }

        bindViews()
        setupDropdown()
        setupActions()
        loadTrip()
    }

    /* ================= INIT ================= */

    private fun bindViews() {
        tvTripNumber = findViewById(R.id.tvTripNumber)
        tvPickup = findViewById(R.id.tvPickup)
        tvDrop = findViewById(R.id.tvDrop)
        tvVehicle = findViewById(R.id.tvVehicle)
        tvDriver = findViewById(R.id.tvDriver)

        dropdownTripStatus = findViewById(R.id.dropdownTripStatus)

        etStartOdometer = findViewById(R.id.etStartOdometer)
        etEndOdometer = findViewById(R.id.etEndOdometer)
        etFuelLiters = findViewById(R.id.etFuelConsumed)

        etPatientName = findViewById(R.id.etPatientName)
        etPatientContact = findViewById(R.id.etPatientContact)
        etCallSource = findViewById(R.id.etCallSource)

        etMechanicName = findViewById(R.id.etMechanicName)
        etServicingKm = findViewById(R.id.etServicingKm)
        etMechanicCost = findViewById(R.id.etMechanicCost)

        etFuelCost = findViewById(R.id.etFuelCost)
        etServicingCost = findViewById(R.id.etServicingCost)
        etPendingAmount = findViewById(R.id.etPendingAmount)
        etHeadOfficeDeposit = findViewById(R.id.etHeadOfficeDeposit)

        layoutStops = findViewById(R.id.layoutStopsContainer)
        etAddStop = findViewById(R.id.etAddStop)
        btnAddStop = findViewById(R.id.btnAddStop)
        btnSave = findViewById(R.id.btnSaveChanges)
    }

    private fun setupDropdown() {
        val statuses = listOf("ASSIGNED", "ONGOING", "COMPLETED")
        dropdownTripStatus.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, statuses)
        )
    }

    private fun setupActions() {
        btnAddStop.setOnClickListener { addStop() }
        btnSave.setOnClickListener { saveTrip() }
    }

    /* ================= LOAD ================= */

    private fun loadTrip() {
        db.collection("trips").document(tripId).get()
            .addOnSuccessListener {
                val trip = it.toObject(TripModel::class.java)
                if (trip == null) {
                    Toast.makeText(this, "Trip not found", Toast.LENGTH_LONG).show()
                    finish(); return@addOnSuccessListener
                }
                tripData = trip
                populateUI()
            }
    }

    private fun populateUI() {

        tvTripNumber.text = tripData.trip_number
        tvPickup.text = tripData.pickup ?: ""
        tvDrop.text = tripData.final_drop ?: ""
        tvVehicle.text = tripData.vehicle_display ?: ""
        tvDriver.text = tripData.driver_display ?: ""

        dropdownTripStatus.setText(tripData.status, false)

        etStartOdometer.setText((tripData.start_odometer ?: 0).toString())

        val endOdo = tripData.end_odometer ?: 0
        etEndOdometer.setText(if (endOdo > 0) endOdo.toString() else "")

        etFuelLiters.setText(tripData.fuel_liters.toString())

        etPatientName.setText(tripData.patient_name ?: "")
        etPatientContact.setText(tripData.patient_number ?: "")
        etCallSource.setText(tripData.call_source ?: "")

        etMechanicName.setText(tripData.mechanic_name ?: "")
        etServicingKm.setText((tripData.servicing_km ?: 0).toString())
        etMechanicCost.setText(tripData.mechanic_cost.toString())

        etFuelCost.setText(tripData.fuel_cost.toString())
        etServicingCost.setText(tripData.servicing_cost.toString())
        etPendingAmount.setText(tripData.pending_amount.toString())
        etHeadOfficeDeposit.setText(tripData.head_office_deposit.toString())

        renderStops(tripData.intermediate_stops)

        if (tripData.status == "COMPLETED") disableEditing()
    }

    /* ================= STOPS ================= */

    private fun renderStops(stops: List<String>) {
        layoutStops.removeAllViews()
        if (stops.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No intermediate stops"
            layoutStops.addView(tv)
            return
        }
        stops.forEach {
            val tv = TextView(this)
            tv.text = "• $it"
            layoutStops.addView(tv)
        }
    }

    private fun addStop() {
        val stop = etAddStop.text.toString().trim()
        if (stop.isEmpty()) return
        tripData.intermediate_stops.add(stop)
        etAddStop.text?.clear()
        renderStops(tripData.intermediate_stops)
    }

    /* ================= SAVE ================= */

    private fun saveTrip() {

        val status = dropdownTripStatus.text.toString()
        val startOdo = tripData.start_odometer ?: 0
        val endOdo = etEndOdometer.text.toString().toIntOrNull()

        if (status == "COMPLETED") {
            if (endOdo == null || endOdo <= startOdo) {
                etEndOdometer.error = "End odometer must be greater than start"
                return
            }
        }

        val updateMap = mutableMapOf<String, Any>(
            "status" to status,
            "end_odometer" to (endOdo ?: 0),
            "fuel_liters" to (etFuelLiters.text.toString().toDoubleOrNull() ?: 0.0),
            "patient_name" to etPatientName.text.toString(),
            "patient_number" to etPatientContact.text.toString(),
            "call_source" to etCallSource.text.toString(),
            "mechanic_name" to etMechanicName.text.toString(),
            "servicing_km" to (etServicingKm.text.toString().toIntOrNull() ?: 0),
            "mechanic_cost" to (etMechanicCost.text.toString().toDoubleOrNull() ?: 0.0),
            "fuel_cost" to (etFuelCost.text.toString().toDoubleOrNull() ?: 0.0),
            "servicing_cost" to (etServicingCost.text.toString().toDoubleOrNull() ?: 0.0),
            "pending_amount" to (etPendingAmount.text.toString().toDoubleOrNull() ?: 0.0),
            "head_office_deposit" to (etHeadOfficeDeposit.text.toString().toDoubleOrNull() ?: 0.0),
            "intermediate_stops" to tripData.intermediate_stops
        )

        db.collection("trips").document(tripId).update(updateMap)
            .addOnSuccessListener {
                if (status == "COMPLETED") updateVehicleStatus(tripData.vehicle_id, tripData.driver_id)
                Toast.makeText(this, "Trip updated", Toast.LENGTH_LONG).show()

                finish()
            }
    }

    private fun updateVehicleStatus(vehicleId: String?, tripDriverId: String?) {
        if (vehicleId == null) return
        db.collection("vehicles").document(vehicleId).update("status", "ACTIVE")
        db.collection("vehicles").document(vehicleId).update("current_trip_id", null)
        if (tripDriverId != null) {
            db.collection("users").document(tripDriverId).update("status", "active")
            db.collection("users").document(tripDriverId).update("current_trip_id", null)
        }
    }

    private fun disableEditing() {
        btnSave.isEnabled = false
        btnSave.text = "Trip Completed"
    }
}
