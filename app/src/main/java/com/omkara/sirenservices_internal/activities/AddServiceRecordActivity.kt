package com.omkara.sirenservices_internal.activities

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import java.util.*

class AddServiceRecordActivity : AppCompatActivity() {

    private lateinit var autoServiceType: AutoCompleteTextView
    private lateinit var edtServiceCost: TextInputEditText
    private lateinit var edtOdometer: TextInputEditText
    private lateinit var edtServiceDate: TextInputEditText
    private lateinit var edtNotes: TextInputEditText
    private lateinit var btnSave: MaterialButton

    private lateinit var vehicleId: String
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service_record)

        vehicleId = intent.getStringExtra("vehicle_id") ?: ""

        initViews()
        setupToolbar()
        setupDropdown()
        setupDatePicker()

        btnSave.setOnClickListener { saveServiceRecord() }
    }

    private fun initViews() {
        autoServiceType = findViewById(R.id.autoServiceType)
        edtServiceCost = findViewById(R.id.edtServiceCost)
        edtOdometer = findViewById(R.id.edtOdometer)
        edtServiceDate = findViewById(R.id.edtServiceDate)
        edtNotes = findViewById(R.id.edtNotes)
        btnSave = findViewById(R.id.btnSaveService)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupDropdown() {
        val serviceTypes = listOf(
            "General Service",
            "Engine Oil Change",
            "Full Maintenance",
            "Brake Service",
            "Tyre Replacement",
            "Battery Replacement",
            "Wheel Alignment / Balancing",
            "Clutch Repair",
            "Insurance Repair",
            "Accidental Repair",
            "Other"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, serviceTypes)
        autoServiceType.setAdapter(adapter)
        autoServiceType.setOnClickListener { autoServiceType.showDropDown() }
    }

    private fun setupDatePicker() {
        edtServiceDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    edtServiceDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun saveServiceRecord() {
        // Clear previous errors
        autoServiceType.error = null
        edtServiceCost.error = null
        edtOdometer.error = null
        edtServiceDate.error = null

        val serviceType = autoServiceType.text.toString().trim()
        val costStr = edtServiceCost.text.toString().trim()
        val odoStr = edtOdometer.text.toString().trim()
        val serviceDate = edtServiceDate.text.toString().trim()
        val notes = edtNotes.text.toString().trim()

        if (serviceType.isEmpty()) {
            autoServiceType.error = "Select service type"
            autoServiceType.requestFocus()
            return
        }
        if (costStr.isEmpty()) {
            edtServiceCost.error = "Enter service cost"
            edtServiceCost.requestFocus()
            return
        }
        val serviceCost = costStr.toDoubleOrNull()
        if (serviceCost == null) {
            edtServiceCost.error = "Enter valid cost"
            edtServiceCost.requestFocus()
            return
        }

        if (odoStr.isEmpty()) {
            edtOdometer.error = "Enter odometer reading"
            edtOdometer.requestFocus()
            return
        }
        val newOdometer = odoStr.toDoubleOrNull()
        if (newOdometer == null) {
            edtOdometer.error = "Enter valid odometer"
            edtOdometer.requestFocus()
            return
        }

        if (serviceDate.isEmpty()) {
            edtServiceDate.error = "Select service date"
            edtServiceDate.requestFocus()
            return
        }

        if (vehicleId.isEmpty()) {
            Toast.makeText(this, "Vehicle id missing", Toast.LENGTH_SHORT).show()
            return
        }

        val loading = ProgressDialog(this)
        loading.setCancelable(false)
        loading.setMessage("Saving service...")
        loading.show()

        val vehicleRef = db.collection("vehicles").document(vehicleId)
        val serviceRef = vehicleRef.collection("service_records").document()

        db.runTransaction { tx ->
            val vehicleSnap = tx.get(vehicleRef)
            if (!vehicleSnap.exists()) throw Exception("Vehicle not found")

            // get current odometer value -- try several possible field names
            val lastOdoDouble = when {
                vehicleSnap.contains("odometer_km") -> vehicleSnap.getDouble("odometer_km")
                vehicleSnap.contains("odometer") -> vehicleSnap.getDouble("odometer")
                vehicleSnap.contains("last_service_odometer") -> vehicleSnap.getDouble("last_service_odometer")
                vehicleSnap.contains("odometer_at_registration") -> vehicleSnap.getDouble("odometer_at_registration")
                else -> vehicleSnap.getDouble("odometer") // may be null
            } ?: 0.0

            if (newOdometer < lastOdoDouble) {
                // throw a sentinel exception string so we can identify it in failure listener
                throw IllegalArgumentException("LOW_ODOMETER")
            }

            // Prepare service record map
            val serviceData = mapOf(
                "serviceType" to serviceType,
                "serviceCost" to serviceCost,
                "serviceDate" to serviceDate,
                "odometer_km" to newOdometer,
                "notes" to notes,
                "createdAt" to FieldValue.serverTimestamp()
            )

            tx.set(serviceRef, serviceData)

            // Update vehicle odometer only if new is greater
            if (newOdometer > lastOdoDouble) {
                // pick a canonical field name to update; adapt to your DB fields
                tx.update(vehicleRef, "odometer_km", newOdometer)
                tx.update(vehicleRef, "last_service_odometer", newOdometer)
                tx.update(vehicleRef, "updated_at", FieldValue.serverTimestamp())
            }

            // transaction returns null
            null
        }.addOnSuccessListener {
            loading.dismiss()
            Toast.makeText(this, "Service record added", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            loading.dismiss()
            if (e is IllegalArgumentException && e.message == "LOW_ODOMETER") {
                Toast.makeText(this, "Odometer cannot be less than existing vehicle reading", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
