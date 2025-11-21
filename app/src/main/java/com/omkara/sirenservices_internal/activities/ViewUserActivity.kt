package com.omkara.sirenservices_internal.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.omkara.sirenservices_internal.R
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import android.widget.TextView

class ViewUserActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    private lateinit var tvFullName: TextView
    private lateinit var tvDob: TextView
    private lateinit var tvMobile: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnEditUser: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_user)

        firestore = FirebaseFirestore.getInstance()

        // Incoming document ID
        userId = intent.getStringExtra("USER_ID") ?: ""

        if (userId.isEmpty()) {
            finish()
            return
        }



        // Initialize views
        tvFullName = findViewById(R.id.tvFullName)
        tvDob = findViewById(R.id.tvDob)
        tvMobile = findViewById(R.id.tvMobile)
        tvEmail = findViewById(R.id.tvEmail)
        tvAddress = findViewById(R.id.tvAddress)
        tvStatus = findViewById(R.id.tvStatus)
        btnEditUser = findViewById(R.id.btnEditUser)

        loadUser()

        btnEditUser.setOnClickListener {
            val intent = Intent(this, EditUserActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
    }

    private fun loadUser() {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val fullName = "${doc.getString("firstName")} ${doc.getString("middleName")} ${doc.getString("lastName")}"

                    tvFullName.text = fullName
                    tvDob.text = doc.getString("dob") ?: "-"
                    tvMobile.text = doc.getString("mobile") ?: "-"
                    tvEmail.text = doc.getString("email") ?: "-"
                    tvAddress.text = doc.getString("address") ?: "-"
                    tvStatus.text = doc.getString("status") ?: "-"
                }
            }
            .addOnFailureListener {
                tvFullName.text = "Failed to load"
            }
    }
}
