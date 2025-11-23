package com.omkara.sirenservices_internal.loginsignup

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.textfield.TextInputEditText
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.viewmodels.RegistrationState
import com.omkara.sirenservices_internal.viewmodels.UserRegistrationViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.Calendar

class UserRegistation : AppCompatActivity() {

    private val vm: UserRegistrationViewModel by viewModels()

    // Views
    private lateinit var edtFirstName: TextInputEditText
    private lateinit var edtMiddleName: TextInputEditText
    private lateinit var edtLastName: TextInputEditText
    private lateinit var edtDob: TextInputEditText
    private lateinit var edtMobile: TextInputEditText
    private lateinit var edtEmail: TextInputEditText
    private lateinit var edtAddress: TextInputEditText
    private lateinit var autoRole: AutoCompleteTextView
    private lateinit var autoStatus: AutoCompleteTextView
    private lateinit var driverSection: LinearLayout

    // Driver Fields
    private lateinit var edtLicense: TextInputEditText
    private lateinit var edtAadhar: TextInputEditText
    private lateinit var edtPan: TextInputEditText
    private lateinit var edtAccNo: TextInputEditText
    private lateinit var edtIFSC: TextInputEditText
    private lateinit var edtBankName: TextInputEditText
    private lateinit var edtBranchName: TextInputEditText

    // Images
    private lateinit var btnPickProfile: Button
    private lateinit var imgProfile: ImageView

    private lateinit var btnPickAadharFront: Button
    private lateinit var btnPickAadharBack: Button
    private lateinit var btnPickPan: Button
    private lateinit var btnPickPassbook: Button
    private lateinit var btnPickLicence: Button

    private lateinit var imgAadharFront: ImageView
    private lateinit var imgAadharBack: ImageView
    private lateinit var imgPan: ImageView
    private lateinit var imgPassbook: ImageView
    private lateinit var imgLicence: ImageView

    private lateinit var btnRegister: Button

    // Image URIs
    private var profileUri: Uri? = null
    private var aadharFrontUri: Uri? = null
    private var aadharBackUri: Uri? = null
    private var panUri: Uri? = null
    private var passbookUri: Uri? = null
    private var licenceUri: Uri? = null

