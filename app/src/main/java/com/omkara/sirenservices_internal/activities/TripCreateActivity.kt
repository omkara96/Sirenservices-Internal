package com.omkara.sirenservices_internal.activities

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import java.text.SimpleDateFormat
import java.util.*

class TripCreateActivity : AppCompatActivity() {

    // Views (IDs match your activity_trip_create.xml)
    private lateinit var tvTripNumber: TextView
    private lateinit var edtTripDate: TextInputEditText
    private lateinit var autoVehicle: AutoCompleteTextView
    private lateinit var autoDriver: AutoCompleteTextView
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

    private val db = FirebaseFirestore.getInstance()

    // maps used for adapters: display -> id
    private val vehicleMap = linkedMapOf<String, String>()
    private val driverMap = linkedMapOf<String, String>()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_create)

        initViews()
        setupToolbar()
        generateTripNumberAndDefaultDate()
        setupPaymentMode()
        setupCallSource()
        setupAddStopButton()
        setupDropdownClicks()

        loadDrivers()
        loadAvailableVehicles()
    }

    private fun initViews() {
        tvTripNumber = findViewById(R.id.tvTripNumber)
        edtTripDate = findViewById(R.id.edtTripDate)
        autoVehicle = findViewById(R.id.autoVehicle)
        autoDriver = findViewById(R.id.autoDriver)
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
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun generateTripNumberAndDefaultDate() {
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
        val tripNum = "TRIP-$stamp"
        tvTripNumber.text = "Trip: $tripNum"
        tvTripNumber.tag = tripNum
        edtTripDate.setText(dateFormat.format(Date()))
        // date picker
        edtTripDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this,
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

    private fun setupPaymentMode() {
        val modes = listOf("Online", "Cash")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, modes)
        autoPaymentMode.setAdapter(adapter)
        autoPaymentMode.setOnClickListener { autoPaymentMode.showDropDown() }
    }

    private fun setupCallSource() {
        val sources = listOf("Direct", "Third Party")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, sources)
        autoCallSource.setAdapter(adapter)
        autoCallSource.setOnClickListener { autoCallSource.showDropDown() }
    }

    private fun setupAddStopButton() {
        btnAddStop.setOnClickListener { addStopRow(null) }
    }

    private fun addStopRow(prefill: String?) {
        val et = EditText(this)
        et.hint = "Intermediate stop"
        et.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 8 }
        if (!prefill.isNullOrEmpty()) et.setText(prefill)

        // container row with remove button
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.weight = 1f
        row.addView(et, lp)

        val btnRemove = Button(this)
        btnRemove.text = "Remove"
        btnRemove.setOnClickListener { llStopsContainer.removeView(row) }
        row.addView(btnRemove, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        llStopsContainer.addView(row)
    }

    private fun setupDropdownClicks() {
        autoVehicle.setOnClickListener { autoVehicle.showDropDown() }
        autoDriver.setOnClickListener { autoDriver.showDropDown() }
    }

    private fun loadDrivers() {
        // adjust collection/field names as per your DB (uses firstName+lastName)
        db.collection("users")
            .whereEqualTo("role", "Driver")
            .whereEqualTo("status", "Active")
            .get()
            .addOnSuccessListener { snap ->
                val list = mutableListOf<String>()
                driverMap.clear()
                for (d in snap.documents) {
                    val id = d.id
                    val name = (d.getString("firstName") ?: "") + " " + (d.getString("lastName") ?: "")
                    val display = name.trim().ifEmpty { id }
                    list.add(display)
                    driverMap[display] = id
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
                autoDriver.setAdapter(adapter)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load drivers: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadAvailableVehicles() {
        // 1) get all vehicles with status == ACTIVE
        db.collection("vehicles")
            .whereEqualTo("status", "ACTIVE")
            .get()
            .addOnSuccessListener { snap ->
                val list = mutableListOf<String>()
                vehicleMap.clear()
                for (v in snap.documents) {
                    val vid = v.id
                    // skip if vehicle already occupied (check current_trip_id)
                    val curr = v.getString("current_trip_id")
                    if (!curr.isNullOrEmpty()) continue
                    val number = v.getString("vehicle_number") ?: v.getString("vehicleNumber") ?: vid
                    val display = "$number (${v.getString("make") ?: ""} ${v.getString("model") ?: ""})".trim()
                    list.add(display)
                    vehicleMap[display] = vid
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
                autoVehicle.setAdapter(adapter)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load vehicles: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun collectStops(): List<String> {
        val stops = mutableListOf<String>()
        for (i in 0 until llStopsContainer.childCount) {
            val row = llStopsContainer.getChildAt(i) as LinearLayout
            val et = row.getChildAt(0) as EditText
            val t = et.text.toString().trim()
            if (t.isNotEmpty()) stops.add(t)
        }
        return stops
    }

    private fun validateInputs(): Boolean {
        if (autoVehicle.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Select vehicle", Toast.LENGTH_SHORT).show()
            return false
        }
        if (autoDriver.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Select driver", Toast.LENGTH_SHORT).show()
            return false
        }
        if (edtPickup.text.toString().trim().isEmpty()) {
            edtPickup.error = "Enter pickup"
            return false
        }
        if (edtFinalDrop.text.toString().trim().isEmpty()) {
            edtFinalDrop.error = "Enter final drop"
            return false
        }
        if (edtTripCost.text.toString().trim().isEmpty()) {
            edtTripCost.error = "Enter trip cost"
            return false
        }
        return true
    }

    // called by create button
    fun onCreateTripClicked(view: View?) {
        createTrip()
    }

    private fun createTrip() {
        if (!validateInputs()) return

        val tripNumber = tvTripNumber.tag as? String ?: tvTripNumber.text.toString()
        val tripDate = edtTripDate.text.toString().trim()
        val vehicleDisplay = autoVehicle.text.toString().trim()
        val driverDisplay = autoDriver.text.toString().trim()

        val vehicleId = vehicleMap[vehicleDisplay]
        val driverId = driverMap[driverDisplay]

        if (vehicleId == null) {
            Toast.makeText(this, "Select a valid vehicle", Toast.LENGTH_SHORT).show()
            return
        }
        if (driverId == null) {
            Toast.makeText(this, "Select a valid driver", Toast.LENGTH_SHORT).show()
            return
        }

        val pickup = edtPickup.text.toString().trim()
        val stops = collectStops()
        val finalDrop = edtFinalDrop.text.toString().trim()
        val tripCost = edtTripCost.text.toString().toDoubleOrNull() ?: 0.0
        val paymentMode = autoPaymentMode.text.toString().trim()
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

        // prepare maps
        val tripRef = db.collection("trips").document()
        val billRef = db.collection("bills").document()
        val vehicleRef = db.collection("vehicles").document(vehicleId)

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
            "created_by" to (/* FirebaseAuth.getInstance().uid ?: */ "system"),
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

        val loading = ProgressDialog(this)
        loading.setCancelable(false)
        loading.setMessage("Creating trip...")
        loading.show()

        // Transaction: read vehicle doc and update only vehicle doc and create trip & bill
        db.runTransaction { tx ->
            val vSnap = tx.get(vehicleRef)
            if (!vSnap.exists()) throw Exception("Vehicle not found")

            val status = vSnap.getString("status") ?: "ACTIVE"
            val currTrip = vSnap.getString("current_trip_id")
            if (status != "ACTIVE") throw Exception("Vehicle not available (status=$status)")
            if (!currTrip.isNullOrEmpty()) throw Exception("Vehicle already assigned to another trip")

            // create trip + bill
            tx.set(tripRef, tripMap)
            tx.set(billRef, billMap)

            // mark vehicle occupied
            tx.update(vehicleRef, mapOf(
                "status" to "OCCUPIED",
                "current_trip_id" to tripRef.id,
                "updated_at" to FieldValue.serverTimestamp()
            ))

            null
        }.addOnSuccessListener {
            loading.dismiss()
            Toast.makeText(this, "Trip created successfully", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            loading.dismiss()
            Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // wire the XML button's onClick to this OR set an onclick listener in onCreate
    override fun onResume() {
        super.onResume()
        // reload lists (in case something changed)
        loadDrivers()
        loadAvailableVehicles()
    }
}
