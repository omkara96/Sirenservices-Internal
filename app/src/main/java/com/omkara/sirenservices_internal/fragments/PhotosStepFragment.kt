package com.omkara.sirenservices_internal.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.S3Uploader
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity
import com.omkara.sirenservices_internal.viewmodels.VehicleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotosStepFragment : Fragment() {

    private val vm: VehicleViewModel by activityViewModels()

    private lateinit var btnPrev: FloatingActionButton
    private lateinit var btnNext: FloatingActionButton

    private lateinit var layoutFront: View
    private lateinit var layoutLeft: View
    private lateinit var layoutRight: View
    private lateinit var layoutBack: View
    private lateinit var layoutExtra: View

    private lateinit var layoutRcDoc: View
    private lateinit var layoutInsDoc: View
    private lateinit var layoutPucDoc: View
    private lateinit var layoutPermitDoc: View
    private lateinit var layoutFitnessDoc: View
    private lateinit var layoutOtherDoc: View

    private var currentTargetImage: ImageView? = null
    private var currentKey: String = ""

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val uri = result.data?.data ?: return@registerForActivityResult

        uploadToS3(currentKey, uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_photos, container, false)

        bind(v)
        fixInsets(v)
        setupLabels()
        setupUploads()

        btnPrev.setOnClickListener { (activity as VehicleRegistrationActivity).prevStep() }
        btnNext.setOnClickListener { if (validate()) (activity as VehicleRegistrationActivity).nextStep() }

        return v
    }

    private fun bind(v: View) {
        btnPrev = v.findViewById(R.id.btnPrev)
        btnNext = v.findViewById(R.id.btnNext)

        layoutFront = v.findViewById(R.id.layoutFront)
        layoutLeft = v.findViewById(R.id.layoutLeft)
        layoutRight = v.findViewById(R.id.layoutRight)
        layoutBack = v.findViewById(R.id.layoutBack)
        layoutExtra = v.findViewById(R.id.layoutExtra)

        layoutRcDoc = v.findViewById(R.id.layoutRcDoc)
        layoutInsDoc = v.findViewById(R.id.layoutInsDoc)
        layoutPucDoc = v.findViewById(R.id.layoutPucDoc)
        layoutPermitDoc = v.findViewById(R.id.layoutPermitDoc)
        layoutFitnessDoc = v.findViewById(R.id.layoutFitnessDoc)
        layoutOtherDoc = v.findViewById(R.id.layoutOtherDoc)
    }

    private fun fixInsets(v: View) {
        ViewCompat.setOnApplyWindowInsetsListener(v) { _, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            btnPrev.translationY = -bottom.toFloat()
            btnNext.translationY = -bottom.toFloat()
            insets
        }
    }

    private fun setupLabels() {
        setLabel(layoutFront, "Front Photo")
        setLabel(layoutLeft, "Left Photo")
        setLabel(layoutRight, "Right Photo")
        setLabel(layoutBack, "Back Photo")
        setLabel(layoutExtra, "Extra Photo")

        setDocLabel(layoutRcDoc, "RC Document")
        setDocLabel(layoutInsDoc, "Insurance Document")
        setDocLabel(layoutPucDoc, "PUC Document")
        setDocLabel(layoutPermitDoc, "Permit Document")
        setDocLabel(layoutFitnessDoc, "Fitness Document")
        setDocLabel(layoutOtherDoc, "Other Document")
    }

    private fun setLabel(root: View, label: String) {
        root.findViewById<TextView>(R.id.txtLabel).text = label
    }

    private fun setDocLabel(root: View, label: String) {
        root.findViewById<TextView>(R.id.txtDocLabel).text = label
    }

    private fun setupUploads() {

        setupUpload(layoutFront, "photo_front", "photo_front.jpg")
        setupUpload(layoutLeft, "photo_left", "photo_left.jpg")
        setupUpload(layoutRight, "photo_right", "photo_right.jpg")
        setupUpload(layoutBack, "photo_back", "photo_back.jpg")
        setupUpload(layoutExtra, "photo_extra", "photo_extra.jpg")

        setupUpload(layoutRcDoc, "doc_rc", "doc_rc.jpg")
        setupUpload(layoutInsDoc, "doc_insurance", "doc_insurance.jpg")
        setupUpload(layoutPucDoc, "doc_puc", "doc_puc.jpg")
        setupUpload(layoutPermitDoc, "doc_permit", "doc_permit.jpg")
        setupUpload(layoutFitnessDoc, "doc_fitness", "doc_fitness.jpg")
        setupUpload(layoutOtherDoc, "doc_other", "doc_other.jpg")
    }

    private fun setupUpload(root: View, key: String, fileName: String) {

        val img = root.findViewById<ImageView>(R.id.imgPhoto)
            ?: root.findViewById<ImageView>(R.id.imgDoc)

        val btn = root.findViewById<View>(R.id.btnUploadPhoto)
            ?: root.findViewById<View>(R.id.btnUploadDoc)

        btn.setOnClickListener {
            currentKey = key
            currentTargetImage = img
            chooseImage()
        }
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    /** Upload directly to S3 and save URL in ViewModel */
    private fun uploadToS3(key: String, uri: Uri) {

        val vehicleId = vm.vehicleNumber.value ?: "TEMP"

        val dialog = AlertDialog.Builder(requireContext())
            .setMessage("Uploading $key…")
            .setCancelable(false)
            .create()
        dialog.show()

        lifecycleScope.launch(Dispatchers.IO) {

            try {
                val stream = requireContext().contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open image stream")

                val s3Url = when {
                    key.startsWith("photo_") ->
                        S3Uploader.uploadAmbulancePhoto(vehicleId, "$key.jpg", stream)

                    key.startsWith("doc_") ->
                        S3Uploader.uploadAmbulanceDocument(vehicleId, "$key.jpg", stream)

                    else -> throw Exception("Invalid key: $key")
                }
                Log.d("PHOTOFRAG->P", key + "  |  " + s3Url)
                // Update ViewModel
                vm.setPhoto(key, s3Url)

                withContext(Dispatchers.Main) {
                    currentTargetImage?.setImageURI(uri)
                    Toast.makeText(requireContext(), "Uploaded", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validate(): Boolean {

        val photos = vm.photos.value ?: emptyMap()

        if (!photos.containsKey("photo_front")) {
            Toast.makeText(requireContext(), "Front photo required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!photos.containsKey("doc_rc")) {
            Toast.makeText(requireContext(), "RC document required", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
