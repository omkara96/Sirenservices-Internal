package com.omkara.sirenservices_internal.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.VehicleModel

class VehicleDetailsActivity : AppCompatActivity() {

    private lateinit var tvNumber: TextView
    private lateinit var tvMakeModel: TextView
    private lateinit var tvType: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvOdometer: TextView
    private lateinit var tvChassis: TextView
    private lateinit var tvEngine: TextView
    private lateinit var btnUpdateOdometer: Button
    private lateinit var btnAddService: Button
    private lateinit var btnViewServices: Button
    private lateinit var progress: ProgressBar

    private val db = FirebaseFirestore.getInstance()
    private var vehicleId: String = ""

    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_details)

        tvNumber = findViewById(R.id.tvNumber)
        tvMakeModel = findViewById(R.id.tvMakeModel)
        tvType = findViewById(R.id.tvType)
        tvStatus = findViewById(R.id.tvStatus)
        tvOdometer = findViewById(R.id.tvOdometer)
        tvChassis = findViewById(R.id.tvChassis)
        tvEngine = findViewById(R.id.tvEngine)
        btnUpdateOdometer = findViewById(R.id.btnUpdateOdometer)
        btnAddService = findViewById(R.id.btnAddService)
        btnViewServices = findViewById(R.id.btnViewServices)
        progress = findViewById(R.id.progress)

        vehicleId = intent.getStringExtra("vehicle_id") ?: ""
        if (vehicleId.isEmpty()) {
            Toast.makeText(this, "Vehicle id missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_progress)
            .setCancelable(false)
            .create()

        loadVehicle()

        btnUpdateOdometer.setOnClickListener { showUpdateOdometerDialog() }
        btnAddService.setOnClickListener {
            Toast.makeText(this, "Add service clicked and vehicle id is: " + vehicleId.toString().trim(), Toast.LENGTH_SHORT).show()
            val i = Intent(this, AddServiceRecordActivity::class.java)
            i.putExtra("vehicle_id", vehicleId)
            startActivity(i)
        }
        btnViewServices.setOnClickListener {
            val i = Intent(this, ServiceHistoryActivity::class.java)
            i.putExtra("vehicle_id", vehicleId)
            startActivity(i)
        }
    }

    private fun loadVehicle() {
        progress.visibility = View.VISIBLE
        db.collection("vehicles").document(vehicleId)
            .addSnapshotListener { snap, err ->
                progress.visibility = View.GONE
                if (err != null) {
                    Toast.makeText(this, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if (snap != null && snap.exists()) {
                    val vm = snap.toObject(VehicleModel::class.java)
                    tvNumber.text = vm?.vehicle_number ?: "-"
                    tvMakeModel.text = "${vm?.make ?: ""} ${vm?.model ?: ""}"
                    tvType.text = vm?.vehicle_type ?: ""
                    tvStatus.text = vm?.status ?: ""
                    val odo = vm?.last_service_odometer ?: vm?.odometer_at_registration
                    tvOdometer.text = "Odometer: ${odo?.toString() ?: "-"}"
                    tvChassis.text = "Chassis: ${vm?.chassis_number ?: "-"}"
                    tvEngine.text = "Engine: ${vm?.engine_number ?: "-"}"
                }
            }
    }

    private fun showUpdateOdometerDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update Odometer")

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_update_odometer, null)
        val edtOdo = view.findViewById<EditText>(R.id.edtOdometer)
        builder.setView(view)

        builder.setPositiveButton("Save") { d, _ ->
            val newOdo = edtOdo.text.toString().trim().toDoubleOrNull()
            if (newOdo == null) {
                Toast.makeText(this, "Enter valid number", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            updateOdometer(newOdo)
            d.dismiss()
        }

        builder.setNegativeButton("Cancel") { d, _ -> d.dismiss() }
        builder.show()
    }

    private fun updateOdometer(newOdo: Double) {
        progressDialog.show()
        val vehicleRef = db.collection("vehicles").document(vehicleId)

        // transaction to update odometer and optionally next_service_due_km logic
        db.runTransaction { tr ->
            val snap = tr.get(vehicleRef)
            val currentLastServiceOdo = snap.getDouble("last_service_odometer") ?: snap.getDouble("odometer_at_registration") ?: 0.0
            if (newOdo < currentLastServiceOdo) {
                throw Exception("New odometer ($newOdo) cannot be less than current recorded ($currentLastServiceOdo)")
            }
            tr.update(vehicleRef, mapOf<String, Any>(
                "odometer_at_registration" to newOdo,
                "last_service_odometer" to newOdo,
                "updated_at" to FieldValue.serverTimestamp()
            ))
        }.addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Odometer updated", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
