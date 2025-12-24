package com.omkara.sirenservices_internal.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.BillComponent

class BillGeneratorActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var tripId: String

    /* ---------- DATA ---------- */
    private var tripCost = 0.0
    private var gstAmount = 0.0
    private var finalTotal = 0.0
    private var isEditMode = false

    private val components = mutableListOf<BillComponent>()

    /* ---------- VIEWS ---------- */
    private lateinit var tvTripId: TextView
    private lateinit var tvTripNumber: TextView
    private lateinit var tvTripCost: TextView
    private lateinit var tvFinalTotal: TextView

    private lateinit var etInvoiceFor: TextInputEditText
    private lateinit var componentsContainer: LinearLayout
    private lateinit var cbGst: CheckBox
    private lateinit var etGstPercent: TextInputEditText
    private lateinit var tilGstPercent: View
    private lateinit var btnSaveBill: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_generator)

        tripId = intent.getStringExtra("trip_id") ?: run {
            finish(); return
        }

        bindViews()
        setupActions()
        loadTripDetails()
        loadExistingBillIfAny()
    }

    /* ================= INIT ================= */

    private fun bindViews() {
        tvTripId = findViewById(R.id.tvTripId)
        tvTripNumber = findViewById(R.id.tvTripNumber)
        tvTripCost = findViewById(R.id.tvTripCost)
        tvFinalTotal = findViewById(R.id.tvFinalTotal)

        etInvoiceFor = findViewById(R.id.etInvoiceFor)
        componentsContainer = findViewById(R.id.componentsContainer)
        cbGst = findViewById(R.id.cbGst)
        etGstPercent = findViewById(R.id.etGstPercent)
        tilGstPercent = findViewById(R.id.tilGstPercent)
        btnSaveBill = findViewById(R.id.btnGeneratePdf) // reuse button

        findViewById<ImageButton>(R.id.btnAddComponent)
            .setOnClickListener { addComponentRow() }
    }

    private fun setupActions() {

        cbGst.setOnCheckedChangeListener { _, checked ->
            tilGstPercent.visibility = if (checked) View.VISIBLE else View.GONE
            updateTotals()
        }

        etGstPercent.addTextChangedListener { updateTotals() }

        btnSaveBill.setOnClickListener { saveBill() }
    }

    /* ================= LOAD ================= */

    private fun loadTripDetails() {
        db.collection("trips").document(tripId).get()
            .addOnSuccessListener { snap ->

                tvTripId.text = "Trip ID: $tripId"
                tvTripNumber.text = "Trip Number: ${snap.getString("trip_number") ?: "-"}"

                tripCost = snap.getDouble("trip_cost") ?: 0.0
                tvTripCost.text = "₹$tripCost"

                etInvoiceFor.setText(snap.getString("patient_name") ?: "")
                updateTotals()
            }
    }

    private fun loadExistingBillIfAny() {
        db.collection("bills").document(tripId).get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) return@addOnSuccessListener

                isEditMode = true
                btnSaveBill.text = "Update Bill"

                etInvoiceFor.setText(snap.getString("invoice_for") ?: "")

                cbGst.isChecked = snap.getBoolean("gst_enabled") == true
                etGstPercent.setText(
                    snap.getDouble("gst_percent")?.toString() ?: ""
                )

                val list = snap.get("components") as? List<Map<String, Any>> ?: emptyList()
                list.forEach {
                    val component = BillComponent(
                        name = it["name"] as String,
                        amount = (it["amount"] as Number).toDouble()
                    )
                    components.add(component)
                    addComponentRow(component)
                }

                updateTotals()
            }
    }

    /* ================= COMPONENTS ================= */

    private fun addComponentRow(existing: BillComponent? = null) {

        val view = layoutInflater.inflate(
            R.layout.item_bill_component,
            componentsContainer,
            false
        )

        val etName = view.findViewById<TextInputEditText>(R.id.etComponentName)
        val etAmount = view.findViewById<TextInputEditText>(R.id.etComponentAmount)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDeleteComponent)

        val component = existing ?: BillComponent().also { components.add(it) }

        etName.setText(component.name)
        etAmount.setText(component.amount.toString())

        etName.addTextChangedListener { component.name = it.toString() }
        etAmount.addTextChangedListener {
            component.amount = it.toString().toDoubleOrNull() ?: 0.0
            updateTotals()
        }

        btnDelete.setOnClickListener {
            components.remove(component)
            componentsContainer.removeView(view)
            updateTotals()
        }

        componentsContainer.addView(view)
    }

    /* ================= TOTAL ================= */

    private fun updateTotals() {
        val componentTotal = components.sumOf { it.amount }
        val subTotal = tripCost + componentTotal

        gstAmount = if (cbGst.isChecked) {
            val percent = etGstPercent.text.toString().toDoubleOrNull() ?: 0.0
            subTotal * percent / 100
        } else 0.0

        finalTotal = subTotal + gstAmount
        tvFinalTotal.text = "Total: ₹$finalTotal"
    }

    /* ================= SAVE ================= */

    private fun saveBill() {

        val billData = hashMapOf(
            "trip_id" to tripId,
            "invoice_for" to etInvoiceFor.text.toString().trim(),
            "base_trip_cost" to tripCost,
            "components" to components.map {
                mapOf("name" to it.name, "amount" to it.amount)
            },
            "gst_enabled" to cbGst.isChecked,
            "gst_percent" to (etGstPercent.text.toString().toDoubleOrNull() ?: 0.0),
            "gst_amount" to gstAmount,
            "total_amount" to finalTotal,
            "updated_at" to FieldValue.serverTimestamp()
        )

        db.collection("bills").document(tripId)
            .set(billData)
            .addOnSuccessListener {
                db.collection("trips")
                    .document(tripId)
                    .update("is_bill_generated", true)

                Toast.makeText(this, "Bill saved successfully", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }

    }
}
