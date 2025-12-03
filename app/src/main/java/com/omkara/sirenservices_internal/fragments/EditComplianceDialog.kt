package com.omkara.sirenservices_internal.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.models.ComplianceCurrent
import com.omkara.sirenservices_internal.models.toMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class EditComplianceDialog : DialogFragment() {

    private lateinit var edtProvider: EditText
    private lateinit var edtCertificateNo: EditText
    private lateinit var edtNumber: EditText
    private lateinit var edtType: EditText
    private lateinit var edtPremium: EditText
    private lateinit var edtFrom: EditText
    private lateinit var edtTill: EditText

    private var vehicleId = ""
    private var section = ""
    private var current: ComplianceCurrent? = null
    var onSaved: (() -> Unit)? = null


    companion object {
        fun newInstance(
            vehicleId: String,
            section: String,
            current: ComplianceCurrent?
        ): EditComplianceDialog {

            return EditComplianceDialog().apply {
                arguments = Bundle().apply {
                    putString("vehicleId", vehicleId)
                    putString("section", section)
                    putParcelable("current", current)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        vehicleId = arguments?.getString("vehicleId")!!
        section = arguments?.getString("section")!!
        current = arguments?.getParcelable("current")

        val v = requireActivity().layoutInflater.inflate(R.layout.dialog_edit_compliance, null)

        edtProvider = v.findViewById(R.id.edtProvider)
        edtCertificateNo = v.findViewById(R.id.edtCertificateNo)
        edtNumber = v.findViewById(R.id.edtNumber)
        edtType = v.findViewById(R.id.edtType)
        edtPremium = v.findViewById(R.id.edtPremium)
        edtFrom = v.findViewById(R.id.edtFrom)
        edtTill = v.findViewById(R.id.edtTill)

        fillOldData()
        setupDatePickers()

        return AlertDialog.Builder(requireContext())
            .setTitle("Update ${section.uppercase()}")
            .setView(v)
            .setPositiveButton("Save") { _, _ -> save() }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun fillOldData() {
        current?.let {
            edtProvider.setText(it.provider)
            edtCertificateNo.setText(it.certificate_no)
            edtNumber.setText(it.number)
            edtType.setText(it.type)
            edtPremium.setText(it.premium?.toString() ?: "")
            edtFrom.setText(it.valid_from)
            edtTill.setText(it.valid_till)
        }
    }

    private fun setupDatePickers() {
        edtFrom.setOnClickListener { showDatePicker(edtFrom) }
        edtTill.setOnClickListener { showDatePicker(edtTill) }
    }

    private fun showDatePicker(target: EditText) {
        val c = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                target.setText(String.format("%02d-%02d-%04d", d, m + 1, y))
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun save() {
        val newData = ComplianceCurrent(
            provider = edtProvider.text.toString(),
            certificate_no = edtCertificateNo.text.toString(),
            number = edtNumber.text.toString(),
            type = edtType.text.toString(),
            premium = edtPremium.text.toString().toDoubleOrNull(),
            valid_from = edtFrom.text.toString(),
            valid_till = edtTill.text.toString(),
            updated_at = Timestamp.now()
        )

        lifecycleScope.launch(Dispatchers.IO) {
            updateFirestore(newData)

            launch(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Compliance Updated!", Toast.LENGTH_SHORT).show()
                onSaved?.invoke()   // 🔥 notify fragment
                dismiss()
            }
        }
    }

    private suspend fun updateFirestore(newData: ComplianceCurrent) {
        val db = FirebaseFirestore.getInstance()
        val ref = db.collection("vehicles").document(vehicleId)

        db.runTransaction { tx ->
            val snap = tx.get(ref)

            val sectionObj = snap.get("compliance.$section") as? Map<String, Any?>
            val current = sectionObj?.get("current") as? Map<String, Any?>
            val history = sectionObj?.get("history") as? List<Map<String, Any?>> ?: emptyList()

            val updatedHistory =
                if (current != null) history + current else history

            val updatedSection = mapOf(
                "current" to newData.toMap(),
                "history" to updatedHistory,
                "documents" to (sectionObj?.get("documents") ?: emptyList<Any>())
            )

            tx.update(ref, "compliance.$section", updatedSection)
        }.await()
    }
}
