package com.omkara.sirenservices_internal.fragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.S3Uploader
import com.omkara.sirenservices_internal.activities.FullScreenImageActivity
import com.omkara.sirenservices_internal.viewmodels.UserDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserDocumentsFragment : Fragment(R.layout.fragment_user_documents) {

    private val vm: UserDetailsViewModel by activityViewModels()

    private lateinit var userId: String
    private var replaceKey: String? = null
    private var progressDialog: Dialog? = null

    /* ---------------------------------------------------
       Image Picker Result
    --------------------------------------------------- */
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                val key = replaceKey ?: return@registerForActivityResult
                uploadDocument(uri, key)
            }
        }

    /* ---------------------------------------------------
       Lifecycle
    --------------------------------------------------- */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = requireArguments().getString(ARG_USER_ID, "")
        vm.loadUser(userId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val lblEmptyDocs = view.findViewById<MaterialTextView>(R.id.lblEmptyDocs)

        val docsUi = listOf(
            DocUI(
                card = view.findViewById(R.id.cardAadharFront),
                img = view.findViewById(R.id.imgAadharFront),
                btn = view.findViewById(R.id.btnReplaceAadharFront),
                key = "aadharFront"
            ),
            DocUI(
                card = view.findViewById(R.id.cardAadharBack),
                img = view.findViewById(R.id.imgAadharBack),
                btn = view.findViewById(R.id.btnReplaceAadharBack),
                key = "aadharBack"
            ),
            DocUI(
                card = view.findViewById(R.id.cardPan),
                img = view.findViewById(R.id.imgPan),
                btn = view.findViewById(R.id.btnReplacePan),
                key = "pan"
            ),
            DocUI(
                card = view.findViewById(R.id.cardPassbook),
                img = view.findViewById(R.id.imgPassbook),
                btn = view.findViewById(R.id.btnReplacePassbook),
                key = "passbook"
            ),
            DocUI(
                card = view.findViewById(R.id.cardLicence),
                img = view.findViewById(R.id.imgLicence),
                btn = view.findViewById(R.id.btnReplaceLicence),
                key = "licence"
            )
        )

        vm.user.observe(viewLifecycleOwner) { user ->
            val documents = user?.driver?.documents ?: emptyMap()

            docsUi.forEach { ui ->
                bindDocument(ui, documents[ui.key])
            }

            lblEmptyDocs.visibility =
                if (docsUi.all { it.card.visibility == View.GONE }) View.VISIBLE else View.GONE
        }
    }

    /* ---------------------------------------------------
       UI Binding
    --------------------------------------------------- */
    private fun bindDocument(ui: DocUI, url: String?) {

        if (url.isNullOrEmpty()) {
            ui.card.visibility = View.GONE
            ui.btn.visibility = View.VISIBLE
            ui.btn.setOnClickListener {
                replaceKey = ui.key
                pickImage()
            }
            return
        }

        ui.card.visibility = View.VISIBLE
        ui.btn.visibility = View.VISIBLE

        ui.img.load(url) {
            placeholder(R.drawable.ic_doc_placeholder)
            crossfade(true)
        }

        ui.img.setOnClickListener {
            startActivity(
                Intent(requireContext(), FullScreenImageActivity::class.java)
                    .putExtra("image_url", url)
            )
        }

        ui.img.setOnLongClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            true
        }

        ui.btn.setOnClickListener {
            replaceKey = ui.key
            pickImage()
        }
    }

    /* ---------------------------------------------------
       Image Picker
    --------------------------------------------------- */
    private fun pickImage() {
        ImagePicker.with(this)
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                imagePickerLauncher.launch(intent)
            }
    }

    /* ---------------------------------------------------
       Upload
    --------------------------------------------------- */
    private fun uploadDocument(uri: Uri, key: String) {

        showLoading("Uploading document...")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    requireContext().contentResolver.openInputStream(uri)!!
                        .use { it.readBytes() }
                }

                val path = "documents/$userId/$key.jpg"

                val url = withContext(Dispatchers.IO) {
                    S3Uploader.uploadBytes(bytes, path, "image/jpeg")
                }

                vm.updateUserDocument(userId, key, url)

                dismissLoading()
                Toast.makeText(requireContext(), "Document updated", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                dismissLoading()
                Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /* ---------------------------------------------------
       Loading Dialog
    --------------------------------------------------- */
    private fun showLoading(msg: String) {
        if (progressDialog?.isShowing == true) return

        progressDialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_loading)
            setCancelable(false)
            findViewById<MaterialTextView>(R.id.txtLoadingMessage).text = msg
            show()
        }
    }

    private fun dismissLoading() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    /* ---------------------------------------------------
       Models
    --------------------------------------------------- */
    private data class DocUI(
        val card: MaterialCardView,
        val img: ImageView,
        val btn: MaterialButton,
        val key: String
    )

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: String) =
            UserDocumentsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
    }
}
