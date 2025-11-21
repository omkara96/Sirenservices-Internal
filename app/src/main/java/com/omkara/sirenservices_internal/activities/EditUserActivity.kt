package com.omkara.sirenservices_internal.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.omkara.sirenservices_internal.R
import android.app.DatePickerDialog
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar

class EditUserActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    private lateinit var edtFirstName: TextInputEditText
    private lateinit var edtMiddleName: TextInputEditText
    private lateinit var edtLastName: TextInputEditText
    private lateinit var edtDob: TextInputEditText
    private lateinit var edtMobile: TextInputEditText
    private lateinit var edtEmail: TextInputEditText
    private lateinit var edtAddress: TextInputEditText

    private lateinit var autoRole: AutoCompleteTextView
    private lateinit var autoStatus: AutoCompleteTextView

    private lateinit var btnUpdateUser: MaterialButton

    private lateinit var layoutEmail: TextInputLayout
    private lateinit var layoutDob: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        firestore = FirebaseFirestore.getInstance()

        userId = intent.getStringExtra("USER_ID") ?: ""
        if (userId.isEmpty()) { finish(); return }

        initViews()
        setupDropdowns()
        disableEmailField()
        setupDobPicker()

        loadUser()

        btnUpdateUser.setOnClickListener {
            updateUser()
        }
    }

    private fun initViews() {
        edtFirstName = findViewById(R.id.edtFirstName)
        edtMiddleName = findViewById(R.id.edtMiddleName)
        edtLastName = findViewById(R.id.edtLastName)
        edtDob = findViewById(R.id.edtDob)
        edtMobile = findViewById(R.id.edtMobile)
        edtEmail = findViewById(R.id.edtEmail)
        edtAddress = findViewById(R.id.edtAddress)

        autoRole = findViewById(R.id.autoRole)
        autoStatus = findViewById(R.id.autoStatus)

        btnUpdateUser = findViewById(R.id.btnUpdateUser)

        layoutEmail = findViewById(R.id.layoutEmail)
        layoutDob = findViewById(R.id.layoutDob)
    }

    private fun setupDropdowns() {
        val roles = listOf("Admin", "User", "Manager", "Staff", "Driver")
        autoRole.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, roles))

        val statuses = listOf("Active", "Inactive", "New", "Pending", "Occupied")
        autoStatus.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, statuses))
    }

    private fun disableEmailField() {
        edtEmail.isEnabled = false
        layoutEmail.isEnabled = false
        edtEmail.setTextColor(resources.getColor(R.color.md_theme_outline, null))
    }

    private fun setupDobPicker() {
        edtDob.setOnClickListener {
            val cal = Calendar.getInstance()
            val dp = DatePickerDialog(
                this,
                { _, y, m, d ->
                    edtDob.setText("$d-${m + 1}-$y")
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            dp.show()
        }
    }

    private fun loadUser() {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    edtFirstName.setText(doc.getString("firstName"))
                    edtMiddleName.setText(doc.getString("middleName"))
                    edtLastName.setText(doc.getString("lastName"))
                    edtDob.setText(doc.getString("dob"))
                    edtMobile.setText(doc.getString("mobile"))
                    edtEmail.setText(doc.getString("email"))
                    edtAddress.setText(doc.getString("address"))
                    autoRole.setText(doc.getString("role"), false)
                    autoStatus.setText(doc.getString("status"), false)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateUser() {
        val data = hashMapOf(
            "firstName" to edtFirstName.text.toString().trim(),
            "middleName" to edtMiddleName.text.toString().trim(),
            "lastName" to edtLastName.text.toString().trim(),
            "dob" to edtDob.text.toString().trim(),
            "mobile" to edtMobile.text.toString().trim(),
            "address" to edtAddress.text.toString().trim(),
            "role" to autoRole.text.toString().trim(),
            "status" to autoStatus.text.toString().trim()
            // Email intentionally NOT updated
        )

        firestore.collection("users").document(userId)
            .update(data as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "User updated", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error updating user", Toast.LENGTH_LONG).show()
            }
    }
}
