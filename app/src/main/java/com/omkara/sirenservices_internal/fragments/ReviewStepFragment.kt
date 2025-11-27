package com.omkara.sirenservices_internal.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
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
import com.omkara.sirenservices_internal.viewmodels.VehicleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

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

        bind(v)
        fillReviewData()

        btnSave.setOnClickListener { trySaveVehicle() }

        return v
    }

    private fun bind(v: View) {
        btnSave = v.findViewById(R.id.btnSave)
        txtVehicle = v.findViewById(R.id.txtReview)
        txtCompliance = v.findViewById(R.id.txtcmpl)
        txtService = v.findViewById(R.id.txtser)
        txtPhotos = v.findViewById(R.id.txtphoto)
    }

    private fun fillReviewData() {

        txtVehicle.text = """
            • Vehicle No: ${vm.vehicleNumber.value}
            • RC No: ${vm.rcNumber.value}
            • Make/Model: ${vm.make.value} / ${vm.model.value}
            • Year: ${vm.manufactureYear.value}
            • Seats: ${vm.seatingCapacity.value}
            • Odometer (Reg): ${vm.odometerAtRegistration}
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
            • Permit No: ${vm.permitNumber.value}
            • Permit Expiry: ${vm.permitExpiry.value}
            • Fitness Expiry: ${vm.fitnessExpiry.value}
        """.trimIndent()

        txtService.text = """
            • Last Service Date: ${vm.lastServiceDate}
            • Last Service Odo: ${vm.lastServiceOdometer}
            • Workshop: ${vm.lastServiceWorkshop}
            • Next Service: ${vm.nextServiceDueKm} KM
        """.trimIndent()

        val payload: MutableMap<String, Any?> = vm.getFinalPayload().toMutableMap()
        val photos: Map<String, String> = vm.photos.value ?: emptyMap()

        Log.d("PHOTOFRAG->R", photos.toString())

        txtPhotos.text = buildString {
            appendLine("Uploaded Photos:")
            appendLine("• Front: ${photos["photo_front"]}")
            appendLine("• Back: ${photos["photo_back"]}")
            appendLine("• Left: ${photos["photo_left"]}")
            appendLine("• Right: ${photos["photo_right"]}")
            appendLine("• Extra: ${photos["photo_extra"]}")
            appendLine("• RC: ${photos["doc_rc"]}")
            appendLine("• Insurance: ${photos["doc_insurance"]}")
            appendLine("• PUC: ${photos["doc_puc"]}")
            appendLine("• Permit: ${photos["doc_permit"]}")
            appendLine("• Fitness: ${photos["doc_fitness"]}")
            appendLine("• Other Doc: ${photos["doc_other"]}")
        }

    }

    private fun validateBeforeSave(): Boolean {

        if (vm.vehicleNumber.value.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Vehicle number missing!", Toast.LENGTH_SHORT).show()
            return false
        }

        val photos = vm.photos.value ?: emptyMap()

        if (!photos.containsKey("photo_front")) {
            Toast.makeText(requireContext(), "Front photo required!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!photos.containsKey("doc_rc")) {
            Toast.makeText(requireContext(), "RC Document required!", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun trySaveVehicle() {

        if (!validateBeforeSave()) return

        val dialog = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setMessage("Saving vehicle…")
            .create()
        dialog.show()

        val payload: MutableMap<String, Any?> = vm.getFinalPayload().toMutableMap()
        payload["photos"] = vm.photos.value ?: emptyMap<String, String>()
        payload["vehicle_id"] = vm.vehicleNumber.value

        val vehicleId = vm.vehicleNumber.value!!.trim()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                FirebaseFirestore.getInstance()
                    .collection("vehicles")
                    .document(vehicleId)
                    .set(payload)
                    .await()

                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    showSuccess()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showSuccess() {
        AlertDialog.Builder(requireContext())
            .setTitle("Success")
            .setMessage("Vehicle registered successfully!")
            .setPositiveButton("OK") { _, _ -> activity?.finish() }
            .show()
    }
}
