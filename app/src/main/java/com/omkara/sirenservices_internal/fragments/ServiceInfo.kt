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
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity
import com.omkara.sirenservices_internal.viewmodels.VehicleViewModel
import java.util.*

class ServiceInfo : Fragment() {

    private val vm: VehicleViewModel by activityViewModels()

    private lateinit var edtLastServiceDate: TextInputEditText
    private lateinit var edtLastServiceOdometer: TextInputEditText
    private lateinit var edtWorkshop: TextInputEditText
    private lateinit var edtNextServiceKm: TextInputEditText

    private lateinit var btnPrev: FloatingActionButton
    private lateinit var btnNext: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_service_info, container, false)

        bind(v)
        fixFabInsets(v)
        setupDatePicker()
        setupClearErrorOnFocus()
        setupNavigation()

        return v
    }

    private fun bind(v: View) {
        edtLastServiceDate = v.findViewById(R.id.edtLastServiceDate)
        edtLastServiceOdometer = v.findViewById(R.id.edtLastServiceOdometer)
        edtWorkshop = v.findViewById(R.id.edtWorkshop)
        edtNextServiceKm = v.findViewById(R.id.edtNextServiceKm)

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

    private fun setupDatePicker() {
        edtLastServiceDate.isFocusable = false
        edtLastServiceDate.setOnClickListener { showDatePicker(edtLastServiceDate) }
        edtLastServiceDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showDatePicker(edtLastServiceDate)
        }
    }

    private fun showDatePicker(target: TextInputEditText) {
        val cal = Calendar.getInstance()
        val dp = DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                target.setText(String.format("%02d-%02d-%04d", d, m + 1, y))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dp.datePicker.maxDate = System.currentTimeMillis()
        dp.show()
    }

    private fun setupClearErrorOnFocus() {
        listOf(
            edtLastServiceDate,
            edtLastServiceOdometer,
            edtWorkshop,
            edtNextServiceKm
        ).forEach { field ->
            field.setOnFocusChangeListener { _, _ ->
                field.error = null
            }
        }
    }

    private fun setupNavigation() {
        btnPrev.setOnClickListener {
            (activity as VehicleRegistrationActivity).prevStep()
        }

        btnNext.setOnClickListener {
            if(edtLastServiceDate.text.isNullOrBlank()){
                saveToViewModel()
                (activity as VehicleRegistrationActivity).nextStep()
            }
            else if (validate()) {
                saveToViewModel()
                (activity as VehicleRegistrationActivity).nextStep()
            }
        }
    }

    private fun validate(): Boolean {

        // Required: Last service date
        if (edtLastServiceDate.text.isNullOrBlank()) {
            edtLastServiceDate.error = "Required"
            return false
        }

        // Required: Last service KM
        val lastOdo = edtLastServiceOdometer.text.toString().trim().toDoubleOrNull()
        if (lastOdo == null) {
            edtLastServiceOdometer.error = "Enter valid KM"
            return false
        }

        // Workshop name
        if (edtWorkshop.text.isNullOrBlank()) {
            edtWorkshop.error = "Required"
            return false
        }

        // Next service due KM
        val nextKm = edtNextServiceKm.text.toString().trim().toDoubleOrNull()
        if (nextKm == null) {
            edtNextServiceKm.error = "Enter valid next KM"
            return false
        }

        // Compare with registration odometer
        val regOdo = vm.odometerAtRegistration
        if (regOdo != null && lastOdo < regOdo) {
            edtLastServiceOdometer.error = "Cannot be less than registration KM ($regOdo)"
            return false
        }

        // Next service must be greater
        if (nextKm <= lastOdo) {
            edtNextServiceKm.error = "Next KM must be greater than last service KM"
            return false
        }

        return true
    }

    private fun saveToViewModel() {

        val data = mapOf(
            "last_service_date" to edtLastServiceDate.text.toString().trim(),
            "last_service_odometer" to edtLastServiceOdometer.text.toString().trim().toDoubleOrNull(),
            "last_service_workshop" to edtWorkshop.text.toString().trim(),
            "next_service_due_km" to edtNextServiceKm.text.toString().trim().toDoubleOrNull()
        )

        vm.updateServiceInfo(data)
    }
}
