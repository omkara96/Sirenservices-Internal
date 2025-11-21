package com.omkara.sirenservices_internal.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.TripModel

class EditTripActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var tripId: String
    private var tripData: TripModel? = null

    // Views
    private lateinit var tvTripNumber: TextView
    private lateinit var tvPickup: TextView
    private lateinit var tvDrop: TextView
    private lateinit var tvVehicle: TextView
    private lateinit var tvDriver: TextView

    private lateinit var statusDropdown: MaterialAutoCompleteTextView
    private lateinit var etFuelCost: TextInputEditText
    private lateinit var etMechanicCost: TextInputEditText
    private lateinit var etServicingCost: TextInputEditText
    private lateinit var etPendingAmount: TextInputEditText
    private lateinit var etHODeposit: TextInputEditText
    private lateinit var etNewStop: TextInputEditText

    private lateinit var stopsList: LinearLayout
    private lateinit var btnAddStop: ImageButton
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_trip)

        db = FirebaseFirestore.getInstance()
        tripId = intent.getStringExtra("trip_id") ?: ""

        initViews()
        loadTrip()
    }

    private fun initViews() {
        tvTripNumber = findViewById(R.id.tvTripNumber)
        tvPickup = findViewById(R.id.tvPickup)
        tvDrop = findViewById(R.id.tvDrop)
        tvVehicle = findViewById(R.id.tvVehicle)
        tvDriver = findViewById(R.id.tvDriver)

        statusDropdown = findViewById(R.id.dropdownTripStatus)
        etFuelCost = findViewById(R.id.etFuelCost)
        etMechanicCost = findViewById(R.id.etMechanicCost)
        etServicingCost = findViewById(R.id.etServicingCost)
        etPendingAmount = findViewById(R.id.etPendingAmount)
        etHODeposit = findViewById(R.id.etHeadOfficeDeposit)
        etNewStop = findViewById(R.id.etAddStop)

        stopsList = findViewById(R.id.layoutStopsContainer)
        btnAddStop = findViewById(R.id.btnAddStop)
        btnSave = findViewById(R.id.btnSaveChanges)

        btnAddStop.setOnClickListener { addStop() }
        btnSave.setOnClickListener { saveChanges() }

       // statusDropdown.setSimpleItems(arrayOf("ASSIGNED", "ONGOING", "COMPLETED"))
        val statusList = listOf("ASSIGNED", "ONGOING", "COMPLETED")
        val adapterStatus = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            statusList
        )
        statusDropdown.setAdapter(adapterStatus)

        statusDropdown.setOnClickListener {
            statusDropdown.showDropDown()
        }
    }

    private fun loadTrip() {
        db.collection("trips").document(tripId)
            .get()
            .addOnSuccessListener { snap ->
                val trip = snap.toObject(TripModel::class.java)
                tripData = trip

                if (trip == null) {
                    Toast.makeText(this, "Trip not found!", Toast.LENGTH_LONG).show()
                    finish()
                    return@addOnSuccessListener
                }

                updateUI(trip)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateUI(t: TripModel) {
        // READ ONLY fields
        tvTripNumber.text = t.trip_number
        tvPickup.text = t.pickup
        tvDrop.text = t.final_drop
        tvVehicle.text = t.vehicle_display
        tvDriver.text = t.driver_display

        // Editable fields
        statusDropdown.setText(t.status, false)
        etFuelCost.setText(t.fuel_cost.toString())
        etMechanicCost.setText(t.mechanic_cost.toString())
        etServicingCost.setText(t.servicing_cost.toString())
        etPendingAmount.setText(t.pending_amount.toString())
        etHODeposit.setText(t.head_office_deposit.toString())

        renderStops(t.intermediate_stops ?: emptyList())

        // Lock if COMPLETED
        if (t.status == "COMPLETED") disableEditing()
    }

    private fun renderStops(stops: List<String>) {
        stopsList.removeAllViews()

        for (stop in stops) {
            val view = layoutInflater.inflate(R.layout.item_stop, stopsList, false)
            val tvStop = view.findViewById<TextView>(R.id.tvStopName)
            val deleteBtn = view.findViewById<ImageButton>(R.id.btnDeleteStop)

            tvStop.text = stop

            deleteBtn.setOnClickListener {
                tripData?.intermediate_stops?.remove(stop)
                renderStops(tripData?.intermediate_stops ?: emptyList())
            }

            stopsList.addView(view)
        }
    }

    private fun addStop() {
        val newStop = etNewStop.text.toString().trim()
        if (newStop.isEmpty()) {
            etNewStop.error = "Stop cannot be empty"
            return
        }

        tripData?.intermediate_stops?.add(newStop)
        etNewStop.text?.clear()
        renderStops(tripData?.intermediate_stops ?: emptyList())
    }

    private fun disableEditing() {
        // Disable inputs
        statusDropdown.isEnabled = false
        etFuelCost.isEnabled = false
        etMechanicCost.isEnabled = false
        etServicingCost.isEnabled = false
        etPendingAmount.isEnabled = false
        etHODeposit.isEnabled = false
        etNewStop.isEnabled = false
        btnAddStop.isEnabled = false

        btnSave.isEnabled = false
        btnSave.text = "Trip Completed"
    }

    private fun saveChanges() {
        val updateMap = HashMap<String, Any?>().apply {
            put("status", statusDropdown.text.toString())
            put("fuel_cost", etFuelCost.text.toString().toDoubleOrNull() ?: 0.0)
            put("mechanic_cost", etMechanicCost.text.toString().toDoubleOrNull() ?: 0.0)
            put("servicing_cost", etServicingCost.text.toString().toDoubleOrNull() ?: 0.0)
            put("pending_amount", etPendingAmount.text.toString().toDoubleOrNull() ?: 0.0)
            put("head_office_deposit", etHODeposit.text.toString().toDoubleOrNull() ?: 0.0)
            put("intermediate_stops", tripData?.intermediate_stops ?: emptyList<String>())
        }



        db.collection("trips").document(tripId)
            .update(updateMap)
            .addOnSuccessListener {
                // Update vehicle ONLY if trip became COMPLETED
                if (statusDropdown.text.toString() == "COMPLETED") {
                    updateVehicleStatusToAvailable(tripData?.vehicle_id)
                }
                Toast.makeText(this, "Trip updated!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun updateVehicleStatusToAvailable(vehicleId: String?) {
        if (vehicleId.isNullOrEmpty()) return

        db.collection("vehicles")
            .document(vehicleId)
            .update("status", "AVAILABLE")
            .addOnSuccessListener {
                Toast.makeText(this, "Vehicle marked as Available", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update vehicle status", Toast.LENGTH_SHORT).show()
            }
    }

}
