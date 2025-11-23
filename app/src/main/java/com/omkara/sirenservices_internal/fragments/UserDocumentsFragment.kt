package com.omkara.sirenservices_internal.fragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.load
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.activities.FullScreenImageActivity
import com.omkara.sirenservices_internal.S3Uploader
import com.omkara.sirenservices_internal.viewmodels.UserDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserDocumentsFragment : Fragment() {

    private val vm: UserDetailsViewModel by activityViewModels()
    private val ioScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: String): UserDocumentsFragment {
            val f = UserDocumentsFragment()
            f.arguments = Bundle().apply { putString(ARG_USER_ID, userId) }
            return f
        }
    }

    private val userId: String by lazy {
        arguments?.getString(ARG_USER_ID, "") ?: ""
    }

    private var replaceKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.loadUser(userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_user_documents, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Cards
        val cardFront = view.findViewById<MaterialCardView>(R.id.cardAadharFront)
        val cardBack = view.findViewById<MaterialCardView>(R.id.cardAadharBack)
        val cardPan = view.findViewById<MaterialCardView>(R.id.cardPan)
        val cardPassbook = view.findViewById<MaterialCardView>(R.id.cardPassbook)
        val cardLicence = view.findViewById<MaterialCardView>(R.id.cardLicence)

        // Images
        val imgFront = view.findViewById<ImageView>(R.id.imgAadharFront)
        val imgBack = view.findViewById<ImageView>(R.id.imgAadharBack)
        val imgPan = view.findViewById<ImageView>(R.id.imgPan)
        val imgPassbook = view.findViewById<ImageView>(R.id.imgPassbook)
        val imgLicence = view.findViewById<ImageView>(R.id.imgLicence)

        // Replace buttons
        val btnFront = view.findViewById<MaterialButton>(R.id.btnReplaceAadharFront)
        val btnBack = view.findViewById<MaterialButton>(R.id.btnReplaceAadharBack)
        val btnPan = view.findViewById<MaterialButton>(R.id.btnReplacePan)
        val btnPassbook = view.findViewById<MaterialButton>(R.id.btnReplacePassbook)
        val btnLicence = view.findViewById<MaterialButton>(R.id.btnReplaceLicence)

        val lblEmptyDocs = view.findViewById<MaterialTextView>(R.id.lblEmptyDocs)

        // Observe user data
        vm.user.observe(viewLifecycleOwner) { user ->

            val docs = user?.driver?.documents ?: emptyMap()

            fun handleDocument(
                card: MaterialCardView,
                img: ImageView,
                button: MaterialButton,
                key: String
            ) {
                val url = docs[key]

                if (url.isNullOrEmpty()) {
                    card.visibility = View.GONE
                    button.visibility = View.VISIBLE
                } else {
                    card.visibility = View.VISIBLE
                    button.visibility = View.VISIBLE

                    img.load(url) {
                        crossfade(true)
                        placeholder(R.drawable.ic_doc_placeholder)
                    }

                    // Full Screen View
                    img.setOnClickListener {
                        val i = Intent(requireContext(), FullScreenImageActivity::class.java)
                        i.putExtra("image_url", url)
                        startActivity(i)
                    }

                    // Replace button
                    button.setOnClickListener {
                        replaceKey = key
                        pickImage()
                    }

                    // Long press → download
                    img.setOnLongClickListener {
                        downloadImage(url)
                        true
                    }
                }
            }

            handleDocument(cardFront, imgFront, btnFront, "aadharFront")
            handleDocument(cardBack, imgBack, btnBack, "aadharBack")
            handleDocument(cardPan, imgPan, btnPan, "pan")
            handleDocument(cardPassbook, imgPassbook, btnPassbook, "passbook")
            handleDocument(cardLicence, imgLicence, btnLicence, "licence")

            val allGone = listOf(cardFront, cardBack, cardPan, cardPassbook, cardLicence)
                .all { it.visibility == View.GONE }

            lblEmptyDocs.visibility = if (allGone) View.VISIBLE else View.GONE
        }
    }

    // -------------------------------
    // Image Picker
    // -------------------------------
    private fun pickImage() {
        ImagePicker.with(this)
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri: Uri = data?.data ?: return

        val key = replaceKey ?: return
        uploadDocument(uri, key)
    }

    // -------------------------------
    // Upload to S3 + Update Firestore
    // -------------------------------
    private fun uploadDocument(uri: Uri, key: String) {
        showLoading("Uploading...")

        ioScope.launch {
            try {
                val uploader = S3Uploader

                val stream = requireContext().contentResolver.openInputStream(uri)!!
                val bytes = stream.readBytes()
                stream.close()

                val filePath = "documents/$userId/$key.jpg"

                val url = uploader.uploadBytes(
                    bytes,
                    filePath,
                    "image/jpeg"
                )

                vm.updateUserDocument(userId, key, url)

                dismissLoading()

                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Updated successfully!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                dismissLoading()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // -------------------------------
    // Download Image
    // -------------------------------
    private fun downloadImage(url: String) {
        Toast.makeText(requireContext(), "Downloading...", Toast.LENGTH_SHORT).show()

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    // -------------------------------
    // Progress Dialog
    // -------------------------------
    private var progressDialog: Dialog? = null

    private fun showLoading(msg: String) {
        val d = Dialog(requireContext())
        d.setContentView(R.layout.dialog_loading)
        d.setCancelable(false)

        val txt = d.findViewById<MaterialTextView>(R.id.txtLoadingMessage)
        txt.text = msg

        progressDialog = d
        d.show()
    }

    private fun dismissLoading() {
        progressDialog?.dismiss()
    }
}
