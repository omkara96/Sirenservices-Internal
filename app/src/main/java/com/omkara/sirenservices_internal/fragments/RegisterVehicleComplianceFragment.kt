package com.omkara.sirenservices_internal.fragments

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.S3Uploader
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity
import com.omkara.sirenservices_internal.models.*
import com.omkara.sirenservices_internal.viewmodels.VehicleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RegisterVehicleComplianceFragment : Fragment() {

    private val vm: VehicleViewModel by activityViewModels()

    // --- Insurance
    private lateinit var edtInsuranceProvider: TextInputEditText
    private lateinit var edtInsuranceCertificateNo: TextInputEditText
    private lateinit var edtInsuranceNumber: TextInputEditText
    private lateinit var edtInsuranceType: TextInputEditText
    private lateinit var edtInsurancePremium: TextInputEditText
    private lateinit var edtInsuranceStart: TextInputEditText
    private lateinit var edtInsuranceEnd: TextInputEditText
    private lateinit var btnUploadInsurancePdf: View

    // --- PUC
    private lateinit var edtPucProvider: TextInputEditText
    private lateinit var edtPucCertificateNo: TextInputEditText
    private lateinit var edtPucNumber: TextInputEditText
    private lateinit var edtPucPremium: TextInputEditText
    private lateinit var edtPucStart: TextInputEditText
    private lateinit var edtPucEnd: TextInputEditText
    private lateinit var btnUploadPucPdf: View

    // --- Permit
    private lateinit var edtPermitProvider: TextInputEditText
    private lateinit var edtPermitCertificateNo: TextInputEditText
    private lateinit var edtPermitNumber: TextInputEditText
    private lateinit var edtPermitType: TextInputEditText
    private lateinit var edtPermitPremium: TextInputEditText
    private lateinit var edtPermitStart: TextInputEditText
    private lateinit var edtPermitExpiry: TextInputEditText
    private lateinit var btnUploadPermitPdf: View

    // --- Fitness
    private lateinit var edtFitnessProvider: TextInputEditText
    private lateinit var edtFitnessCertificateNo: TextInputEditText
    private lateinit var edtFitnessNumber: TextInputEditText
    private lateinit var edtFitnessType: TextInputEditText
    private lateinit var edtFitnessPremium: TextInputEditText
    private lateinit var edtFitnessValidFrom: TextInputEditText
    private lateinit var edtFitnessValidTill: TextInputEditText
    private lateinit var btnUploadFitnessPdf: View

    // Validation layouts
    private lateinit var lytInsuranceProvider: TextInputLayout
    private lateinit var lytInsuranceNumber: TextInputLayout
    private lateinit var lytInsurancePremium: TextInputLayout
    private lateinit var lytPucNumber: TextInputLayout
    private lateinit var lytPermitNumber: TextInputLayout
    private lateinit var lytPermitType: TextInputLayout

    // FABs
    private lateinit var btnPrev: FloatingActionButton
    private lateinit var btnNext: FloatingActionButton

    // PDF pickers
    private val pickInsurancePdf =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { onPdfPicked("insurance", it, "insurance.pdf") }
        }

    private val pickPucPdf =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { onPdfPicked("puc", it, "puc.pdf") }
        }

    private val pickPermitPdf =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { onPdfPicked("permit", it, "permit.pdf") }
        }

    private val pickFitnessPdf =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { onPdfPicked("fitness", it, "fitness.pdf") }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_cmpliance, container, false)

        bind(v)
        setupDatePickers()
        setupErrorClearListeners()
        fixFabInsets(v)
        prefillIfExists()

        btnPrev.setOnClickListener { (activity as? VehicleRegistrationActivity)?.prevStep() }

        btnNext.setOnClickListener {
            if (validate()) {
                saveToViewModel()
                (activity as? VehicleRegistrationActivity)?.nextStep()
            }
        }

        btnUploadInsurancePdf.setOnClickListener { pickInsurancePdf.launch(arrayOf("application/pdf")) }
        btnUploadPucPdf.setOnClickListener { pickPucPdf.launch(arrayOf("application/pdf")) }
        btnUploadPermitPdf.setOnClickListener { pickPermitPdf.launch(arrayOf("application/pdf")) }
        btnUploadFitnessPdf.setOnClickListener { pickFitnessPdf.launch(arrayOf("application/pdf")) }

        return v
    }

    private fun bind(v: View) {
        // Insurance
        edtInsuranceProvider = v.findViewById(R.id.edtInsuranceProvider)
        edtInsuranceCertificateNo = v.findViewById(R.id.edtInsuranceCertificateNo)
        edtInsuranceNumber = v.findViewById(R.id.edtInsuranceNumber)
        edtInsuranceType = v.findViewById(R.id.edtInsuranceType)
        edtInsurancePremium = v.findViewById(R.id.edtInsurancePremium)
        edtInsuranceStart = v.findViewById(R.id.edtInsuranceStart)
        edtInsuranceEnd = v.findViewById(R.id.edtInsuranceEnd)
        btnUploadInsurancePdf = v.findViewById(R.id.btnUploadInsurancePdf)

        // PUC
        edtPucProvider = v.findViewById(R.id.edtPucProvider)
        edtPucCertificateNo = v.findViewById(R.id.edtPucCertificateNo)
        edtPucNumber = v.findViewById(R.id.edtPucNumber)
        edtPucPremium = v.findViewById(R.id.edtPucPremium)
        edtPucStart = v.findViewById(R.id.edtPucStart)
        edtPucEnd = v.findViewById(R.id.edtPucEnd)
        btnUploadPucPdf = v.findViewById(R.id.btnUploadPucPdf)

        // Permit
        edtPermitProvider = v.findViewById(R.id.edtPermitProvider)
        edtPermitCertificateNo = v.findViewById(R.id.edtPermitCertificateNo)
        edtPermitNumber = v.findViewById(R.id.edtPermitNumber)
        edtPermitType = v.findViewById(R.id.edtPermitType)
        edtPermitPremium = v.findViewById(R.id.edtPermitPremium)
        edtPermitStart = v.findViewById(R.id.edtPermitStart)
        edtPermitExpiry = v.findViewById(R.id.edtPermitExpiry)
        btnUploadPermitPdf = v.findViewById(R.id.btnUploadPermitPdf)

        // Fitness
        edtFitnessProvider = v.findViewById(R.id.edtFitnessProvider)
        edtFitnessCertificateNo = v.findViewById(R.id.edtFitnessCertificateNo)
        edtFitnessNumber = v.findViewById(R.id.edtFitnessNumber)
        edtFitnessType = v.findViewById(R.id.edtFitnessType)
        edtFitnessPremium = v.findViewById(R.id.edtFitnessPremium)
        edtFitnessValidFrom = v.findViewById(R.id.edtFitnessValidFrom)
        edtFitnessValidTill = v.findViewById(R.id.edtFitnessValidTill)
        btnUploadFitnessPdf = v.findViewById(R.id.btnUploadFitnessPdf)

        // Layouts
        lytInsuranceProvider = v.findViewById(R.id.lytInsuranceProvider)
        lytInsuranceNumber = v.findViewById(R.id.lytInsuranceNumber)
        lytInsurancePremium = v.findViewById(R.id.lytInsurancePremium)
        lytPucNumber = v.findViewById(R.id.lytPucNumber)
        lytPermitNumber = v.findViewById(R.id.lytPermitNumber)
        lytPermitType = v.findViewById(R.id.lytPermitType)

        btnPrev = v.findViewById(R.id.btnPrev)
        btnNext = v.findViewById(R.id.btnNext)
    }

    private fun fixFabInsets(v: View) {
        ViewCompat.setOnApplyWindowInsetsListener(v) { _, insets ->
            val bottomInset =
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            btnNext.translationY = -bottomInset.toFloat()
            btnPrev.translationY = -bottomInset.toFloat()
            insets
        }
    }

    private fun setupDatePickers() {
        val dateFields = listOf(
            edtInsuranceStart, edtInsuranceEnd,
            edtPucStart, edtPucEnd,
            edtPermitStart, edtPermitExpiry,
            edtFitnessValidFrom, edtFitnessValidTill
        )

        dateFields.forEach { field ->
            field.isFocusable = false
            field.setOnClickListener { showDatePicker(field) }
        }
    }

    private fun showDatePicker(target: EditText) {
        val cal = Calendar.getInstance()
        val dp = DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                target.setText(String.format("%02d-%02d-%04d", d, m + 1, y))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dp.show()
    }

    private fun setupErrorClearListeners() {
        val layouts = listOf(
            lytInsuranceProvider,
            lytInsuranceNumber,
            lytInsurancePremium,
            lytPucNumber,
            lytPermitNumber,
            lytPermitType
        )

        layouts.forEach { layout ->
            layout.editText?.setOnFocusChangeListener { _, _ ->
                layout.error = null
            }
        }
    }

    private fun prefillIfExists() {
        val cmpl = vm.compliance.value ?: return

        cmpl.insurance.current?.let {
            edtInsuranceProvider.setText(it.provider ?: "")
            edtInsuranceCertificateNo.setText(it.certificate_no ?: "")
            edtInsuranceNumber.setText(it.number ?: "")
            edtInsuranceType.setText(it.type ?: "")
            edtInsurancePremium.setText(it.premium?.toString() ?: "")
            edtInsuranceStart.setText(it.valid_from ?: "")
            edtInsuranceEnd.setText(it.valid_till ?: "")
        }

        cmpl.puc.current?.let {
            edtPucProvider.setText(it.provider ?: "")
            edtPucCertificateNo.setText(it.certificate_no ?: "")
            edtPucNumber.setText(it.number ?: "")
            edtPucPremium.setText(it.premium?.toString() ?: "")
            edtPucStart.setText(it.valid_from ?: "")
            edtPucEnd.setText(it.valid_till ?: "")
        }

        cmpl.permit.current?.let {
            edtPermitProvider.setText(it.provider ?: "")
            edtPermitCertificateNo.setText(it.certificate_no ?: "")
            edtPermitNumber.setText(it.number ?: "")
            edtPermitType.setText(it.type ?: "")
            edtPermitPremium.setText(it.premium?.toString() ?: "")
            edtPermitStart.setText(it.valid_from ?: "")
            edtPermitExpiry.setText(it.valid_till ?: "")
        }

        cmpl.fitness.current?.let {
            edtFitnessProvider.setText(it.provider ?: "")
            edtFitnessCertificateNo.setText(it.certificate_no ?: "")
            edtFitnessNumber.setText(it.number ?: "")
            edtFitnessType.setText(it.type ?: "")
            edtFitnessPremium.setText(it.premium?.toString() ?: "")
            edtFitnessValidFrom.setText(it.valid_from ?: "")
            edtFitnessValidTill.setText(it.valid_till ?: "")
        }
    }

    private fun validate(): Boolean {
        var ok = true

        if (edtInsuranceProvider.text.isNullOrBlank()) {
            lytInsuranceProvider.error = "Required"; ok = false
        }

        if (edtInsuranceNumber.text.isNullOrBlank()) {
            lytInsuranceNumber.error = "Required"; ok = false
        }

        if (edtInsurancePremium.text.isNullOrBlank()) {
            lytInsurancePremium.error = "Required"; ok = false
        }

        if (edtPucNumber.text.isNullOrBlank()) {
            lytPucNumber.error = "Required"; ok = false
        }

        if (edtPermitNumber.text.isNullOrBlank()) {
            lytPermitNumber.error = "Required"; ok = false
        }

        if (edtPermitType.text.isNullOrBlank()) {
            lytPermitType.error = "Required"; ok = false
        }

        if (!checkDateOrder(edtInsuranceStart, edtInsuranceEnd, "Insurance expiry must be after start")) ok = false
        if (!checkDateOrder(edtPucStart, edtPucEnd, "PUC expiry must be after start")) ok = false
        if (!checkDateOrder(edtPermitStart, edtPermitExpiry, "Permit expiry must be after start")) ok = false
        if (!checkDateOrder(edtFitnessValidFrom, edtFitnessValidTill, "Fitness expiry must be after start")) ok = false

        return ok
    }

    private fun checkDateOrder(start: TextInputEditText, end: TextInputEditText, msg: String): Boolean {
        val s = start.text.toString()
        val e = end.text.toString()
        if (s.isEmpty() || e.isEmpty()) return true

        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val d1 = sdf.parse(s)
            val d2 = sdf.parse(e)
            if (d1 != null && d2 != null && d2.before(d1)) {
                end.error = msg
                false
            } else true
        } catch (ex: Exception) {
            end.error = "Invalid date"
            false
        }
    }

    private fun saveToViewModel() {

        val insurance = ComplianceSection(
            current = ComplianceCurrent(
                provider = edtInsuranceProvider.text.toString().trim(),
                certificate_no = edtInsuranceCertificateNo.text.toString().trim(),
                number = edtInsuranceNumber.text.toString().trim(),
                type = edtInsuranceType.text.toString().trim(),
                premium = edtInsurancePremium.text.toString().trim().toDoubleOrNull(),
                valid_from = edtInsuranceStart.text.toString().trim(),
                valid_till = edtInsuranceEnd.text.toString().trim(),
                updated_at = Timestamp.now()
            ),
            history = emptyList()
        )

        val puc = ComplianceSection(
            current = ComplianceCurrent(
                provider = edtPucProvider.text.toString().trim(),
                certificate_no = edtPucCertificateNo.text.toString().trim(),
                number = edtPucNumber.text.toString().trim(),
                premium = edtPucPremium.text.toString().trim().toDoubleOrNull(),
                valid_from = edtPucStart.text.toString().trim(),
                valid_till = edtPucEnd.text.toString().trim(),
                updated_at = Timestamp.now()
            ),
            history = emptyList()
        )

        val permit = ComplianceSection(
            current = ComplianceCurrent(
                provider = edtPermitProvider.text.toString().trim(),
                certificate_no = edtPermitCertificateNo.text.toString().trim(),
                number = edtPermitNumber.text.toString().trim(),
                type = edtPermitType.text.toString().trim(),
                premium = edtPermitPremium.text.toString().trim().toDoubleOrNull(),
                valid_from = edtPermitStart.text.toString().trim(),
                valid_till = edtPermitExpiry.text.toString().trim(),
                updated_at = Timestamp.now()
            ),
            history = emptyList()
        )

        val fitness = ComplianceSection(
            current = ComplianceCurrent(
                provider = edtFitnessProvider.text.toString().trim(),
                certificate_no = edtFitnessCertificateNo.text.toString().trim(),
                number = edtFitnessNumber.text.toString().trim(),
                type = edtFitnessType.text.toString().trim(),
                premium = edtFitnessPremium.text.toString().trim().toDoubleOrNull(),
                valid_from = edtFitnessValidFrom.text.toString().trim(),
                valid_till = edtFitnessValidTill.text.toString().trim(),
                updated_at = Timestamp.now()
            ),
            history = emptyList()
        )

        vm.updateComplianceInfo(
            ComplianceModel(
                insurance = insurance,
                puc = puc,
                permit = permit,
                fitness = fitness
            )
        )

        Toast.makeText(requireContext(), "Compliance saved", Toast.LENGTH_SHORT).show()
    }

    // -------------------------------------------------------------------
    // ---------------------- PDF UPLOAD HANDLING ------------------------
    // -------------------------------------------------------------------

    private fun onPdfPicked(type: String, uri: Uri, fileName: String) {

        val vehicleId = vm.vehicleNumber.value

        if (vehicleId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Vehicle number missing", Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(requireContext(), "Uploading $type PDF…", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val stream = requireContext().contentResolver.openInputStream(uri)
                    ?: throw Exception("Unable to open file")

                val url = S3Uploader.uploadAmbulanceDocument(
                    ambulanceId = vehicleId,
                    fileName = fileName,
                    stream = stream
                )

                // SAVE INTO VIEWMODEL (NOT FIRESTORE)
                vm.setComplianceDocument(type, url)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "$type document uploaded", Toast.LENGTH_SHORT).show()
                }

            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Upload failed: ${ex.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
