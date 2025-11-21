package com.omkara.sirenservices_internal.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.ParticularModel

class BillGeneratorActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var tripId: String

    private lateinit var particularsContainer: LinearLayout
    private lateinit var btnAddParticular: ImageButton
    private lateinit var btnSave: Button

    private lateinit var etInvoiceFor: TextInputEditText
    private lateinit var etAdjustments: TextInputEditText
    private lateinit var tvSubtotal: TextView

    private val particularsList = mutableListOf<ParticularModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_generator)

        db = FirebaseFirestore.getInstance()
        tripId = intent.getStringExtra("trip_id") ?: ""

        initViews()
       // loadExistingBill()
    }

    private fun initViews() {
        particularsContainer = findViewById(R.id.particularsContainer)
        btnAddParticular = findViewById(R.id.btnAddParticular)
        btnSave = findViewById(R.id.btnSaveBill)

        etInvoiceFor = findViewById(R.id.etInvoiceFor)
        etAdjustments = findViewById(R.id.etAdjustments)
        tvSubtotal = findViewById(R.id.tvSubtotal)

        btnAddParticular.setOnClickListener { addParticularRow() }
        btnSave.setOnClickListener { saveBill() }
    }

    private fun addParticularRow(model: ParticularModel? = null) {
        val view = layoutInflater.inflate(R.layout.item_particular, particularsContainer, false)

        val etName = view.findViewById<TextInputEditText>(R.id.etParticularName)
        val etQty = view.findViewById<TextInputEditText>(R.id.etQty)
        val etRate = view.findViewById<TextInputEditText>(R.id.etRate)
        val tvAmount = view.findViewById<TextView>(R.id.tvAmount)
        val deleteBtn = view.findViewById<ImageButton>(R.id.btnDeleteParticular)

        val item = model ?: ParticularModel("", 0, 0.0, 0.0)
        particularsList.add(item)

        etName.setText(item.name)
        etQty.setText(item.qty.toString())
        etRate.setText(item.rate.toString())
        tvAmount.text = "Amount: ₹${item.amount}"

        val watcher = {
            val q = etQty.text.toString().toIntOrNull() ?: 0
            val r = etRate.text.toString().toDoubleOrNull() ?: 0.0
            item.qty = q
            item.rate = r
            item.amount = q * r
            tvAmount.text = "Amount: ₹${item.amount}"
            updateSubtotal()
        }

        etQty.addTextChangedListener { watcher() }
        etRate.addTextChangedListener { watcher() }

        deleteBtn.setOnClickListener {
            particularsList.remove(item)
            particularsContainer.removeView(view)
            updateSubtotal()
        }

        particularsContainer.addView(view)
        updateSubtotal()
    }

    private fun updateSubtotal() {
        val total = particularsList.sumOf { it.amount }
        tvSubtotal.text = "₹$total"
    }



    private fun saveBill() {
        val subtotal = particularsList.sumOf { it.amount }
        val adjustments = etAdjustments.text.toString().toDoubleOrNull() ?: 0.0
        val finalTotal = subtotal + adjustments

        val bill = hashMapOf(
            "trip_id" to tripId,
            "invoice_for" to etInvoiceFor.text.toString(),
            "invoice_date" to FieldValue.serverTimestamp(),
            "bill_number" to "BILL-${System.currentTimeMillis()}",
            "subtotal" to subtotal,
            "adjustments" to adjustments,
            "total_amount" to finalTotal,
            "particulars" to particularsList.map {
                mapOf(
                    "name" to it.name,
                    "qty" to it.qty,
                    "rate" to it.rate,
                    "amount" to it.amount
                )
            }
        )

        db.collection("bills").document(tripId)
            .set(bill)
            .addOnSuccessListener {
                Toast.makeText(this, "Bill saved", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
