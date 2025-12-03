package com.omkara.sirenservices_internal.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.VehicleShowServiceRecordAdapter
import com.omkara.sirenservices_internal.models.ServiceRecord
import java.util.*

class VehicleShowServiceFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var swipe: SwipeRefreshLayout

    private lateinit var txtMonthlyCost: android.widget.TextView
    private lateinit var txtYearlyCost: android.widget.TextView
    private lateinit var txtTotalCost: android.widget.TextView

    private lateinit var adapter: VehicleShowServiceRecordAdapter
    private var vehicleId = ""

    private val db = FirebaseFirestore.getInstance()

    companion object {
        fun newInstance(vehicleId: String): VehicleShowServiceFragment {
            val f = VehicleShowServiceFragment()
            f.arguments = Bundle().apply { putString("vehicle_id", vehicleId) }
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vehicleId = arguments?.getString("vehicle_id") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_vehicle_show_service, container, false)

        swipe = v.findViewById(R.id.swipeRefresh)
        recycler = v.findViewById(R.id.recyclerService)

        txtMonthlyCost = v.findViewById(R.id.txtMonthlyCost)
        txtYearlyCost = v.findViewById(R.id.txtYearlyCost)
        txtTotalCost = v.findViewById(R.id.txtTotalCost)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = VehicleShowServiceRecordAdapter(emptyList())
        recycler.adapter = adapter

        v.findViewById<View>(R.id.btnAddService).setOnClickListener {
            showAddServiceDialog()
        }

        swipe.setOnRefreshListener {
            loadServiceRecords()
        }

        loadServiceRecords()

        return v
    }

    // -------------------------------------------------------
    // LOAD SERVICE RECORDS
    // -------------------------------------------------------
    private fun loadServiceRecords() {
        swipe.isRefreshing = true

        db.collection("vehicles")
            .document(vehicleId)
            .collection("service_records")
            .get()
            .addOnSuccessListener { snap ->

                val list = snap.documents.mapNotNull { it.toObject(ServiceRecord::class.java) }

                // Sort by newest → oldest
                val sorted = list.sortedByDescending { it.createdAt?.seconds ?: 0 }

                adapter.update(sorted)

                calculateDashboard(sorted)

                swipe.isRefreshing = false
            }
            .addOnFailureListener {
                swipe.isRefreshing = false
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // -------------------------------------------------------
    // CALCULATE MONTH / YEAR / TOTAL
    // -------------------------------------------------------
    private fun calculateDashboard(list: List<ServiceRecord>) {
        val now = Calendar.getInstance()
        val currMonth = now.get(Calendar.MONTH)
        val currYear = now.get(Calendar.YEAR)

        var monthCost = 0.0
        var yearCost = 0.0
        var totalCost = 0.0

        list.forEach { rec ->
            val ts = rec.createdAt ?: return@forEach
            val cal = Calendar.getInstance().apply { time = ts.toDate() }

            totalCost += rec.serviceCost

            if (cal.get(Calendar.YEAR) == currYear) {
                yearCost += rec.serviceCost
                if (cal.get(Calendar.MONTH) == currMonth) {
                    monthCost += rec.serviceCost
                }
            }
        }

        txtMonthlyCost.text = "₹${monthCost.toInt()}"
        txtYearlyCost.text = "₹${yearCost.toInt()}"
        txtTotalCost.text = "₹${totalCost.toInt()}"
    }

    // -------------------------------------------------------
    // ADD NEW SERVICE RECORD
    // -------------------------------------------------------
    private fun showAddServiceDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_service_record, null)

        val edtType = view.findViewById<EditText>(R.id.edtServiceType)
        val edtCost = view.findViewById<EditText>(R.id.edtServiceCost)
        val edtDate = view.findViewById<EditText>(R.id.edtServiceDate)
        val edtKM = view.findViewById<EditText>(R.id.edtServiceKM)
        val edtNotes = view.findViewById<EditText>(R.id.edtNotes)

        edtDate.setOnClickListener { pickDate(edtDate) }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Service Record")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                saveServiceRecord(
                    type = edtType.text.toString(),
                    cost = edtCost.text.toString().toDoubleOrNull() ?: 0.0,
                    date = edtDate.text.toString(),
                    km = edtKM.text.toString().toDoubleOrNull() ?: 0.0,
                    notes = edtNotes.text.toString()
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveServiceRecord(
        type: String,
        cost: Double,
        date: String,
        km: Double,
        notes: String
    ) {
        val record = ServiceRecord(
            serviceType = type,
            serviceCost = cost,
            serviceDate = date,
            odometer_km = km,
            notes = notes,
            createdAt = Timestamp.now()
        )

        db.collection("vehicles")
            .document(vehicleId)
            .collection("service_records")
            .add(record)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Service Added", Toast.LENGTH_SHORT).show()
                loadServiceRecords()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun pickDate(target: EditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(),
            { _, y, m, d ->
                target.setText(String.format("%02d-%02d-%04d", d, m + 1, y))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
