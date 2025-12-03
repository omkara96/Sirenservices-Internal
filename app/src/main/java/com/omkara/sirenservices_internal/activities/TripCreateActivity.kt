package com.omkara.sirenservices_internal.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.VehicleDriverSearchListAdapter
import java.text.SimpleDateFormat
import java.util.*

class TripCreateActivity : AppCompatActivity() {

    // Views
    private lateinit var tvTripNumber: TextView
    private lateinit var edtTripDate: TextInputEditText
    private lateinit var autoVehicle: AutoCompleteTextView
    private lateinit var autoDriver: AutoCompleteTextView

    private lateinit var edtStartOdometer: TextInputEditText
    private lateinit var edtEndOdometer: TextInputEditText

    private lateinit var edtPickup: TextInputEditText
    private lateinit var llStopsContainer: LinearLayout
    private lateinit var btnAddStop: Button
    private lateinit var edtFinalDrop: TextInputEditText

    private lateinit var edtTripCost: TextInputEditText
    private lateinit var autoPaymentMode: AutoCompleteTextView
    private lateinit var edtPendingAmount: TextInputEditText
    private lateinit var edtPendingWith: TextInputEditText

    private lateinit var edtFuelCost: TextInputEditText
    private lateinit var edtFuelLiters: TextInputEditText
    private lateinit var edtServicingCost: TextInputEditText
    private lateinit var edtServicingKm: TextInputEditText
    private lateinit var edtMechanicName: TextInputEditText
    private lateinit var edtMechanicCost: TextInputEditText

    private lateinit var edtPatientName: TextInputEditText
    private lateinit var edtPatientNumber: TextInputEditText
    private lateinit var autoCallSource: AutoCompleteTextView
    private lateinit var edtHeadOfficeDeposit: TextInputEditText

    private lateinit var btnCreateTrip: MaterialButton
    private lateinit var loadingOverlay: View

    // Firebase
    private val db = FirebaseFirestore.getInstance()