    // Request Codes
    private val PICK_PROFILE_REQ = 201
    private val REQ_AADHAR_FRONT = 202
    private val REQ_AADHAR_BACK = 203
    private val REQ_PAN = 204
    private val REQ_PASSBOOK = 205
    private val REQ_LICENCE = 206

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_registation)

        bindViews()
        setupDropdowns()
        setupListeners()
        observeViewModel()
    }

    private fun bindViews() {
        edtFirstName = findViewById(R.id.edtFirstName)
        edtMiddleName = findViewById(R.id.edtMiddleName)
        edtLastName = findViewById(R.id.edtLastName)
        edtDob = findViewById(R.id.edtDob)
        edtMobile = findViewById(R.id.edtMobile)
        edtEmail = findViewById(R.id.edtEmail)
        edtAddress = findViewById(R.id.edtAddress)
        autoRole = findViewById(R.id.autoRole)
        autoStatus = findViewById(R.id.autoStatus)
        driverSection = findViewById(R.id.driverSection)

        edtLicense = findViewById(R.id.edtLicense)
        edtAadhar = findViewById(R.id.edtAadhar)
        edtPan = findViewById(R.id.edtPan)
        edtAccNo = findViewById(R.id.edtAccNo)
        edtIFSC = findViewById(R.id.edtIFSC)
        edtBankName = findViewById(R.id.edtBankName)
        edtBranchName = findViewById(R.id.edtBranchName)

        btnPickProfile = findViewById(R.id.btnPickProfile)
        imgProfile = findViewById(R.id.imgProfilePhoto)
        btnRegister = findViewById(R.id.btnRegisterUser)

        btnPickAadharFront = findViewById(R.id.btnPickAadharFront)
        btnPickAadharBack = findViewById(R.id.btnPickAadharBack)
        btnPickPan = findViewById(R.id.btnPickPan)
        btnPickPassbook = findViewById(R.id.btnPickPassbook)
        btnPickLicence = findViewById(R.id.btnPickLicence)

        imgAadharFront = findViewById(R.id.imgAadharFrontPreview)
        imgAadharBack = findViewById(R.id.imgAadharBackPreview)
        imgPan = findViewById(R.id.imgPanPreview)
        imgPassbook = findViewById(R.id.imgPassbookPreview)
        imgLicence = findViewById(R.id.imgLicencePreview)
    }

    private fun setupDropdowns() {
        autoRole.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1,
                listOf("owner", "driver", "staff", "operator", "mechanic"))
        )
        autoRole.setOnItemClickListener { _, _, pos, _ ->
            toggleDriverSection(autoRole.text.toString() == "driver")
        }

        autoStatus.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1,
                listOf("active", "inactive"))
        )
    }

    private fun setupListeners() {

        edtDob.setOnClickListener { showDobPicker() }

        btnPickProfile.setOnClickListener { pickImage(PICK_PROFILE_REQ) }
        btnPickAadharFront.setOnClickListener { pickImage(REQ_AADHAR_FRONT) }
        btnPickAadharBack.setOnClickListener { pickImage(REQ_AADHAR_BACK) }
        btnPickPan.setOnClickListener { pickImage(REQ_PAN) }
        btnPickPassbook.setOnClickListener { pickImage(REQ_PASSBOOK) }
        btnPickLicence.setOnClickListener { pickImage(REQ_LICENCE) }

        btnRegister.setOnClickListener { submitForm() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            vm.state.collectLatest { st ->
                when (st) {
                    is RegistrationState.Loading -> {
                        btnRegister.isEnabled = false
                        btnRegister.text = "Saving..."
                    }
                    is RegistrationState.Success -> {
                        Toast.makeText(this@UserRegistation, "Saved!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is RegistrationState.Error -> {
                        btnRegister.isEnabled = true
                        btnRegister.text = "Save User"
                        Toast.makeText(this@UserRegistation, st.message, Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun showDobPicker() {
        val cal = Calendar.getInstance()
        val dp = DatePickerDialog(
            this,
            { _, y, m, d -> edtDob.setText(String.format("%02d-%02d-%04d", d, m + 1, y)) },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dp.datePicker.maxDate = System.currentTimeMillis()
        dp.show()
    }

    private fun pickImage(req: Int) {
        ImagePicker.with(this)
            .compress(1024)
            .maxResultSize(1600, 1600)
            .start(req)
    }

    override fun onActivityResult(req: Int, result: Int, data: Intent?) {
        super.onActivityResult(req, result, data)
        if (result != Activity.RESULT_OK || data == null) return
        val uri = data.data ?: return

        when (req) {
            PICK_PROFILE_REQ -> {
                profileUri = uri; imgProfile.load(uri)
            }
            REQ_AADHAR_FRONT -> {
                aadharFrontUri = uri; imgAadharFront.load(uri)
            }
            REQ_AADHAR_BACK -> {
                aadharBackUri = uri; imgAadharBack.load(uri)
            }
            REQ_PAN -> {
                panUri = uri; imgPan.load(uri)
            }
            REQ_PASSBOOK -> {
                passbookUri = uri; imgPassbook.load(uri)
            }
            REQ_LICENCE -> {
                licenceUri = uri; imgLicence.load(uri)
            }
        }
    }

    private fun toggleDriverSection(show: Boolean) {
        driverSection.visibility = if (show) LinearLayout.VISIBLE else LinearLayout.GONE
    }

    private fun submitForm() {

        val firstName = edtFirstName.text.toString().trim()
        val mobile = edtMobile.text.toString().trim()
        val role = autoRole.text.toString().trim()

        if (firstName.isEmpty() || mobile.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Name, Mobile & Role required", Toast.LENGTH_SHORT).show()
            return
        }

        val docInputs = mutableMapOf<String, suspend () -> InputStream?>()

        if (aadharFrontUri != null)
            docInputs["aadharFront"] = { contentResolver.openInputStream(aadharFrontUri!!) }

        if (aadharBackUri != null)
            docInputs["aadharBack"] = { contentResolver.openInputStream(aadharBackUri!!) }

        if (panUri != null)
            docInputs["pan"] = { contentResolver.openInputStream(panUri!!) }

        if (passbookUri != null)
            docInputs["passbook"] = { contentResolver.openInputStream(passbookUri!!) }

        if (licenceUri != null)
            docInputs["licence"] = { contentResolver.openInputStream(licenceUri!!) }

        val profileProvider: suspend () -> InputStream? = {
            profileUri?.let { contentResolver.openInputStream(it) }
        }

        vm.registerUser(
            userId = null,
            firstName = firstName,
            middleName = edtMiddleName.text.toString(),
            lastName = edtLastName.text.toString(),
            dob = edtDob.text.toString(),
            mobile = mobile,
            email = edtEmail.text.toString(),
            address = edtAddress.text.toString(),
            role = role,
            status = autoStatus.text.toString(),
            profileUri = profileUri,
            profileInputStreamProvider = profileProvider,
            isDriver = role == "driver",
            licenseNo = edtLicense.text.toString(),
            aadharNo = edtAadhar.text.toString(),
            panNo = edtPan.text.toString(),
            accountNo = edtAccNo.text.toString(),
            ifsc = edtIFSC.text.toString(),
            bankName = edtBankName.text.toString(),
            branchName = edtBranchName.text.toString(),
            documentInputs = if (docInputs.isEmpty()) null else docInputs
        )
    }
}
