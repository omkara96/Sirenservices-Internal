package com.omkara.sirenservices_internal.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.VehicleAdapter
import com.omkara.sirenservices_internal.adapter.VehicleListItem
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity
import java.text.SimpleDateFormat
import java.util.Locale

class VehicleListActivity : AppCompatActivity() {

    private lateinit var edtSearch: EditText
    private lateinit var spFilterStatus: Spinner
    private lateinit var btnClearFilter: Button
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var rvVehicles: RecyclerView
    private lateinit var fabAdd: View
    private lateinit var tvEmpty: TextView

    private lateinit var adapter: VehicleAdapter
    private val allVehicles = ArrayList<VehicleListItem>()
    private val filteredVehicles = ArrayList<VehicleListItem>()

    private val firestore = FirebaseFirestore.getInstance()
    private var vehiclesListener: ListenerRegistration? = null

    private lateinit var progressDialog: AlertDialog

    private val statusOptions = listOf("All", "ACTIVE", "UNDER_MAINTENANCE", "DECOMMISSIONED")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_list)

        initViews()
        initAdapter()
        initStatusFilter()
        initListeners()

        startListeningVehicles()
    }

    override fun onDestroy() {
        vehiclesListener?.remove()
        super.onDestroy()
    }

    // ---------------------------------------------------------
    // INITIAL SETUP
    // ---------------------------------------------------------

    private fun initViews() {
        edtSearch = findViewById(R.id.edtSearch)
        spFilterStatus = findViewById(R.id.spFilterStatus)
        btnClearFilter = findViewById(R.id.btnClearFilter)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        rvVehicles = findViewById(R.id.rvVehicles)
        fabAdd = findViewById(R.id.fabAdd)
        tvEmpty = findViewById(R.id.tvEmpty)

        // Progress dialog
        progressDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_progress)
            .setCancelable(false)
            .create()
    }

    private fun initAdapter() {
        adapter = VehicleAdapter(filteredVehicles) { item ->
            val intent = Intent(this, VehicleDetailsActivity::class.java)
            intent.putExtra("vehicle_id", item.id)
            startActivity(intent)
        }

        rvVehicles.layoutManager = LinearLayoutManager(this)
        rvVehicles.adapter = adapter
    }

    private fun initStatusFilter() {
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFilterStatus.adapter = spinnerAdapter
    }

    private fun initListeners() {

        fabAdd.setOnClickListener {
            startActivity(Intent(this, VehicleRegistrationActivity::class.java))
        }

        swipeRefresh.setOnRefreshListener {
            refreshVehicles()
        }

        btnClearFilter.setOnClickListener {
            spFilterStatus.setSelection(0)
            edtSearch.setText("")
            applyFilters()
        }

        edtSearch.setOnEditorActionListener { _, _, _ ->
            applyFilters()
            true
        }

        spFilterStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // ---------------------------------------------------------
    // FIREBASE LIVE LISTEN
    // ---------------------------------------------------------

    private fun startListeningVehicles() {
        progressDialog.show()

        vehiclesListener?.remove()

        vehiclesListener = firestore.collection("vehicles")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->

                progressDialog.dismiss()
                swipeRefresh.isRefreshing = false

                if (err != null) {
                    Toast.makeText(
                        this,
                        "Error loading vehicles: ${err.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addSnapshotListener
                }

                allVehicles.clear()

                if (snap != null && !snap.isEmpty) {

                    for (doc in snap.documents) {

                        val id = doc.id

                        // -------------------------
                        // BASIC DETAILS
                        // -------------------------
                        val vehicleNumber = doc.getString("vehicle_info.vehicle_number") ?: ""
                        val make = doc.getString("vehicle_info.make") ?: ""
                        val model = doc.getString("vehicle_info.model") ?: ""

                        // -------------------------
                        // STATUS (new model)
                        // -------------------------
                        val status = doc.getString("vehicle_info.status") ?: "ACTIVE"

                        // -------------------------
                        // PHOTOS
                        // -------------------------
                        val photoFront = doc.getString("photos.photo_front")

                        // -------------------------
                        // ODOMETER
                        // -------------------------
                        val odometerKm =
                            doc.getDouble("service_info.last_service_odometer")
                                ?: doc.getDouble("vehicle_info.odometer_at_registration")
                                ?: 0.0

                        // -------------------------
                        // FETCH LATEST SERVICE DATE FROM SUBCOLLECTION
                        // -------------------------
                        firestore.collection("vehicles")
                            .document(id)
                            .collection("service_records")
                            .orderBy("createdAt", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { serviceSnap ->

                                val lastServiceDate: String = if (!serviceSnap.isEmpty) {
                                    val ts = serviceSnap.documents[0].getTimestamp("createdAt")
                                    val date = ts?.toDate()
                                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date!!)
                                } else {
                                    "-" // no service records
                                }

                                // Add AFTER pulling service date
                                allVehicles.add(
                                    VehicleListItem(
                                        id = id,
                                        vehicleNumber = vehicleNumber,
                                        make = make,
                                        model = model,
                                        status = status,
                                        odometerKm = odometerKm,
                                        photoUrl = photoFront,
                                        lastServiceDate = lastServiceDate
                                    )
                                )

                                // Only refresh UI after all vehicles loaded
                                if (allVehicles.size == snap.size()) {
                                    applyFilters()
                                }
                            }
                    }
                }
            }
    }


    private fun refreshVehicles() {
        swipeRefresh.isRefreshing = true
        startListeningVehicles()
    }

    // ---------------------------------------------------------
    // FILTERING
    // ---------------------------------------------------------

    private fun applyFilters() {
        val query = edtSearch.text.toString().trim().lowercase()
        val statusFilter = spFilterStatus.selectedItem.toString()

        filteredVehicles.clear()

        for (v in allVehicles) {
            val matchesSearch =
                query.isEmpty() ||
                        v.vehicleNumber.lowercase().contains(query) ||
                        v.make.lowercase().contains(query) ||
                        v.model.lowercase().contains(query)

            val matchesStatus =
                (statusFilter == "All") || (v.status == statusFilter)

            if (matchesSearch && matchesStatus)
                filteredVehicles.add(v)
        }

        adapter.updateList(filteredVehicles)
        tvEmpty.visibility = if (filteredVehicles.isEmpty()) View.VISIBLE else View.GONE
    }
}
