package com.omkara.sirenservices_internal.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.VehicleShowComplianceHistoryAdapter
import com.omkara.sirenservices_internal.models.ComplianceCurrent
import java.text.SimpleDateFormat
import java.util.*

class ComplianceHistoryActivity : AppCompatActivity() {

    private lateinit var txtTitle: TextView
    private lateinit var txtCurrentTitle: TextView
    private lateinit var txtCurrentDetails: TextView
    private lateinit var edtSearch: EditText
    private lateinit var recycler: RecyclerView

    private var vehicleId = ""
    private var complianceType = ""

    private var adapter: VehicleShowComplianceHistoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compliance_history)

        vehicleId = intent.getStringExtra("vehicle_id") ?: ""
        complianceType = intent.getStringExtra("compliance_type") ?: ""

        txtTitle = findViewById(R.id.txtHistoryTitle)
        txtCurrentTitle = findViewById(R.id.txtCurrentTitle)
        txtCurrentDetails = findViewById(R.id.txtCurrentDetails)
        edtSearch = findViewById(R.id.edtSearch)
        recycler = findViewById(R.id.recyclerHistory)

        txtTitle.text = "${complianceType.uppercase()} History"
        txtCurrentTitle.text = "Current ${complianceType.uppercase()}"

        recycler.layoutManager = LinearLayoutManager(this)

        loadHistory()
        setupSearch()
    }

    private fun setupSearch() {
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter?.setFilter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadHistory() {
        FirebaseFirestore.getInstance()
            .collection("vehicles")
            .document(vehicleId)
            .get()
            .addOnSuccessListener { doc ->

                val root = doc.get("compliance") as? Map<String, Any> ?: emptyMap()
                val section = root[complianceType] as? Map<String, Any> ?: emptyMap()

                // 🔹 Load current
                val currentMap = section["current"] as? Map<String, Any>
                val currentObj = currentMap?.let { mapToComplianceCurrent(it) }

                if (currentObj != null) {
                    txtCurrentDetails.text = """
                        Provider: ${currentObj.provider ?: "-"}
                        Number: ${currentObj.number ?: "-"}
                        Cert No: ${currentObj.certificate_no ?: "-"}
                        Type: ${currentObj.type ?: "-"}
                        Premium: ${currentObj.premium ?: "-"}
                        Valid: ${currentObj.valid_from ?: "-"} → ${currentObj.valid_till ?: "-"}
                    """.trimIndent()
                } else {
                    txtCurrentDetails.text = "No Active Record"
                }

                // 🔹 HISTORY
                val historyListMap =
                    section["history"] as? List<Map<String, Any>> ?: emptyList()

                val historyList = historyListMap.map { mapToComplianceCurrent(it) }

                // 🔹 Sort by valid_till DESC
                val sorted = historyList.sortedByDescending {
                    parseDate(it.valid_till)
                }

                adapter = VehicleShowComplianceHistoryAdapter(sorted)
                recycler.adapter = adapter

            }.addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun parseDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return 0
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            sdf.parse(dateStr)?.time ?: 0
        } catch (e: Exception) { 0 }
    }

    private fun mapToComplianceCurrent(map: Map<String, Any>): ComplianceCurrent {
        return ComplianceCurrent(
            provider = map["provider"] as? String,
            certificate_no = map["certificate_no"] as? String,
            number = map["number"] as? String,
            type = map["type"] as? String,
            premium = (map["premium"] as? Number)?.toDouble(),
            valid_from = map["valid_from"] as? String,
            valid_till = map["valid_till"] as? String,
            updated_at = map["updated_at"] as? Timestamp
        )
    }
}