    // display → ID maps
    private val vehicleMap = linkedMapOf<String, String>()
    private val driverMap = linkedMapOf<String, String>()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_create)

        initViews()
        setupToolbar()
        generateTripNumberAndDefaultDate()
        setupDropdowns()
        setupAddStopButton()

        loadDrivers()
        loadAvailableVehicles()
    }

    // -----------------------------------------
    // INIT VIEWS
    // -----------------------------------------

    private fun initViews() {
        tvTripNumber = findViewById(R.id.tvTripNumber)
        edtTripDate = findViewById(R.id.edtTripDate)

        autoVehicle = findViewById(R.id.autoVehicle)
        autoDriver = findViewById(R.id.autoDriver)

        edtStartOdometer = findViewById(R.id.edtStartOdometer)
        edtEndOdometer = findViewById(R.id.edtEndOdometer)

        edtPickup = findViewById(R.id.edtPickup)
        llStopsContainer = findViewById(R.id.llStopsContainer)
        btnAddStop = findViewById(R.id.btnAddStop)
        edtFinalDrop = findViewById(R.id.edtFinalDrop)

        edtTripCost = findViewById(R.id.edtTripCost)
        autoPaymentMode = findViewById(R.id.autoPaymentMode)
        edtPendingAmount = findViewById(R.id.edtPendingAmount)
        edtPendingWith = findViewById(R.id.edtPendingWith)

        edtFuelCost = findViewById(R.id.edtFuelCost)
        edtFuelLiters = findViewById(R.id.edtFuelLiters)
        edtServicingCost = findViewById(R.id.edtServicingCost)
        edtServicingKm = findViewById(R.id.edtServicingKm)
        edtMechanicName = findViewById(R.id.edtMechanicName)
        edtMechanicCost = findViewById(R.id.edtMechanicCost)

        edtPatientName = findViewById(R.id.edtPatientName)
        edtPatientNumber = findViewById(R.id.edtPatientNumber)
        autoCallSource = findViewById(R.id.autoCallSource)
        edtHeadOfficeDeposit = findViewById(R.id.edtHeadOfficeDeposit)

        btnCreateTrip = findViewById(R.id.btnCreateTrip)
        loadingOverlay = findViewById(R.id.loadingOverlay)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    // -----------------------------------------
    // GENERATE DATE + TRIP NUMBER
    // -----------------------------------------

    private fun generateTripNumberAndDefaultDate() {
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
        val tripNum = "TRIP-$stamp"
        tvTripNumber.text = "Trip: $tripNum"
        tvTripNumber.tag = tripNum

        edtTripDate.setText(dateFormat.format(Date()))
        edtTripDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    val cal = Calendar.getInstance()
                    cal.set(y, m, d)
                    edtTripDate.setText(dateFormat.format(cal.time))
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    // -----------------------------------------
    // DROPDOWNS SETUP
    // -----------------------------------------

    private fun setupDropdowns() {
        autoPaymentMode.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, listOf("Online", "Cash"))
        )
        autoPaymentMode.setOnClickListener { autoPaymentMode.showDropDown() }

        autoCallSource.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, listOf("Direct", "Third Party"))
        )
        autoCallSource.setOnClickListener { autoCallSource.showDropDown() }

        autoVehicle.setOnClickListener {
            if (vehicleMap.isNotEmpty()) {
                openSearchableSelector("Select Vehicle", vehicleMap.keys.toList()) { selected ->
                    autoVehicle.setText(selected)
                }
            }
        }

        autoDriver.setOnClickListener {
            if (driverMap.isNotEmpty()) {
                openSearchableSelector("Select Driver", driverMap.keys.toList()) { selected ->
                    autoDriver.setText(selected)
                }
            }
        }

    }

    // -----------------------------------------
    // LOAD DROPDOWN DATA
    // -----------------------------------------

    private fun loadDrivers() {
        db.collection("users")
            .whereEqualTo("role", "driver")
            .whereEqualTo("status", "active")
            .get()
            .addOnSuccessListener { snap ->
                val list = mutableListOf<String>()
                driverMap.clear()

                for (doc in snap.documents) {
                    val id = doc.id
                    val name = "${doc.getString("firstName") ?: ""} ${doc.getString("lastName") ?: ""}".trim()
                    driverMap[name] = id
                    list.add(name)
                }

                autoDriver.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, list))
            }
    }

    private fun loadAvailableVehicles() {
        db.collection("vehicles")
            .get()
            .addOnSuccessListener { snap ->
                val list = mutableListOf<String>()
                vehicleMap.clear()

                for (doc in snap.documents) {
                    val id = doc.id

                    // vehicle_details is nested
                    val details = doc.get("vehicle_info") as? Map<*, *> ?: continue

                    val status = details["status"] as? String ?: "INACTIVE"
                    if (status != "ACTIVE") continue   // skip inactive vehicles

                    // check current trip
                    val curr = doc.getString("current_trip_id")
                    if (!curr.isNullOrEmpty()) continue  // skip occupied vehicles

                    val number = details["vehicle_number"] as? String ?: id
                    val make = details["make"] as? String ?: ""
                    val model = details["model"] as? String ?: ""

                    val display = "$number ($make $model)".trim()

                    vehicleMap[display] = id
                    list.add(display)
                }

                autoVehicle.setAdapter(
                    ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
                )
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load vehicles: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    // -----------------------------------------
    // INTERMEDIATE STOPS
    // -----------------------------------------

    private fun setupAddStopButton() {
        btnAddStop.setOnClickListener { addStopRow(null) }
    }

    private fun addStopRow(prefill: String?) {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val et = EditText(this)
        et.hint = "Intermediate stop"
        et.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        if (!prefill.isNullOrEmpty()) et.setText(prefill)
        row.addView(et)

        val btnRemove = Button(this)
        btnRemove.text = "Remove"
        btnRemove.setOnClickListener { llStopsContainer.removeView(row) }
        row.addView(btnRemove)

        llStopsContainer.addView(row)
    }

    private fun collectStops(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until llStopsContainer.childCount) {
            val row = llStopsContainer.getChildAt(i) as LinearLayout
            val et = row.getChildAt(0) as EditText
            val t = et.text.toString().trim()
            if (t.isNotEmpty()) list.add(t)
        }
        return list
    }

    // -----------------------------------------
    // VALIDATION
    // -----------------------------------------

    private fun validateInputs(): Boolean {
        if (autoVehicle.text.isNullOrEmpty()) {
            Toast.makeText(this, "Select a vehicle", Toast.LENGTH_SHORT).show()
            return false
        }
        if (autoDriver.text.isNullOrEmpty()) {
            Toast.makeText(this, "Select a driver", Toast.LENGTH_SHORT).show()
            return false
        }
        if (edtPickup.text.isNullOrEmpty()) {
            edtPickup.error = "Enter pickup"
            return false
        }
        if (edtFinalDrop.text.isNullOrEmpty()) {
            edtFinalDrop.error = "Enter final drop"
            return false
        }
        if (edtTripCost.text.isNullOrEmpty()) {
            edtTripCost.error = "Enter trip cost"
            return false
        }

        if (edtStartOdometer.text.isNullOrEmpty()) {
            edtStartOdometer.error = "Start odometer is required"
            return false
        }

        return true
    }

    // -----------------------------------------
    // CREATE TRIP CLICK
    // -----------------------------------------

    fun onCreateTripClicked(view: View?) {
        createTrip()
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    // -----------------------------------------
    // FIREBASE LOGIC
    // -----------------------------------------

    private fun createTrip() {
        if (!validateInputs()) return

        showLoading(true)

        val tripNumber = tvTripNumber.tag as String
        val tripDate = edtTripDate.text.toString().trim()

        val vehicleDisplay = autoVehicle.text.toString().trim()
        val driverDisplay = autoDriver.text.toString().trim()

        val vehicleId = vehicleMap[vehicleDisplay]!!
        val driverId = driverMap[driverDisplay]!!

        val pickup = edtPickup.text.toString().trim()
        val stops = collectStops()
        val finalDrop = edtFinalDrop.text.toString().trim()

        val startOdo = edtStartOdometer.text.toString().toIntOrNull()
        val endOdo = edtEndOdometer.text.toString().toIntOrNull()

        val tripCost = edtTripCost.text.toString().toDouble()
        val paymentMode = autoPaymentMode.text.toString()
        val pendingAmount = edtPendingAmount.text.toString().toDoubleOrNull() ?: 0.0
        val pendingWith = edtPendingWith.text.toString().trim()

        val fuelCost = edtFuelCost.text.toString().toDoubleOrNull() ?: 0.0
        val fuelLiters = edtFuelLiters.text.toString().toDoubleOrNull() ?: 0.0

        val servicingCost = edtServicingCost.text.toString().toDoubleOrNull() ?: 0.0
        val servicingKm = edtServicingKm.text.toString().toIntOrNull()

        val mechanicName = edtMechanicName.text.toString().trim()
        val mechanicCost = edtMechanicCost.text.toString().toDoubleOrNull() ?: 0.0

        val patientName = edtPatientName.text.toString().trim()
        val patientNumber = edtPatientNumber.text.toString().trim()
        val callSource = autoCallSource.text.toString().trim()

        val headOfficeDeposit = edtHeadOfficeDeposit.text.toString().toDoubleOrNull() ?: 0.0

        // FIRESTORE REFS
        val tripRef = db.collection("trips").document()
        val billRef = db.collection("bills").document()
        val vehicleRef = db.collection("vehicles").document(vehicleId)

        // BUILD TRIP MAP
        val tripMap = hashMapOf<String, Any?>(
            "trip_number" to tripNumber,
            "trip_date" to tripDate,
            "vehicle_id" to vehicleId,
            "vehicle_display" to vehicleDisplay,
            "driver_id" to driverId,
            "driver_display" to driverDisplay,

            "pickup" to pickup,
            "intermediate_stops" to stops,
            "final_drop" to finalDrop,

            "start_odometer" to startOdo,
            "end_odometer" to endOdo,

            "trip_cost" to tripCost,
            "payment_mode" to paymentMode,
            "pending_amount" to pendingAmount,
            "pending_with" to pendingWith,

            "fuel_cost" to fuelCost,
            "fuel_liters" to fuelLiters,
            "servicing_cost" to servicingCost,
            "servicing_km" to servicingKm,
            "mechanic_name" to mechanicName,
            "mechanic_cost" to mechanicCost,

            "patient_name" to patientName,
            "patient_number" to patientNumber,
            "call_source" to callSource,

            "head_office_deposit" to headOfficeDeposit,
            "created_by" to "system",
            "created_at" to FieldValue.serverTimestamp(),
            "status" to "ASSIGNED",

            "bill_id" to billRef.id
        )

        val billMap = mapOf(
            "trip_id" to tripRef.id,
            "trip_number" to tripNumber,
            "total_amount" to tripCost,
            "status" to "DRAFT",
            "created_at" to FieldValue.serverTimestamp()
        )

        // RUN TRANSACTION
        db.runTransaction { tx ->
            val vehicleSnap = tx.get(vehicleRef)

            if (!vehicleSnap.exists()) throw Exception("Vehicle not found")

            val status = vehicleSnap.getString("status") ?: "ACTIVE"
            val currTrip = vehicleSnap.getString("current_trip_id")

            if (status != "ACTIVE") throw Exception("Vehicle not available (status=$status)")
            if (!currTrip.isNullOrEmpty()) throw Exception("Vehicle already in trip")

            // CREATE TRIP + BILL
            tx.set(tripRef, tripMap)
            tx.set(billRef, billMap)

            // UPDATE VEHICLE
            tx.update(vehicleRef, mapOf(
                "status" to "OCCUPIED",
                "current_trip_id" to tripRef.id,
                "updated_at" to FieldValue.serverTimestamp(),
                "last_odometer" to startOdo
            ))

            null
        }.addOnSuccessListener {
            showLoading(false)
            Toast.makeText(this, "Trip created successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            showLoading(false)
            Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDrivers()
        loadAvailableVehicles()
    }

    private fun openSearchableSelector(
        title: String,
        list: List<String>,
        onSelect: (String) -> Unit
    ) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_searchable_list, null)
        dialog.setContentView(view)

        val edtSearch = view.findViewById<EditText>(R.id.edtSearch)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerList)

        val adapter = VehicleDriverSearchListAdapter(list) { selected ->
            onSelect(selected)
            dialog.dismiss()
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        dialog.show()
    }


}
