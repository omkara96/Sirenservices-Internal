package com.omkara.sirenservices_internal.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.TripsAdapter
import com.omkara.sirenservices_internal.models.TripModel

class TripsDashboardActivity : AppCompatActivity() {

    private lateinit var tripsRecyclerView: RecyclerView
    private lateinit var fabAddTrip: FloatingActionButton
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trips_dashboard)

        tripsRecyclerView = findViewById(R.id.trips_recycler_view)
        fabAddTrip = findViewById(R.id.fab_add_trip)
        db = FirebaseFirestore.getInstance()

        fabAddTrip.setOnClickListener {
            startActivity(Intent(this, TripCreateActivity::class.java))
        }

        loadTrips()
    }

    private fun loadTrips() {
        db.collection("trips").get()
            .addOnSuccessListener { result ->
                val trips = result.toObjects(TripModel::class.java)
                tripsRecyclerView.adapter = TripsAdapter(trips)
            }
    }
}