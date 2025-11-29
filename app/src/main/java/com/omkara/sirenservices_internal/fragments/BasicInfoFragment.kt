package com.omkara.sirenservices_internal.fragments.details

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import coil.load
import coil.request.CachePolicy
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R

class BasicInfoFragment : Fragment() {

    private lateinit var layoutImages: LinearLayout
    private lateinit var txtVehicleNumber: TextView
    private lateinit var txtMakeModel: TextView
    private lateinit var txtYear: TextView
    private lateinit var txtFuel: TextView
    private lateinit var txtTransmission: TextView

    private lateinit var txtChassis: TextView
    private lateinit var txtEngine: TextView

    private lateinit var txtOdometer: TextView

    private lateinit var txtAlerts: TextView
    private lateinit var txtNotes: TextView
    private lateinit var btnUpdateOdometer: View

    private var vehicleId: String = ""

    companion object {
        fun newInstance(vehicleId: String): BasicInfoFragment {
            val f = BasicInfoFragment()
            val b = Bundle()
            b.putString("vehicle_id", vehicleId)
            f.arguments = b
            return f
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vehicleId = requireActivity().intent.getStringExtra("vehicle_id") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_basic_info, container, false)
        bindViews(v)
        loadVehicleInfo()

        return v
    }

    private fun bindViews(v: View) {
        layoutImages = v.findViewById(R.id.layoutImages)

        txtVehicleNumber = v.findViewById(R.id.txtVehicleNumber)
        txtMakeModel = v.findViewById(R.id.txtMakeModel)
        txtYear = v.findViewById(R.id.txtYear)
        txtFuel = v.findViewById(R.id.txtFuel)
        txtTransmission = v.findViewById(R.id.txtTransmission)

        txtChassis = v.findViewById(R.id.txtChassis)
        txtEngine = v.findViewById(R.id.txtEngine)

        txtOdometer = v.findViewById(R.id.txtOdometer)
        btnUpdateOdometer = v.findViewById(R.id.btnUpdateOdometer)

        txtAlerts = v.findViewById(R.id.txtAlerts)
        txtNotes = v.findViewById(R.id.txtNotes)
    }

    // --------------------------------------------------------
    // LOAD VEHICLE DATA FROM FIRESTORE
    // --------------------------------------------------------
    private fun loadVehicleInfo() {

        FirebaseFirestore.getInstance()
            .collection("vehicles")
            .document(vehicleId)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) return@addOnSuccessListener

                txtVehicleNumber.text = doc.getString("vehicle_number") ?: "-"
                txtMakeModel.text = "${doc.getString("make")} • ${doc.getString("model")}"

                txtYear.text = "Year: ${doc.get("manufacture_year") ?: "-"}"
                txtFuel.text = "Fuel: ${doc.getString("fuel_type") ?: "-"}"
                txtTransmission.text = "Transmission: ${doc.getString("transmission") ?: "-"}"

                txtChassis.text = "Chassis No: ${doc.getString("chassis_number") ?: "-"}"
                txtEngine.text = "Engine No: ${doc.getString("engine_number") ?: "-"}"

                val odometer = doc.getDouble("last_service_odometer")
                    ?: doc.getDouble("odometer_at_registration")
                txtOdometer.text = "${odometer ?: 0} km"

                loadImages(doc.get("photos") as? Map<String, String> ?: emptyMap())

                btnUpdateOdometer.setOnClickListener {
                    showUpdateOdometerDialog(odometer ?: 0.0)
                }

            }
    }

    // --------------------------------------------------------
    // LOAD IMAGES
    // --------------------------------------------------------
    private fun loadImages(photoMap: Map<String, String>) {
        layoutImages.removeAllViews()

        val keys = listOf(
            "photo_front", "photo_back", "photo_left",
            "photo_right", "photo_extra",
            "doc_rc", "doc_insurance", "doc_puc",
            "doc_permit", "doc_fitness", "doc_other"
        )

        for (key in keys) {
            val url = photoMap[key] ?: continue

            val img = ImageView(requireContext())
            val params = LinearLayout.LayoutParams(160, 120)
            params.marginEnd = 12
            img.layoutParams = params
            img.scaleType = ImageView.ScaleType.CENTER_CROP
            img.setBackgroundResource(R.drawable.round_image_bg)

            img.load(url) {
                crossfade(true)
                placeholder(R.drawable.ic_ambulance)
                error(R.drawable.ic_att_absent)
                diskCachePolicy(CachePolicy.ENABLED)
            }

            img.setOnClickListener {
                openFullImage(url)
            }

            layoutImages.addView(img)
        }

        if (layoutImages.childCount == 0) {
            val t = TextView(requireContext())
            t.text = "No photos uploaded"
            t.setPadding(16, 8, 16, 8)
            layoutImages.addView(t)
        }
    }

    // --------------------------------------------------------
    // FULL SCREEN IMAGE VIEWER
    // --------------------------------------------------------
    private fun openFullImage(url: String) {
        val dialog = AlertDialog.Builder(requireContext()).create()
        val img = ImageView(requireContext())

        img.load(url) {
            placeholder(R.drawable.ic_ambulance)
            error(R.drawable.ic_att_absent)
            diskCachePolicy(CachePolicy.ENABLED)
        }

        dialog.setView(img)
        dialog.show()
    }

    // --------------------------------------------------------
    // UPDATE ODOMETER
    // --------------------------------------------------------
    private fun showUpdateOdometerDialog(currentOdo: Double) {
        val input = EditText(requireContext())
        input.hint = "Enter new Odometer"
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText(currentOdo.toInt().toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Update Odometer")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->

                val newOdo = input.text.toString().toDoubleOrNull()
                if (newOdo == null || newOdo < currentOdo) {
                    showToast("Invalid odometer value!")
                    return@setPositiveButton
                }

                updateOdometerToFirestore(newOdo)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateOdometerToFirestore(newOdo: Double) {

        FirebaseFirestore.getInstance()
            .collection("vehicles")
            .document(vehicleId)
            .update("last_service_odometer", newOdo)
            .addOnSuccessListener {
                txtOdometer.text = "$newOdo km"
                showToast("Odometer updated successfully")
            }
            .addOnFailureListener {
                showToast("Failed: ${it.message}")
            }
    }

    private fun showToast(msg: String) {
        android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show()
    }
}
