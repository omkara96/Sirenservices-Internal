package com.omkara.sirenservices_internal.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.TripModel

class ViewTripActivity : AppCompatActivity() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var tripId: String

    // Trip summary
    private lateinit var tvTripNumber: TextView
    private lateinit var tvTripDate: TextView
    private lateinit var tvVehicle: TextView
    private lateinit var tvDriver: TextView

    // Odometer
    private lateinit var tvStartOdometer: TextView
    private lateinit var tvEndOdometer: TextView
    private lateinit var tvFuelConsumed: TextView

    // Patient
    private lateinit var tvPatientName: TextView
    private lateinit var tvPatientContact: TextView
    private lateinit var tvCallSource: TextView

    // Servicing
    private lateinit var cardServicing: MaterialCardView
    private lateinit var tvMechanicName: TextView
    private lateinit var tvServicingKm: TextView
    private lateinit var tvMechanicCost: TextView

    // Financial
    private lateinit var tvTripCost: TextView
    private lateinit var tvFuelCost: TextView
    private lateinit var tvServicingCost: TextView
    private lateinit var tvPendingAmount: TextView
    private lateinit var tvHeadOfficeDeposit: TextView

    private lateinit var btnEditTrip: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_trip)

        tripId = intent.getStringExtra("trip_id") ?: run {
            finish()
            return
        }

        bindViews()
        setupActions()
        loadTrip()
    }

    private fun bindViews() {
        tvTripNumber = findViewById(R.id.tvTripNumber)
        tvTripDate = findViewById(R.id.tvTripDate)
        tvVehicle = findViewById(R.id.tvVehicle)
        tvDriver = findViewById(R.id.tvDriver)

        tvStartOdometer = findViewById(R.id.tvStartOdometer)
        tvEndOdometer = findViewById(R.id.tvEndOdometer)
        tvFuelConsumed = findViewById(R.id.tvFuelConsumed)

        tvPatientName = findViewById(R.id.tvPatientName)
        tvPatientContact = findViewById(R.id.tvPatientContact)
        tvCallSource = findViewById(R.id.tvCallSource)

        cardServicing = findViewById(R.id.cardServicing)
        tvMechanicName = findViewById(R.id.tvMechanicName)
        tvServicingKm = findViewById(R.id.tvServicingKm)
        tvMechanicCost = findViewById(R.id.tvMechanicCost)

        tvTripCost = findViewById(R.id.tvTripCost)
        tvFuelCost = findViewById(R.id.tvFuelCost)
        tvServicingCost = findViewById(R.id.tvServicingCost)
        tvPendingAmount = findViewById(R.id.tvPendingAmount)
        tvHeadOfficeDeposit = findViewById(R.id.tvHeadOfficeDeposit)

        btnEditTrip = findViewById(R.id.btnEditTrip)
    }

    private fun setupActions() {
        btnEditTrip.setOnClickListener {
            startActivity(
                Intent(this, EditTripActivity::class.java).apply {
                    putExtra("trip_id", tripId)
                }
            )
        }
    }

    private fun loadTrip() {
        db.collection("trips")
            .document(tripId)
            .get()
            .addOnSuccessListener { doc ->
                val trip = doc.toObject(TripModel::class.java) ?: return@addOnSuccessListener
                bindTripData(trip)
            }
    }

    private fun bindTripData(trip: TripModel) {

        // ===== TRIP SUMMARY =====
        tvTripNumber.text = trip.trip_number
        tvTripDate.text = "Date: ${trip.trip_date}"
        tvVehicle.text = "Vehicle: ${trip.vehicle_display}"
        tvDriver.text = "Driver: ${trip.driver_display}"

        // ===== ODOMETER =====
        tvStartOdometer.text = "Start Odometer: ${trip.start_odometer} km"
        tvFuelConsumed.text = "Fuel Consumed: ${trip.fuel_liters} L"

        if (trip.status == "COMPLETED") {
            tvEndOdometer.visibility = View.VISIBLE
            tvEndOdometer.text = "End Odometer: ${trip.end_odometer} km"
            btnEditTrip.visibility = View.GONE
        } else {
            tvEndOdometer.visibility = View.GONE
        }

        // ===== PATIENT =====
        tvPatientName.text = "Patient: ${trip.patient_name?.ifBlank { "N/A" }}"
        tvPatientContact.text = "Contact: ${trip.patient_number?.ifBlank { "N/A" }}"
        tvCallSource.text = "Call Source: ${trip.call_source?.ifBlank { "N/A" }}"

        // ===== SERVICING =====
        val servicingKm = trip.servicing_km ?: 0
        if (servicingKm > 0) {
            cardServicing.visibility = View.VISIBLE
            tvMechanicName.text = "Mechanic: ${trip.mechanic_name}"
            tvServicingKm.text = "Servicing KM: ${trip.servicing_km}"
            tvMechanicCost.text = "Cost: ₹${trip.mechanic_cost}"
        } else {
            cardServicing.visibility = View.GONE
        }

        // ===== FINANCIAL =====
        tvTripCost.text = "Trip Cost: ₹${trip.trip_cost}"
        tvFuelCost.text = "Fuel Cost: ₹${trip.fuel_cost}"
        tvServicingCost.text = "Servicing Cost: ₹${trip.servicing_cost}"
        tvPendingAmount.text = "Pending Amount: ₹${trip.pending_amount}"
        tvHeadOfficeDeposit.text = "HO Deposit: ₹${trip.head_office_deposit}"
    }
}
