package com.omkara.sirenservices_internal.fragments


import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.omkara.sirenservices_internal.R
import java.util.*

class Cmpliance : Fragment() {

    private lateinit var edtInsuranceProvider: TextInputEditText
    private lateinit var edtInsuranceNumber: TextInputEditText
    private lateinit var edtInsuranceStart: TextInputEditText
    private lateinit var edtInsuranceEnd: TextInputEditText

    private lateinit var edtPucNumber: TextInputEditText
    private lateinit var edtPucStart: TextInputEditText
    private lateinit var edtPucEnd: TextInputEditText

    private lateinit var edtFitnessNumber: TextInputEditText
    private lateinit var edtFitnessExpiry: TextInputEditText
    private lateinit var edtPermitNumber: TextInputEditText
    private lateinit var edtPermitExpiry: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_cmpliance , container, false)
        bindViews(v)
        setupDatePickers()
        return v
    }

    private fun bindViews(v: View) {
        edtInsuranceProvider = v.findViewById(R.id.edtInsuranceProvider)
        edtInsuranceNumber = v.findViewById(R.id.edtInsuranceNumber)
        edtInsuranceStart = v.findViewById(R.id.edtInsuranceStart)
        edtInsuranceEnd = v.findViewById(R.id.edtInsuranceEnd)

        edtPucNumber = v.findViewById(R.id.edtPucNumber)
        edtPucStart = v.findViewById(R.id.edtPucStart)
        edtPucEnd = v.findViewById(R.id.edtPucEnd)

        edtFitnessNumber = v.findViewById(R.id.edtFitnessNumber)
        edtFitnessExpiry = v.findViewById(R.id.edtFitnessExpiry)
        edtPermitNumber = v.findViewById(R.id.edtPermitNumber)
        edtPermitExpiry = v.findViewById(R.id.edtPermitExpiry)
    }

    private fun setupDatePickers() {
        listOf(edtInsuranceStart, edtInsuranceEnd, edtPucStart, edtPucEnd, edtFitnessExpiry, edtPermitExpiry).forEach { edit ->
            edit.isFocusable = false
            edit.setOnClickListener { showDatePicker(edit) }
            edit.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDatePicker(edit) }
        }
    }

    private fun showDatePicker(target: TextInputEditText) {
        val cal = Calendar.getInstance()
        val dp = DatePickerDialog(requireContext(), { _, y, m, d ->
            val date = String.format(Locale.getDefault(), "%02d-%02d-%04d", d, m+1, y)
            target.setText(date)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dp.show()
    }

    fun validate(): Boolean {
        // insurance dates check (if both given)
        val insStart = edtInsuranceStart.text.toString().trim()
        val insEnd = edtInsuranceEnd.text.toString().trim()
        if (insStart.isNotEmpty() && insEnd.isNotEmpty()) {
            // minimal check: format dd-mm-yyyy and end after start
            if (!isDateOrderValid(insStart, insEnd)) {
                edtInsuranceEnd.error = "Expiry must be after start"
                return false
            }
        }
        val pucStart = edtPucStart.text.toString().trim()
        val pucEnd = edtPucEnd.text.toString().trim()
        if (pucStart.isNotEmpty() && pucEnd.isNotEmpty()) {
            if (!isDateOrderValid(pucStart, pucEnd)) {
                edtPucEnd.error = "PUC valid till must be after start"
                return false
            }
        }
        val fitness = edtFitnessExpiry.text.toString().trim()
        val permit = edtPermitExpiry.text.toString().trim()
        // no strict requirements; optional fields allowed
        return true
    }

    private fun isDateOrderValid(start: String, end: String): Boolean {
        // parse dd-MM-yyyy
        try {
            val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val s = sdf.parse(start)
            val e = sdf.parse(end)
            return s != null && e != null && !e.before(s)
        } catch (e: Exception) {
            return false
        }
    }

    fun getData(): Map<String, Any?> {
        val m = HashMap<String, Any?>()
        m["insurance_provider"] = edtInsuranceProvider.text.toString().trim()
        m["insurance_number"] = edtInsuranceNumber.text.toString().trim()
        m["insurance_start"] = edtInsuranceStart.text.toString().trim().ifEmpty { null }
        m["insurance_end"] = edtInsuranceEnd.text.toString().trim().ifEmpty { null }

        m["puc_number"] = edtPucNumber.text.toString().trim()
        m["puc_start"] = edtPucStart.text.toString().trim().ifEmpty { null }
        m["puc_end"] = edtPucEnd.text.toString().trim().ifEmpty { null }

        m["fitness_number"] = edtFitnessNumber.text.toString().trim()
        m["fitness_expiry"] = edtFitnessExpiry.text.toString().trim().ifEmpty { null }
        m["permit_number"] = edtPermitNumber.text.toString().trim()
        m["permit_expiry"] = edtPermitExpiry.text.toString().trim().ifEmpty { null }
        return m
    }
}
