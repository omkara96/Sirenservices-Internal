package com.omkara.sirenservices_internal.fragments.details

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.PhotoPagerAdapter
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

class BasicInfoFragment : Fragment() {

    private lateinit var photoPager: ViewPager2
    private lateinit var photoIndicator: WormDotsIndicator

    private lateinit var txtVehicleNumber: TextView
    private lateinit var txtMakeModel: TextView
    private lateinit var txtYear: TextView
    private lateinit var txtFuel: TextView
    private lateinit var txtTransmission: TextView
    private lateinit var txtChassis: TextView
    private lateinit var txtEngine: TextView
    private lateinit var txtOdometer: TextView
    private lateinit var txtVehicleOwnership: TextView
    private lateinit var btnUpdateOdometer: View
    private lateinit var scrollView: NestedScrollView
    private lateinit var txtYearSeats: TextView

    private var vehicleId = ""

    companion object {
        fun newInstance(id: String): BasicInfoFragment {
            val f = BasicInfoFragment()
            f.arguments = Bundle().apply { putString("vehicle_id", id) }
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vehicleId = arguments?.getString("vehicle_id") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_basic_info, container, false)

        photoPager = v.findViewById(R.id.photoPager)
        photoIndicator = v.findViewById(R.id.photoIndicator)

        txtVehicleNumber = v.findViewById(R.id.txtVehicleNumber)
        txtMakeModel = v.findViewById(R.id.txtMakeModel)
        txtYear = v.findViewById(R.id.txtYear)
        txtFuel = v.findViewById(R.id.txtFuel)
        txtTransmission = v.findViewById(R.id.txtTransmission)
        txtChassis = v.findViewById(R.id.txtChassis)
        txtEngine = v.findViewById(R.id.txtEngine)
        txtOdometer = v.findViewById(R.id.txtOdometer)
        txtVehicleOwnership = v.findViewById(R.id.txtvehicleOwnership)
        btnUpdateOdometer = v.findViewById(R.id.btnUpdateOdometer)
        scrollView = v.findViewById(R.id.scrollView)
        txtYearSeats = v.findViewById<TextView>(R.id.txtYearSeats)

        val btnLeft = v.findViewById<ImageView>(R.id.btnLeft)
        val btnRight = v.findViewById<ImageView>(R.id.btnRight)

        loadVehicleInfo()

        btnLeft.setOnClickListener {
            val current = photoPager.currentItem
            if (current > 0) photoPager.currentItem = current - 1
        }

        btnRight.setOnClickListener {
            val current = photoPager.currentItem
            val total = photoPager.adapter?.itemCount ?: 0
            if (current < total - 1) {
                photoPager.currentItem = current + 1
            }
        }

        return v
    }

    private fun loadVehicleInfo() {
        FirebaseFirestore.getInstance()
            .collection("vehicles")
            .document(vehicleId)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) return@addOnSuccessListener

                // ---- Read Nested vehicle_info ----
                val info = doc.get("vehicle_info") as? Map<String, Any> ?: emptyMap()

                txtVehicleNumber.text = info["vehicle_number"]?.toString() ?: "-"
                txtMakeModel.text = "${info["make"] ?: "-"} • ${info["model"] ?: "-"}"
                txtYear.text = "Year: ${info["manufacture_year"] ?: "-"}"
                txtFuel.text = "Fuel: ${info["fuel_type"] ?: "-"}"
                txtTransmission.text = "Transmission: ${info["transmission"] ?: "-"}"
                txtChassis.text = "Chassis No: ${info["chassis_number"] ?: "-"}"
                txtEngine.text = "Engine No: ${info["engine_number"] ?: "-"}"
                txtVehicleOwnership.text = "Ownership: ${info["owner_type"] ?: "-"}"
                txtYearSeats.text="Seating Capacity: ${info["seating_capacity"] ?: "-"}"

                val odo = (info["odometer_at_registration"] as? Number)?.toDouble() ?: 0.0
                txtOdometer.text = "$odo km"

                // ---- Photos ----
                val photos = doc.get("photos") as? Map<String, String> ?: emptyMap()
                val photoList = listOfNotNull(
                    photos["photo_front"],
                    photos["photo_back"],
                    photos["photo_left"],
                    photos["photo_right"],
                    photos["photo_rcbook"]
                )

                setupPhotoSlider(photoList)
                setupSwipeFix()

                btnUpdateOdometer.setOnClickListener {
                    showUpdateOdometerDialog(odo)
                }

            }
    }

    private fun setupPhotoSlider(photoUrls: List<String>) {

        val urls = if (photoUrls.isEmpty()) {
            listOf("android.resource://${requireContext().packageName}/${R.drawable.ic_doc_placeholder}")
        } else photoUrls

        photoPager.adapter = PhotoPagerAdapter(requireContext(), urls)
        photoIndicator.attachTo(photoPager)

        (photoPager.getChildAt(0))?.setOnTouchListener { v, _ ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    private fun setupSwipeFix() {
        val parentVP = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
        val child = photoPager.getChildAt(0)

        child.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            scrollView.requestDisallowInterceptTouchEvent(true)
            parentVP?.isUserInputEnabled = false

            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                parentVP?.isUserInputEnabled = true
            }
            false
        }
    }

    private fun showUpdateOdometerDialog(previous: Double) {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(previous.toInt().toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Update Odometer")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newValue = input.text.toString().toDoubleOrNull()
                if (newValue == null || newValue < previous) {
                    Toast.makeText(requireContext(), "Invalid value!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                updateOdometer(newValue)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateOdometer(value: Double) {
        FirebaseFirestore.getInstance()
            .collection("vehicles")
            .document(vehicleId)
            .update("vehicle_info.odometer_at_registration", value)
            .addOnSuccessListener {
                txtOdometer.text = "$value km"
                Toast.makeText(requireContext(), "Odometer updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
