package com.omkara.sirenservices_internal.fragments

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

class VehicleInfoFragment : Fragment() {

    private val vm: VehicleViewModel by activityViewModels()

    private lateinit var edtVehicleNumber: TextInputEditText
    private lateinit var edtRcNumber: TextInputEditText
    private lateinit var edtMake: TextInputEditText
    private lateinit var edtModel: TextInputEditText
    private lateinit var edtYear: TextInputEditText
    private lateinit var edtSeats: TextInputEditText
    private lateinit var edtOdometerAtReg: TextInputEditText

    private lateinit var btnNext: FloatingActionButton

    // ----- FIX FAB POSITION -----
    private fun fixFabInsets(v: View) {
        ViewCompat.setOnApplyWindowInsetsListener(v) { _, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            btnNext.translationY = -bottomInset.toFloat()
            btnNext.setPadding(0, 0, 0, bottomInset + 40)

            insets
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_vehicle_info, container, false)

        bind(v)
        fixFabInsets(v)

        btnNext.setOnClickListener {
            if (validate()) {

                // SAVE DATA TO VIEWMODEL
                vm.updateVehicleInfo(
                    mapOf(
                        "vehicle_number" to edtVehicleNumber.text.toString().trim(),
                        "rc_number" to edtRcNumber.text.toString().trim(),
                        "make" to edtMake.text.toString().trim(),
                        "model" to edtModel.text.toString().trim(),
                        "manufacture_year" to edtYear.text.toString().trim().toInt(),
                        "seating_capacity" to edtSeats.text.toString().trim().toInt(),
                        "odometer_at_registration" to edtOdometerAtReg.text.toString().trim().toDouble()
                    )
                )

                (activity as VehicleRegistrationActivity).nextStep()
            }
        }

        return v
    }

    private fun bind(v: View) {
        edtVehicleNumber = v.findViewById(R.id.edtVehicleNumber)
        edtRcNumber = v.findViewById(R.id.edtRcNumber)
        edtMake = v.findViewById(R.id.edtMake)
        edtModel = v.findViewById(R.id.edtModel)
        edtYear = v.findViewById(R.id.edtYear)
        edtSeats = v.findViewById(R.id.edtSeats)
        edtOdometerAtReg = v.findViewById(R.id.edtOdometerAtReg)

        btnNext = v.findViewById(R.id.btnNext)
    }

    // ----- VALIDATION -----
    private fun validate(): Boolean {

        if (edtVehicleNumber.text.isNullOrBlank()) {
            edtVehicleNumber.error = "Required"
            return false
        }

        if (edtMake.text.isNullOrBlank()) {
            edtMake.error = "Required"
            return false
        }

        if (edtModel.text.isNullOrBlank()) {
            edtModel.error = "Required"
            return false
        }

        if (edtYear.text.isNullOrBlank()) {
            edtYear.error = "Required"
            return false
        }

        if (edtSeats.text.isNullOrBlank()) {
            edtSeats.error = "Required"
            return false
        }

        if (edtOdometerAtReg.text.isNullOrBlank()) {
            edtOdometerAtReg.error = "Required"
            return false
        }

        return true
    }
}
