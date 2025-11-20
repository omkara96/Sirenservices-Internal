package com.omkara.sirenservices_internal.dashboards

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.activities.UserListActivity
import com.omkara.sirenservices_internal.activities.VehicleListActivity
import com.omkara.sirenservices_internal.loginsignup.UserRegistation
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity

class AdminDashboard : AppCompatActivity() {
    private lateinit var btnUserAdd : MaterialButton
    private lateinit var btnVehicleAdd : MaterialButton
    private lateinit var btnManageDriver: MaterialButton
    private lateinit var btnManageVehicle: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnUserAdd = findViewById(R.id.btnAddDriver)
        btnUserAdd.setOnClickListener {
        startActivity(Intent(this, UserRegistation::class.java))
    }
        btnVehicleAdd = findViewById(R.id.btnAddVehicle)
        btnVehicleAdd.setOnClickListener(){
            startActivity(Intent(this, VehicleRegistrationActivity::class.java))

        }

        btnManageDriver = findViewById(R.id.btnManageDriver)
        btnManageDriver.setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }

        btnManageVehicle = findViewById(R.id.btnAddVehicle1)
        btnManageVehicle.setOnClickListener {
            startActivity(Intent(this, VehicleListActivity::class.java))
        }
    }
}