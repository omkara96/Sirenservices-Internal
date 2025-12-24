package com.omkara.sirenservices_internal.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R

class ViewBillActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var tripId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_bill)

        tripId = intent.getStringExtra("trip_id") ?: run {
            finish(); return
        }

        loadBill()
        findViewById<Button>(R.id.btnGeneratePdf).setOnClickListener {
            Toast.makeText(this, "PDF generation coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBill() {

        db.collection("bills").document(tripId).get()
            .addOnSuccessListener { billSnap ->

                if (!billSnap.exists()) {
                    Toast.makeText(this, "Bill not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                db.collection("trips").document(tripId).get()
                    .addOnSuccessListener { tripSnap ->

                        /* ---------------- HEADER ---------------- */

                        findViewById<TextView>(R.id.tvBillNumber).text =
                            "Bill No: ${tripSnap.getString("trip_number") ?: "-"}"

                        findViewById<TextView>(R.id.tvBillDate).text =
                            "Date: ${tripSnap.getString("trip_date") ?: "-"}"

                        findViewById<TextView>(R.id.tvInvoiceFor).text =
                            "Invoice For: ${billSnap.getString("invoice_for") ?: "-"}"


                        /* ---------------- KM DETAILS ---------------- */

                        val startKm = tripSnap.getLong("start_odometer") ?: 0
                        val endKm = tripSnap.getLong("end_odometer") ?: 0
                        val totalKm = endKm - startKm

                        findViewById<TextView>(R.id.tvKmDetails).text =
                            "Start KM: $startKm   End KM: $endKm   Total KM: $totalKm"


                        /* ---------------- PARTICULARS ---------------- */

                        val container = findViewById<LinearLayout>(R.id.layoutParticulars)
                        container.removeAllViews()

                        var subtotal = 0.0

                        // 1️⃣ Base Trip Charge (MANDATORY)
                        val baseTripCost = billSnap.getDouble("base_trip_cost") ?: 0.0
                        subtotal += baseTripCost
                        container.addView(createParticularRow("Base Trip Charge", baseTripCost))

                        // 2️⃣ Additional Components
                        val components =
                            billSnap.get("components") as? List<Map<String, Any>> ?: emptyList()

                        for (p in components) {
                            val name = p["name"] as? String ?: continue
                            val amount = (p["amount"] as? Number)?.toDouble() ?: 0.0
                            subtotal += amount
                            container.addView(createParticularRow(name, amount))
                        }

                        // 3️⃣ Two blank rows (visual spacing)
                        repeat(2) {
                            container.addView(createParticularRow(" ", 0.0, isBlank = true))
                        }


                        /* ---------------- TOTALS CARD ---------------- */

                        findViewById<TextView>(R.id.tvSubtotal).text =
                            "Subtotal: ₹$subtotal"

                        val gstEnabled = billSnap.getBoolean("gst_enabled") == true
                        val gstPercent = billSnap.getDouble("gst_percent") ?: 0.0
                        val gstAmount = billSnap.getDouble("gst_amount") ?: 0.0

                        val tvGst = findViewById<TextView>(R.id.tvGst)
                        if (gstEnabled) {
                            tvGst.visibility = View.VISIBLE
                            tvGst.text = "GST {Applicable only on Base Price: ₹$baseTripCost} ($gstPercent%): ₹$gstAmount"
                        } else {
                            tvGst.visibility = View.GONE
                        }

                        val grandTotal = billSnap.getDouble("total_amount") ?: subtotal
                        findViewById<TextView>(R.id.tvGrandTotal).text =
                            "Grand Total: ₹$grandTotal"

                        findViewById<TextView>(R.id.tvAmountWords).text =
                            "Amount in words: ${convertToWords(grandTotal)}"
                    }
            }
    }

    private fun createParticularRow(
        name: String,
        amount: Double,
        isBlank: Boolean = false
    ): LinearLayout {

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val tvName = TextView(this)
        tvName.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        tvName.text = name

        val tvAmount = TextView(this)
        tvAmount.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        tvAmount.text = if (isBlank) "" else "₹$amount"

        row.addView(tvName)
        row.addView(tvAmount)

        return row
    }



    private fun convertToWords(amount: Double): String {
        // placeholder – implement later
        return "Rupees ${amount.toInt()} only"
    }
}
