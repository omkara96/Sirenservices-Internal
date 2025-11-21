package com.omkara.sirenservices_internal.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.TripModel

class ViewTripActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var tripId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_trip)

        tripId = intent.getStringExtra("trip_id") ?: ""

        loadTrip()

        findViewById<Button>(R.id.btnEditTrip).setOnClickListener {
            val i = Intent(this, EditTripActivity::class.java)
            i.putExtra("trip_id", tripId)
            startActivity(i)
        }
    }

    private fun loadTrip() {
        db.collection("trips").document(tripId).get()
            .addOnSuccessListener { doc ->

                val trip = doc.toObject(TripModel::class.java)

                if (trip != null) {
                    findViewById<TextView>(R.id.tvTripNumber).text = trip.trip_number
                    findViewById<TextView>(R.id.tvTripDate).text = trip.trip_date
                    findViewById<TextView>(R.id.tvVehicle).text = trip.vehicle_display
                    findViewById<TextView>(R.id.tvDriver).text = trip.driver_display
                    findViewById<TextView>(R.id.tvPickup).text = trip.pickup
                    findViewById<TextView>(R.id.tvDrop).text = trip.final_drop
                    findViewById<TextView>(R.id.tvTripCost).text = "₹${trip.trip_cost}"
                    findViewById<TextView>(R.id.tvFuelCost).text = "₹${trip.fuel_cost}"
                    findViewById<TextView>(R.id.tvServicingCost).text = "₹${trip.servicing_cost}"
                    findViewById<TextView>(R.id.tvPendingAmount).text = "₹${trip.pending_amount}"
                    findViewById<TextView>(R.id.tvPaymentMode).text = trip.payment_mode

                    val layoutStops = findViewById<LinearLayout>(R.id.layoutStops)
                    layoutStops.removeAllViews()

                    trip.intermediate_stops.forEach { stop ->
                        val tv = TextView(this)
                        tv.text = "• $stop"
                        tv.textSize = 16f
                        tv.setPadding(6, 6, 6, 6)
                        layoutStops.addView(tv)
                    }

                    // hide edit button for completed trips
                    if (trip.status == "COMPLETED") {
                        findViewById<Button>(R.id.btnEditTrip).visibility = View.GONE
                    }
                }
            }
    }
}
