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
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity
import java.util.*

class ServiceInfo : Fragment() {

    private lateinit var edtLastServiceDate: TextInputEditText
    private lateinit var edtLastServiceOdometer: TextInputEditText
    private lateinit var edtWorkshop: TextInputEditText
    private lateinit var edtServiceNotes: TextInputEditText
    private lateinit var edtNextServiceKm: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_service_info, container, false)
        bindViews(v)
        setupDatePicker()
        return v
    }

    private fun bindViews(v: View) {
        edtLastServiceDate = v.findViewById(R.id.edtLastServiceDate)
        edtLastServiceOdometer = v.findViewById(R.id.edtLastServiceOdometer)
        edtWorkshop = v.findViewById(R.id.edtWorkshop)
        edtServiceNotes = v.findViewById(R.id.edtServiceNotes)
        edtNextServiceKm = v.findViewById(R.id.edtNextServiceKm)
    }

    private fun setupDatePicker() {
        edtLastServiceDate.isFocusable = false
        edtLastServiceDate.setOnClickListener { showDatePicker(edtLastServiceDate) }
        edtLastServiceDate.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDatePicker(edtLastServiceDate) }
    }

    private fun showDatePicker(target: TextInputEditText) {
        val cal = Calendar.getInstance()
        val dp = DatePickerDialog(requireContext(), { _, y, m, d ->
            val s = String.format(Locale.getDefault(), "%02d-%02d-%04d", d, m+1, y)
            target.setText(s)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dp.datePicker.maxDate = System.currentTimeMillis()
        dp.show()
    }

    fun validate(): Boolean {
        val odoRegStr = (activity as? VehicleRegistrationActivity)?.pagerAdapter?.getVehicleFragment()?.getData()?.get("odometer_at_registration") as? Double
        val lastOdoStr = edtLastServiceOdometer.text.toString().trim().toDoubleOrNull()

        if (lastOdoStr != null && odoRegStr != null) {
            if (lastOdoStr < odoRegStr) {
                edtLastServiceOdometer.error = "Service odo cannot be less than registration odo"
                return false
            }
        }
        return true
    }

    fun getData(): Map<String, Any?> {
        val m = HashMap<String, Any?>()
        m["last_service_date"] = edtLastServiceDate.text.toString().trim().ifEmpty { null }
        m["last_service_odometer"] = edtLastServiceOdometer.text.toString().trim().toDoubleOrNull()
        m["last_service_workshop"] = edtWorkshop.text.toString().trim().ifEmpty { null }
        m["last_service_notes"] = edtServiceNotes.text.toString().trim().ifEmpty { null }
        m["next_service_due_km"] = edtNextServiceKm.text.toString().trim().toDoubleOrNull()
        return m
    }
}
