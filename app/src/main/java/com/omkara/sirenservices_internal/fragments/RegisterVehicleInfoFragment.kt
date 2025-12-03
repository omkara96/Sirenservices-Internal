package com.omkara.sirenservices_internal.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.S3Uploader
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity
import com.omkara.sirenservices_internal.viewmodels.VehicleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class RegisterVehicleInfoFragment : Fragment() {

    private val vm: VehicleViewModel by activityViewModels()

    // Text inputs
    private lateinit var edtVehicleNumber: TextInputEditText
    private lateinit var edtRcNumber: TextInputEditText
    private lateinit var edtMake: TextInputEditText
    private lateinit var edtModel: TextInputEditText
    private lateinit var edtYear: TextInputEditText
    private lateinit var edtSeats: TextInputEditText
    private lateinit var edtOdometerAtReg: TextInputEditText
    private lateinit var edtChassiNumber: TextInputEditText
    private lateinit var edtEngineNumber: TextInputEditText

    // Dropdowns
    private lateinit var transmissionType: AutoCompleteTextView
    private lateinit var fuelType: AutoCompleteTextView
    private lateinit var vehicleOwnership: AutoCompleteTextView

    // Photo ImageViews
    private lateinit var imgFront: ImageView
    private lateinit var imgBack: ImageView
    private lateinit var imgLeft: ImageView
    private lateinit var imgRight: ImageView
    private lateinit var imgRcBook: ImageView

    private lateinit var btnNext: FloatingActionButton

    private val IMAGE_PICKER = 5001
    private var currentUploadKey: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_vehicle_info, container, false)

        bind(v)
        fixFabInsets(v)
        setupDropdowns()
        setupImageClickListeners()

        btnNext.setOnClickListener { proceedNext() }

        return v
    }

    // -------------------------------
    // Bind all XML Views
    // -------------------------------
    private fun bind(v: View) {

        imgFront = v.findViewById(R.id.imgFrontPhoto)
        imgBack = v.findViewById(R.id.imgBackPhoto)
        imgLeft = v.findViewById(R.id.imgLeftPhoto)
        imgRight = v.findViewById(R.id.imgRightPhoto)
        imgRcBook = v.findViewById(R.id.imgRcBookPhoto)

        edtVehicleNumber = v.findViewById(R.id.edtVehicleNumber)
        edtRcNumber = v.findViewById(R.id.edtRcNumber)
        edtMake = v.findViewById(R.id.edtMake)
        edtModel = v.findViewById(R.id.edtModel)
        edtYear = v.findViewById(R.id.edtYear)
        edtSeats = v.findViewById(R.id.edtSeats)
        edtOdometerAtReg = v.findViewById(R.id.edtOdometerAtReg)
        edtChassiNumber = v.findViewById(R.id.edtChassiNumber)
        edtEngineNumber = v.findViewById(R.id.edtEngineNumber)

        transmissionType = v.findViewById(R.id.autoTransmissionType)
        fuelType = v.findViewById(R.id.autoFuelType)
        vehicleOwnership = v.findViewById(R.id.autoOwnershipType)

        btnNext = v.findViewById(R.id.btnNext)
    }

    // -------------------------------
    // Dropdown setup
    // -------------------------------
    private fun setupDropdowns() {
        fuelType.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1,
                listOf("Petrol", "Diesel", "CNG", "EV"))
        )

        transmissionType.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1,
                listOf("Manual", "Automatic"))
        )

        vehicleOwnership.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1,
                listOf("OWN/Self", "Third Party"))
        )
    }

    // -------------------------------
    // FAB position fix
    // -------------------------------
    private fun fixFabInsets(v: View) {
        ViewCompat.setOnApplyWindowInsetsListener(v) { _, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            btnNext.translationY = -bottomInset.toFloat()
            btnNext.setPadding(0, 0, 0, bottomInset + 40)
            insets
        }
    }

    // -------------------------------
    // Setup photo click listeners
    // -------------------------------
    private fun setupImageClickListeners() {

        imgFront.setOnClickListener { pickImage("photo_front") }
        imgBack.setOnClickListener { pickImage("photo_back") }
        imgLeft.setOnClickListener { pickImage("photo_left") }
        imgRight.setOnClickListener { pickImage("photo_right") }
        imgRcBook.setOnClickListener { pickImage("photo_rcbook") }
    }

    private fun pickImage(key: String) {

        if (edtVehicleNumber.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Enter Vehicle Number first!", Toast.LENGTH_LONG).show()
            return
        }

        currentUploadKey = key

        val i = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(i, IMAGE_PICKER)
    }

    // -------------------------------
    // Image picker result
    // -------------------------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICKER && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            val key = currentUploadKey ?: return

            uploadToS3(uri, key)
        }
    }

    // -------------------------------
    // Upload to S3 without UUID
    // -------------------------------
    private fun uploadToS3(uri: Uri, key: String) {

        val vehicleId = edtVehicleNumber.text.toString().trim()

        val fileName = when (key) {
            "photo_front" -> "front.jpg"
            "photo_back" -> "back.jpg"
            "photo_left" -> "left.jpg"
            "photo_right" -> "right.jpg"
            "photo_rcbook" -> "rc_book.jpg"
            else -> "unknown.jpg"
        }

        lifecycleScope.launch(Dispatchers.IO) {

            try {
                val stream: InputStream =
                    requireContext().contentResolver.openInputStream(uri)!!

                val url = S3Uploader.uploadAmbulancePhoto(
                    vehicleId,
                    fileName,
                    stream
                )

                vm.setPhoto(key, url)

                withContext(Dispatchers.Main) {
                    updateImagePreview(key, uri)
                    Toast.makeText(requireContext(), "Uploaded Successfully!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // -------------------------------
    // Update image preview
    // -------------------------------
    private fun updateImagePreview(key: String, uri: Uri) {

        val target: ImageView? = when (key) {
            "photo_front" -> imgFront
            "photo_back" -> imgBack
            "photo_left" -> imgLeft
            "photo_right" -> imgRight
            "photo_rcbook" -> imgRcBook
            else -> null
        }

        target?.load(uri)
    }

    // -------------------------------
    // Validation
    // -------------------------------
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

    // -------------------------------
    // Save & go next
    // -------------------------------
    private fun proceedNext() {

        if (!validate()) return

        vm.updateVehicleInfo(
            mapOf(
                "vehicle_number" to edtVehicleNumber.text.toString().trim(),
                "rc_number" to edtRcNumber.text.toString().trim(),
                "make" to edtMake.text.toString().trim(),
                "model" to edtModel.text.toString().trim(),
                "manufacture_year" to edtYear.text.toString().trim().toInt(),
                "seating_capacity" to edtSeats.text.toString().trim().toInt(),
                "odometer_at_registration" to edtOdometerAtReg.text.toString().trim().toDouble(),
                "chassis_number" to edtChassiNumber.text.toString().trim(),
                "engine_number" to edtEngineNumber.text.toString().trim(),
                "transmission" to transmissionType.text.toString().trim(),
                "fuel_type" to fuelType.text.toString().trim(),
                "owner_type" to vehicleOwnership.text.toString().trim(),
                "status" to "ACTIVE"
            )
        )

        (activity as VehicleRegistrationActivity).nextStep()
    }
}
