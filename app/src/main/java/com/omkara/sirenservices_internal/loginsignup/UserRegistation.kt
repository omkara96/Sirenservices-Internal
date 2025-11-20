package com.omkara.sirenservices_internal.loginsignup

import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hbb20.CountryCodePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R

data class User(
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val mobile: String,
    val email: String,
    val role: String,
    val status: String
)

class UserRegistation : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    private lateinit var fname: TextInputEditText
    private lateinit var mname: TextInputEditText
    private lateinit var lname: TextInputEditText
    private lateinit var uphone: TextInputEditText
    private lateinit var uemail: TextInputEditText
    private lateinit var ccp: CountryCodePicker
    private lateinit var autoRole: AutoCompleteTextView
    private lateinit var autoStatus: AutoCompleteTextView
    private lateinit var btnRegisterUser: MaterialButton

    private lateinit var rFname: TextInputLayout
    private lateinit var rLname: TextInputLayout
    private lateinit var rPhone: TextInputLayout
    private lateinit var rEmail: TextInputLayout
    private lateinit var dropdownRole: TextInputLayout
    private lateinit var dropdownStatus: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_registation)

        // Handle system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        fname = findViewById(R.id.edtFirstName)
        mname = findViewById(R.id.edtMiddleName)
        lname = findViewById(R.id.edtLastName)
     //   uphone = findViewById(R.id.edtMobile)
        uemail = findViewById(R.id.edtEmail)
       // ccp = findViewById(R.id.ccp)
        autoRole = findViewById(R.id.autoRole)
        autoStatus = findViewById(R.id.autoStatus)
        btnRegisterUser = findViewById(R.id.btnRegisterUser)

        rFname = findViewById(R.id.rFname)
        rLname = findViewById(R.id.rLname)
        rPhone = findViewById(R.id.rPhone)
        rEmail = findViewById(R.id.rEmail)
        dropdownRole = findViewById(R.id.dropdownRole)
        dropdownStatus = findViewById(R.id.dropdownStatus)

        uphone = findViewById(R.id.edtMobile)
        ccp = findViewById(R.id.ccp)

// Fix: link CountryCodePicker with EditText
        ccp.registerCarrierNumberEditText(uphone)

        firestore = FirebaseFirestore.getInstance()

        // Setup Role dropdown
        val roles = listOf("Admin", "User", "Manager", "Staff", "Driver")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        autoRole.setAdapter(roleAdapter)

        // Setup Status dropdown
        val statuses = listOf("Active", "Inactive", "New", "Pending")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses)
        autoStatus.setAdapter(statusAdapter)

        // Button click listener
        btnRegisterUser.setOnClickListener {
            if (validateFields()) {
                registerUser()
            }
        }
    }

    private fun validateFields(): Boolean {
        val firstName = fname.text.toString().trim()
        val lastName = lname.text.toString().trim()
        val mobile = uphone.text.toString().trim()
        val email = uemail.text.toString().trim()
        val role = autoRole.text.toString().trim()
        val status = autoStatus.text.toString().trim()

        if (firstName.isEmpty()) {
            rFname.error = "First name is required"
            return false
        } else rFname.error = null

        if (lastName.isEmpty()) {
            rLname.error = "Last name is required"
            return false
        } else rLname.error = null

        if (mobile.isEmpty()) {
            rPhone.error = "Mobile number is required"
            return false
        } else if (mobile.length < 6) {
            rPhone.error = "Enter a valid mobile number"
            return false
        } else rPhone.error = null

        if (email.isEmpty()) {
            rEmail.error = "Email is required"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            rEmail.error = "Enter a valid email"
            return false
        } else rEmail.error = null

        if (role.isEmpty()) {
            dropdownRole.error = "Select a role"
            return false
        } else dropdownRole.error = null

        if (status.isEmpty()) {
            dropdownStatus.error = "Select a status"
            return false
        } else dropdownStatus.error = null

        return true
    }

    private fun registerUser() {
        val firstName = fname.text.toString().trim()
        val middleName = mname.text.toString().trim()
        val lastName = lname.text.toString().trim()
        val mobile = ccp.fullNumberWithPlus
        val email = uemail.text.toString().trim()
        val role = autoRole.text.toString().trim()
        val status = autoStatus.text.toString().trim()

        val user = User(firstName, middleName, lastName, mobile, email, role, status)

        // Show progress dialog
        val progressDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Registering User")
            .setMessage("Please wait...")
            .setCancelable(false)
            .create()
        progressDialog.show()

        firestore.collection("users")
            .add(user)
            .addOnSuccessListener {
                progressDialog.dismiss() // Hide progress

                // Show success dialog
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Success")
                    .setMessage("User registered successfully!")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        finish() // Go back to previous activity
                    }
                    .setCancelable(false)
                    .show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss() // Hide progress
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Failed to register user: ${e.message}")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
    }


    private fun clearFields() {
        fname.text?.clear()
        mname.text?.clear()
        lname.text?.clear()
        uphone.text?.clear()
        uemail.text?.clear()
        autoRole.text = null
        autoStatus.text = null
    }
}
