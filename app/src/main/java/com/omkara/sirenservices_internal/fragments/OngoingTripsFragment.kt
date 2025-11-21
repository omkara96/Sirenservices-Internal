package com.omkara.sirenservices_internal.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapters.TripAdapter
import com.omkara.sirenservices_internal.models.TripModel
import androidx.core.widget.addTextChangedListener

class OngoingTripsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var rv: RecyclerView
    private lateinit var search: TextInputEditText
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var adapter: TripAdapter
    private val list = mutableListOf<TripModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_ongoing_trips, container, false)
        rv = v.findViewById(R.id.rvOngoingTrips)
        search = v.findViewById(R.id.edtSearchOngoing)
        swipe = v.findViewById(R.id.swipeOngoing)

        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = TripAdapter(list) { trip ->
            // Open TripDetailsActivity (create if you haven't)
            // val i = Intent(requireContext(), TripDetailsActivity::class.java)
            // i.putExtra("trip_id", trip.id)
            // startActivity(i)
            Toast.makeText(requireContext(), "Clicked: ${trip.trip_number}", Toast.LENGTH_SHORT).show()
        }
        rv.adapter = adapter

        swipe.setOnRefreshListener { loadTrips() }

        // search text change: simple filter
        search.addTextChangedListener { editable ->
            filterList(editable?.toString() ?: "")
        }

        loadTrips()
        return v
    }

    private fun loadTrips() {
        swipe.isRefreshing = true
        list.clear()
        // query ongoing/assigned
        db.collection("trips")
            .whereIn("status", listOf("ASSIGNED", "ONGOING"))
            .orderBy("created_at", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                for (d in snap.documents) {
                    val t = d.toObject(TripModel::class.java)?.copy(id = d.id)
                    t?.let { list.add(it) }
                }
                adapter.updateList(list)
                swipe.isRefreshing = false
                toggleEmpty()
            }
            .addOnFailureListener { e ->
                swipe.isRefreshing = false
                Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun toggleEmpty() {
        val tvNo = view?.findViewById<View>(R.id.tvNoOngoing)
        tvNo?.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun filterList(query: String) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            adapter.updateList(list)
            toggleEmpty()
            return
        }
        val filtered = list.filter { t ->
            (t.vehicle_number?.lowercase()?.contains(q) == true) ||
                    (t.driver_name?.lowercase()?.contains(q) == true) ||
                    (t.trip_number.lowercase().contains(q)) ||
                    (t.pickup?.lowercase()?.contains(q) == true) ||
                    (t.final_drop?.lowercase()?.contains(q) == true) ||
                    (t.patient_name?.lowercase()?.contains(q) == true)
        }
        adapter.updateList(filtered)
        view?.findViewById<View>(R.id.tvNoOngoing)?.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}
