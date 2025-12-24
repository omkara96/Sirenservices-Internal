package com.omkara.sirenservices_internal.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapters.TripAdapter
import com.omkara.sirenservices_internal.models.TripModel
import java.text.SimpleDateFormat
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.ListenerRegistration
import java.util.*
import com.google.firebase.firestore.MetadataChanges


class CompletedTripsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var rv: RecyclerView
    private lateinit var search: TextInputEditText
    private lateinit var btnDate: Button
    private lateinit var adapter: TripAdapter
    private val list = mutableListOf<TripModel>()
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var filterDate: String? = null
    private var tripListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_completed_trips, container, false)
        rv = v.findViewById(R.id.rvCompletedTrips)
        search = v.findViewById(R.id.edtSearchCompleted)
        btnDate = v.findViewById(R.id.btnFilterDate)

        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = TripAdapter(list) { trip ->
            Toast.makeText(requireContext(), "Clicked: ${trip.trip_number}", Toast.LENGTH_SHORT).show()
        }
        rv.adapter = adapter

        btnDate.setOnClickListener { showDatePicker() }

        search.addTextChangedListener { editable ->
            filterList(editable?.toString() ?: "")
        }

        //loadTrips()
        return v
    }

//    override fun onResume() {
//        super.onResume()
//      //  loadTrips() // 🔥 force refresh
//    }


    private fun showDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val cal = Calendar.getInstance()
            cal.set(y, m, d)
            filterDate = sdf.format(cal.time)
            btnDate.text = filterDate
          //  loadTrips()
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

//    private fun loadTrips() {
//        list.clear()
//        // if a date filter is present, query for that date (assuming trip_date stored as dd/MM/yyyy)
//        val baseQuery = if (!filterDate.isNullOrEmpty()) {
//            db.collection("trips")
//                .whereEqualTo("status", "COMPLETED")
//                .whereEqualTo("trip_date", filterDate)
//                .orderBy("created_at", Query.Direction.DESCENDING)
//        } else {
//            db.collection("trips")
//                .whereEqualTo("status", "COMPLETED")
//                .orderBy("created_at", Query.Direction.DESCENDING)
//        }
//
//        baseQuery.get()
//            .addOnSuccessListener { snap ->
//                for (d in snap.documents) {
//                    val t = d.toObject(TripModel::class.java)?.copy(id = d.id)
//                    t?.let { list.add(it) }
//                }
//                adapter.updateList(list)
//                toggleEmpty()
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_LONG).show()
//            }
//    }

    private fun toggleEmpty() {
        view?.findViewById<View>(R.id.tvNoCompleted)?.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
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
        view?.findViewById<View>(R.id.tvNoCompleted)?.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun attachTripListener() {

        tripListener?.remove()

        val baseQuery = if (!filterDate.isNullOrEmpty()) {
            db.collection("trips")
                .whereEqualTo("status", "COMPLETED")
                .whereEqualTo("trip_date", filterDate)
                .orderBy("created_at", Query.Direction.DESCENDING)
        } else {
            db.collection("trips")
                .whereEqualTo("status", "COMPLETED")
                .orderBy("created_at", Query.Direction.DESCENDING)
        }

        tripListener = baseQuery.addSnapshotListener(
            MetadataChanges.INCLUDE
        ) { snap, err ->

            if (err != null || snap == null) {
                Toast.makeText(requireContext(), "Load failed", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            // 🚨 Ignore cached data
            if (snap.metadata.isFromCache) {
                Log.d("CMPLT-FRGMNT", "Cached snapshot ignored")
                return@addSnapshotListener
            }

            list.clear()

            for (d in snap.documents) {
                val billGenerated = d.getBoolean("is_bill_generated") ?: false
                Log.d(
                    "CMPLT-FRGMNT",
                    "SERVER SNAP → Trip=${d.getString("trip_number")}, billGenerated=$billGenerated"
                )

                val t = d.toObject(TripModel::class.java)?.copy(id = d.id)
                t?.let { list.add(it) }
            }

            adapter.updateList(list)
            toggleEmpty()
        }


    }


    private fun forceReloadFromServer() {
        val baseQuery = if (!filterDate.isNullOrEmpty()) {
            db.collection("trips")
                .whereEqualTo("status", "COMPLETED")
                .whereEqualTo("trip_date", filterDate)
                .orderBy("created_at", Query.Direction.DESCENDING)
        } else {
            db.collection("trips")
                .whereEqualTo("status", "COMPLETED")
                .orderBy("created_at", Query.Direction.DESCENDING)
        }

        baseQuery
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener { snap ->
                list.clear()
                for (d in snap.documents) {
                    val t = d.toObject(TripModel::class.java)?.copy(id = d.id)
                    t?.let { list.add(it) }
                }
                adapter.updateList(list)
                toggleEmpty()
            }
    }


    override fun onResume() {
        super.onResume()
        forceReloadFromServer()
    }
    override fun onStart() {
        super.onStart()
        attachTripListener()
    }

    override fun onStop() {
        super.onStop()
        tripListener?.remove()
    }


}
