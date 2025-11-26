package com.omkara.sirenservices_internal.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity
import com.omkara.sirenservices_internal.viewmodels.VehicleViewModel
import java.text.SimpleDateFormat
import java.util.*

class Cmpliance : Fragment() {

    private val vm: VehicleViewModel by activityViewModels()

    // Inputs
    private lateinit var edtInsuranceProvider: TextInputEditText
    private lateinit var edtInsuranceNumber: TextInputEditText
    private lateinit var edtInsuranceStart: TextInputEditText
    private lateinit var edtInsuranceEnd: TextInputEditText

    private lateinit var edtPucNumber: TextInputEditText
    private lateinit var edtPucStart: TextInputEditText
    private lateinit var edtPucEnd: TextInputEditText

    private lateinit var edtPermitNumber: TextInputEditText
    private lateinit var edtPermitExpiry: TextInputEditText

    private lateinit var edtFitnessExpiry: TextInputEditText

    // Layouts for errors
    private lateinit var lytInsuranceProvider: TextInputLayout
    private lateinit var lytInsuranceNumber: TextInputLayout
    private lateinit var lytPucNumber: TextInputLayout
    private lateinit var lytPermitNumber: TextInputLayout

    // FAB Buttons
    private lateinit var btnPrev: FloatingActionButton
    private lateinit var btnNext: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_cmpliance, container, false)

        bind(v)
        setupDatePickers()
        setupErrorClearListeners()
        fixFabInsets(v)

        btnPrev.setOnClickListener {
            (activity as VehicleRegistrationActivity).prevStep()
        }

        btnNext.setOnClickListener {
            if (validate()) {
                saveToViewModel()
                (activity as VehicleRegistrationActivity).nextStep()
            }
        }

        return v
    }

    private fun bind(v: View) {

        // Layouts
        lytInsuranceProvider = v.findViewById(R.id.lytInsuranceProvider)
        lytInsuranceNumber = v.findViewById(R.id.lytInsuranceNumber)
        lytPucNumber = v.findViewById(R.id.lytPucNumber)
        lytPermitNumber = v.findViewById(R.id.lytPermitNumber)

        // Inputs
        edtInsuranceProvider = v.findViewById(R.id.edtInsuranceProvider)
        edtInsuranceNumber = v.findViewById(R.id.edtInsuranceNumber)
        edtInsuranceStart = v.findViewById(R.id.edtInsuranceStart)
        edtInsuranceEnd = v.findViewById(R.id.edtInsuranceEnd)

        edtPucNumber = v.findViewById(R.id.edtPucNumber)
        edtPucStart = v.findViewById(R.id.edtPucStart)
        edtPucEnd = v.findViewById(R.id.edtPucEnd)

        edtPermitNumber = v.findViewById(R.id.edtPermitNumber)
        edtPermitExpiry = v.findViewById(R.id.edtPermitExpiry)

        edtFitnessExpiry = v.findViewById(R.id.edtFitnessExpiry)

        // FAB Buttons
        btnPrev = v.findViewById(R.id.btnPrev)
        btnNext = v.findViewById(R.id.btnNext)
    }

    private fun fixFabInsets(v: View) {
        ViewCompat.setOnApplyWindowInsetsListener(v) { _, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            btnNext.translationY = -bottomInset.toFloat()
            btnPrev.translationY = -bottomInset.toFloat()

            insets
        }
    }

    private fun setupDatePickers() {
        val fields = listOf(
            edtInsuranceStart, edtInsuranceEnd,
            edtPucStart, edtPucEnd,
            edtPermitExpiry, edtFitnessExpiry
        )

        fields.forEach { field ->
            field.isFocusable = false
            field.setOnClickListener { showDatePicker(field) }
        }
    }

    private fun showDatePicker(target: TextInputEditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                target.setText(String.format("%02d-%02d-%04d", d, m + 1, y))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupErrorClearListeners() {
        listOf(
            lytInsuranceProvider,
            lytInsuranceNumber,
            lytPucNumber,
            lytPermitNumber
        ).forEach { layout ->
            layout.editText?.setOnFocusChangeListener { _, _ -> layout.error = null }
        }
    }

    private fun validate(): Boolean {

        var ok = true

        if (edtInsuranceProvider.text.isNullOrBlank()) {
            lytInsuranceProvider.error = "Required"
            ok = false
        }

        if (edtInsuranceNumber.text.isNullOrBlank()) {
            lytInsuranceNumber.error = "Required"
            ok = false
        }

        if (edtPucNumber.text.isNullOrBlank()) {
            lytPucNumber.error = "Required"
            ok = false
        }

        if (edtPermitNumber.text.isNullOrBlank()) {
            lytPermitNumber.error = "Required"
            ok = false
        }

        if (!checkDateOrder(edtInsuranceStart, edtInsuranceEnd, "Insurance expiry must be after start")) ok = false
        if (!checkDateOrder(edtPucStart, edtPucEnd, "PUC expiry must be after start")) ok = false

        return ok
    }

    private fun checkDateOrder(
        startField: TextInputEditText,
        endField: TextInputEditText,
        errorMsg: String
    ): Boolean {

        val s = startField.text.toString()
        val e = endField.text.toString()

        if (s.isEmpty() || e.isEmpty()) return true

        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val d1 = sdf.parse(s)
            val d2 = sdf.parse(e)

            if (d1 != null && d2 != null && d2.before(d1)) {
                endField.error = errorMsg
                false
            } else true

        } catch (e: Exception) {
            endField.error = "Invalid date format"
            false
        }
    }

    private fun saveToViewModel() {

        val data = hashMapOf<String, Any?>(
            "insurance_provider" to edtInsuranceProvider.text.toString().trim(),
            "insurance_number" to edtInsuranceNumber.text.toString().trim(),
            "insurance_start" to edtInsuranceStart.text.toString().trim(),
            "insurance_end" to edtInsuranceEnd.text.toString().trim(),

            "puc_number" to edtPucNumber.text.toString().trim(),
            "puc_start" to edtPucStart.text.toString().trim(),
            "puc_end" to edtPucEnd.text.toString().trim(),

            "rc_expiry" to null, // You can bind RC expiry field if needed
            "permit_number" to edtPermitNumber.text.toString().trim(),
            "permit_expiry" to edtPermitExpiry.text.toString().trim(),
            "fitness_expiry" to edtFitnessExpiry.text.toString().trim()
        )

        vm.updateComplianceInfo(data)
    }
}
