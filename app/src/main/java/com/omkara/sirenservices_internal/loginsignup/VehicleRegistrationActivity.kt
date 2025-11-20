package com.omkara.sirenservices_internal.loginsignup

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.VehiclePagerAdapter
import com.omkara.sirenservices_internal.models.VehicleModel
import java.util.Calendar




class VehicleRegistrationActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var btnSubmit: MaterialButton
    private lateinit var progressDialog: AlertDialog

    private val db = FirebaseFirestore.getInstance()
    lateinit var pagerAdapter: VehiclePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_registration)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        btnSubmit = findViewById(R.id.btnSubmit)

        progressDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_progress)
            .setCancelable(false)
            .create()

        pagerAdapter = VehiclePagerAdapter(this)
        viewPager.adapter = pagerAdapter

        val tabTitles = arrayOf("Vehicle", "Compliance", "Service")
        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = tabTitles[pos]
        }.attach()

        btnSubmit.setOnClickListener {
            submitForm()
        }
    }

    private fun submitForm() {
        // ask fragments to validate
        val vehicleFragment = pagerAdapter.getVehicleFragment()
        val compFragment = pagerAdapter.getComplianceFragment()
        val servFragment = pagerAdapter.getServiceFragment()

        if (!vehicleFragment.validate()) {
            viewPager.currentItem = 0
            return
        }
        if (!compFragment.validate()) {
            viewPager.currentItem = 1
            return
        }
        if (!servFragment.validate()) {
            viewPager.currentItem = 2
            return
        }

        // gather data
        val map = HashMap<String, Any?>()
        map.putAll(vehicleFragment.getData())
        map.putAll(compFragment.getData())
        map.putAll(servFragment.getData())

        // basic vehicle model mapping
        val model = VehicleModel(
            vehicle_number = map["vehicle_number"] as String? ?: "",
            vehicle_type = map["vehicle_type"] as String? ?: "",
            make = map["make"] as String? ?: "",
            model = map["model"] as String? ?: "",
            manufacture_year = (map["manufacture_year"] as? Int),
            seating_capacity = (map["seating_capacity"] as? Int),
            odometer_at_registration = (map["odometer_at_registration"] as? Double),
            fuel_type = map["fuel_type"] as? String ?: "",
            transmission = map["transmission"] as? String ?: "",
            status = map["status"] as? String ?: "ACTIVE",
            owner_type = map["owner_type"] as? String ?: "OWN",
            owner_user_id = map["owner_user_id"] as? String,
            chassis_number = map["chassis_number"] as? String ?: "",
            engine_number = map["engine_number"] as? String ?: "",
            insurance_provider = map["insurance_provider"] as? String ?: "",
            insurance_number = map["insurance_number"] as? String ?: "",
            insurance_start = map["insurance_start"] as? String,
            insurance_end = map["insurance_end"] as? String,
            puc_number = map["puc_number"] as? String ?: "",
            puc_start = map["puc_start"] as? String,
            puc_end = map["puc_end"] as? String,
            fitness_number = map["fitness_number"] as? String ?: "",
            fitness_expiry = map["fitness_expiry"] as? String,
            permit_number = map["permit_number"] as? String ?: "",
            permit_expiry = map["permit_expiry"] as? String,
            last_service_date = map["last_service_date"] as? String,
            last_service_odometer = (map["last_service_odometer"] as? Double),
            last_service_workshop = map["last_service_workshop"] as? String,
            last_service_notes = map["last_service_notes"] as? String,
            next_service_due_km = (map["next_service_due_km"] as? Double),
            created_at = null,
            updated_at = null
        )

        // Show progress
        progressDialog.show()

        // Save to Firestore
        db.collection("vehicles")
            .add(model)
            .addOnSuccessListener {
                // set timestamps
                it.update("created_at", FieldValue.serverTimestamp())
                it.update("updated_at", FieldValue.serverTimestamp())
                progressDialog.dismiss()
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Success")
                    .setMessage("Vehicle registered successfully.")
                    .setPositiveButton("OK") { d, _ ->
                        d.dismiss()
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
