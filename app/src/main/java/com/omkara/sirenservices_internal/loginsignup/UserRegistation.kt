package com.omkara.sirenservices_internal.loginsignup

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hbb20.CountryCodePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import java.util.*

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

    private lateinit var edtDob: TextInputEditText
    private lateinit var layoutDob: TextInputLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_registation)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        fname = findViewById(R.id.edtFirstName)
        mname = findViewById(R.id.edtMiddleName)
        lname = findViewById(R.id.edtLastName)
        uemail = findViewById(R.id.edtEmail)
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
        ccp.registerCarrierNumberEditText(uphone)

        firestore = FirebaseFirestore.getInstance()

        edtDob = findViewById(R.id.edtDob)
        layoutDob = findViewById(R.id.layoutDob)

        edtDob.setOnClickListener {
            showDobPicker()
        }

        layoutDob.setOnClickListener {
            showDobPicker()
        }


        // Setup Role dropdown
        val roles = listOf("Driver", "Third Party Driver", "User", "Staff")
        autoRole.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles))

        // Setup Status dropdown
        val statuses = listOf("Active", "Inactive", "New", "Pending")
        autoStatus.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses))

        btnRegisterUser.setOnClickListener {
            if (validateFields()) {
                checkIfUserExists()
            }
        }
    }

    private fun showDobPicker() {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dp = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val dob = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                edtDob.setText(dob)
            },
            year,
            month,
            day
        )

        // Max date today (no future DOB)
        dp.datePicker.maxDate = System.currentTimeMillis()

        dp.show()
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

    /**
     * NEW: Check if user already exists (email or mobile)
     */
    private fun checkIfUserExists() {
        val mobileFull = ccp.fullNumberWithPlus
        val email = uemail.text.toString().trim()

        val progress = loadingDialog("Checking user...")
        progress.show()

        // Query by mobile OR email
        firestore.collection("users")
            .whereEqualTo("mobile", mobileFull)
            .get()
            .addOnSuccessListener { snap1 ->

                firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { snap2 ->

                        progress.dismiss()

                        if (!snap1.isEmpty || !snap2.isEmpty) {
                            showUserExistsDialog()
                        } else {
                            registerUser()
                        }
                    }
            }
            .addOnFailureListener {
                progress.dismiss()
                Toast.makeText(this, "Check failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * If duplicate user found
     */
    private fun showUserExistsDialog() {
        AlertDialog.Builder(this)
            .setTitle("User Already Exists")
            .setMessage("A user with the same email or mobile number already exists. Try with Different Email or Mobile.")
            .setCancelable(false)
            .setPositiveButton("OK") { d, _ ->
                d.dismiss()
               // finish()  // Go back
            }.show()
    }



    private fun registerUser() {
        val progressDialog = loadingDialog("Registering user...")
        progressDialog.show()

        val user = hashMapOf(
            "firstName" to fname.text.toString().trim(),
            "middleName" to mname.text.toString().trim(),
            "lastName" to lname.text.toString().trim(),
            "mobile" to ccp.fullNumberWithPlus,
            "email" to uemail.text.toString().trim(),
            "role" to autoRole.text.toString().trim(),
            "status" to autoStatus.text.toString().trim(),
            "created_at" to Date(),
            "updated_at" to Date()
        )

        firestore.collection("users")
            .add(user)
            .addOnSuccessListener {
                progressDialog.dismiss()

                AlertDialog.Builder(this)
                    .setTitle("Success")
                    .setMessage("User registered successfully!")
                    .setCancelable(false)
                    .setPositiveButton("OK") { d, _ ->
                        d.dismiss()
                        finish()
                    }.show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Failed to register user: ${e.message}")
                    .setPositiveButton("OK", null)
                    .show()
            }
    }

    private fun loadingDialog(msg: String): AlertDialog {
        return AlertDialog.Builder(this)
            .setTitle(msg)
            .setMessage("Please wait…")
            .setCancelable(false)
            .create()
    }
}
