package com.omkara.sirenservices_internal.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity
import com.omkara.sirenservices_internal.viewmodels.VehicleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ReviewStepFragment : Fragment() {

    private val vm: VehicleViewModel by activityViewModels()

    private lateinit var btnSave: FloatingActionButton

    private lateinit var txtVehicle: TextView
    private lateinit var txtCompliance: TextView
    private lateinit var txtService: TextView
    private lateinit var txtPhotos: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_review_step, container, false)

        bindViews(v)
        fillData()

        btnSave.setOnClickListener { saveToFirestore() }

        return v
    }

    private fun bindViews(v: View) {
        btnSave = v.findViewById(R.id.btnSave)

        txtVehicle = v.findViewById(R.id.txtReview)
        txtCompliance = v.findViewById(R.id.txtcmpl)
        txtService = v.findViewById(R.id.txtser)
        txtPhotos = v.findViewById(R.id.txtphoto)
    }

    /** ---------------------------------------------------------
     *  DISPLAY ALL DATA FROM VIEWMODEL
     *  --------------------------------------------------------- */
    private fun fillData() {

        txtVehicle.text = """
            • Vehicle No: ${vm.vehicleNumber.value}
            • RC No: ${vm.rcNumber.value}
            • Make / Model: ${vm.make.value} / ${vm.model.value}
            • Year: ${vm.manufactureYear.value}
            • Seats: ${vm.seatingCapacity.value}
            • Odometer At Registration: ${vm.odometerAtRegistration}
        """.trimIndent()

        txtCompliance.text = """
            INSURANCE
            • Provider: ${vm.insuranceProvider.value}
            • Number: ${vm.insuranceNumber.value}
            • Start: ${vm.insuranceStartDate.value}
            • End: ${vm.insuranceEndDate.value}

            PUC
            • Number: ${vm.pucNumber.value}
            • Start: ${vm.pucStartDate.value}
            • End: ${vm.pucEndDate.value}

            OTHER
            • RC Expiry: ${vm.rcExpiryDate.value}
            • Permit Number: ${vm.permitNumber.value}
            • Permit Expiry: ${vm.permitExpiry.value}
            • Fitness Expiry: ${vm.fitnessExpiry.value}
        """.trimIndent()

        txtService.text = """
            • Last Service Date: ${vm.lastServiceDate}
            • Last Service Odo: ${vm.lastServiceOdometer}
            • Workshop: ${vm.lastServiceWorkshop}
            • Next Service Due KM: ${vm.nextServiceDueKm}
        """.trimIndent()

        val map = vm.photos.value ?: emptyMap()

        txtPhotos.text = """
            VEHICLE PHOTOS:
            • Front: ${map["photo_front"]}
            • Left: ${map["photo_left"]}
            • Right: ${map["photo_right"]}
            • Back: ${map["photo_back"]}
            • Extra: ${map["photo_extra"]}

            DOCUMENTS:
            • RC: ${map["doc_rc"]}
            • Insurance: ${map["doc_insurance"]}
            • PUC: ${map["doc_puc"]}
            • Permit: ${map["doc_permit"]}
            • Fitness: ${map["doc_fitness"]}
            • Other: ${map["doc_other"]}
        """.trimIndent()
    }

    /** ---------------------------------------------------------
     *  SAVE FINAL VEHICLE RECORD TO FIRESTORE
     *  --------------------------------------------------------- */
    private fun saveToFirestore() {

        val photos = vm.photos.value ?: emptyMap()

        // Validation: Required URLs
        if (!photos.containsKey("photo_front")) {
            Toast.makeText(requireContext(), "Front photo missing!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!photos.containsKey("doc_rc")) {
            Toast.makeText(requireContext(), "RC Document missing!", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setMessage("Saving vehicle…")
            .create()
        dialog.show()

        val vehicleId = vm.vehicleNumber.value ?: "VEH_${System.currentTimeMillis()}"
        val db = FirebaseFirestore.getInstance()

        val finalPayload = vm.getFinalPayload().toMutableMap()
        finalPayload["photos"] = photos
        finalPayload["vehicle_id"] = vehicleId

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.collection("vehicles")
                    .document(vehicleId)
                    .set(finalPayload)
                    .await()

                withContext(Dispatchers.Main) {
                    dialog.dismiss()

                    AlertDialog.Builder(requireContext())
                        .setTitle("Success")
                        .setMessage("Vehicle registered successfully!")
                        .setPositiveButton("OK") { _, _ ->
                            activity?.finish()
                        }
                        .show()
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    dialog.dismiss()

                    AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Firestore error: ${e.message}")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }
}
