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
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.TripModel
import java.text.SimpleDateFormat
import java.util.*

class TripEditActivity : AppCompatActivity() {

    // Views
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
    private lateinit var btnUpdateTrip: MaterialButton

    private val db = FirebaseFirestore.getInstance()
    private var tripId: String? = null
    private var currentTrip: TripModel? = null

    private val vehicleMap = linkedMapOf<String, String>()
    private val driverMap = linkedMapOf<String, String>()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_edit)

        tripId = intent.getStringExtra("TRIP_ID")
        if (tripId == null) {
            Toast.makeText(this, "Trip ID is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupToolbar()
        setupPaymentMode()
        setupCallSource()
        setupAddStopButton()
        setupDropdownClicks()

        loadDrivers()
        loadAvailableVehicles()
        loadTripDetails()
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
        btnUpdateTrip = findViewById(R.id.btnUpdateTrip)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupPaymentMode() {
        val modes = listOf("Online", "Cash")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, modes)
        autoPaymentMode.setAdapter(adapter)
    }

    private fun setupCallSource() {
        val sources = listOf("Direct", "Third Party")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, sources)
        autoCallSource.setAdapter(adapter)
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
    }

    private fun loadAvailableVehicles() {
        db.collection("vehicles")
            .whereEqualTo("status", "ACTIVE")
            .get()
            .addOnSuccessListener { snap ->
                val list = mutableListOf<String>()
                vehicleMap.clear()
                for (v in snap.documents) {
                    val vid = v.id
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
    }

    private fun loadTripDetails() {
        val loading = ProgressDialog(this)
        loading.setMessage("Loading trip details...")
        loading.show()

        db.collection("trips").document(tripId!!).get()
            .addOnSuccessListener { doc ->
                loading.dismiss()
                if (doc.exists()) {
                    currentTrip = doc.toObject(TripModel::class.java)
                    populateUI()
                } else {
                    Toast.makeText(this, "Trip not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                loading.dismiss()
                Toast.makeText(this, "Failed to load trip: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun populateUI() {
        currentTrip?.let { trip ->
            tvTripNumber.text = "Trip: ${trip.trip_number}"
            edtTripDate.setText(trip.trip_date)
            // Pre-fill other fields

            if (trip.status == "COMPLETED") {
                disableEditing()
            }
        }
    }

    private fun disableEditing() {
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        for (i in 0 until rootLayout.childCount) {
            val view = rootLayout.getChildAt(i)
            view.isEnabled = false
        }
        btnUpdateTrip.visibility = View.GONE
        Toast.makeText(this, "This trip is completed and cannot be edited.", Toast.LENGTH_LONG).show()
    }

    fun onUpdateTripClicked(view: View?) {
        if (currentTrip?.status == "COMPLETED") {
            Toast.makeText(this, "Cannot update a completed trip.", Toast.LENGTH_SHORT).show()
            return
        }

        // Update logic here, similar to createTrip()
    }
}
