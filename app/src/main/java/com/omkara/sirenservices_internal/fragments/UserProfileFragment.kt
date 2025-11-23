package com.omkara.sirenservices_internal.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.omkara.sirenservices_internal.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.*
import androidx.fragment.app.viewModels
import coil.load
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.textfield.TextInputEditText

import com.omkara.sirenservices_internal.models.UserModel
import com.omkara.sirenservices_internal.viewmodels.UserDetailsViewModel

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private val vm: UserDetailsViewModel by viewModels(ownerProducer = { requireActivity() })

    private var userId: String = ""
    private var newPhotoUri: Uri? = null

    companion object {
        fun newInstance(userId: String): UserProfileFragment {
            val f = UserProfileFragment()
            val b = Bundle()
            b.putString("USER_ID", userId)
            f.arguments = b
            return f
        }
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        userId = arguments?.getString("USER_ID") ?: return

        // Views
        val img = v.findViewById<ImageView>(R.id.imgUserProfilePic)
        val btnChangePhoto = v.findViewById<Button>(R.id.btnChangePhoto)
        val edtFirst = v.findViewById<TextInputEditText>(R.id.edtFirstName)
        val edtLast = v.findViewById<TextInputEditText>(R.id.edtLastName)
        val edtMobile = v.findViewById<TextInputEditText>(R.id.edtMobile)
        val edtEmail = v.findViewById<TextInputEditText>(R.id.edtEmail)
        val edtAddress = v.findViewById<TextInputEditText>(R.id.edtAddress)

        val autoRole = v.findViewById<AutoCompleteTextView>(R.id.autoRole)
        val autoStatus = v.findViewById<AutoCompleteTextView>(R.id.autoStatus)

        val driverSection = v.findViewById<LinearLayout>(R.id.driverSection)
        val edtLicense = v.findViewById<TextInputEditText>(R.id.edtLicense)
        val edtAadhar = v.findViewById<TextInputEditText>(R.id.edtAadhar)
        val edtPan = v.findViewById<TextInputEditText>(R.id.edtPan)

        val btnSave = v.findViewById<Button>(R.id.btnSaveProfile)

        // Dropdown
        autoRole.setAdapter(ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            listOf("owner", "driver", "staff", "operator", "mechanic")
        ))

        autoStatus.setAdapter(ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            listOf("active", "inactive")
        ))

        // Load user data
        vm.loadUser(userId)

        vm.user.observe(viewLifecycleOwner) { u: UserModel? ->
            if (u == null) return@observe

            img.load(u.profilePhoto ?: "") {
                placeholder(R.drawable.ic_user)
                error(R.drawable.ic_user)
            }

            edtFirst.setText(u.firstName)
            edtLast.setText(u.lastName)
            edtMobile.setText(u.mobile)
            edtEmail.setText(u.email)
            edtAddress.setText(u.address)

            autoRole.setText(u.role, false)
            autoStatus.setText(u.status, false)

            if (u.role == "driver") {
                driverSection.visibility = View.VISIBLE
                edtLicense.setText(u.driver?.licenseNo ?: "")
                edtAadhar.setText(u.driver?.aadharNo ?: "")
                edtPan.setText(u.driver?.panNo ?: "")
            } else {
                driverSection.visibility = View.GONE
            }
        }

        // Pick photo
        btnChangePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .start(101)
        }

        // Save
        btnSave.setOnClickListener {
            vm.updateUserProfile(
                userId = userId,
                firstName = edtFirst.text.toString(),
                lastName = edtLast.text.toString(),
                email = edtEmail.text.toString(),
                address = edtAddress.text.toString(),
                role = autoRole.text.toString(),
                status = autoStatus.text.toString(),
                license = edtLicense.text.toString(),
                aadhar = edtAadhar.text.toString(),
                pan = edtPan.text.toString(),
                newProfileUri = newPhotoUri,
                context = requireContext()
            )

            Toast.makeText(requireContext(), "Updated Successfully", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)

        if (res == Activity.RESULT_OK && req == 101) {
            val uri = data?.data ?: return
            newPhotoUri = uri

            // Preview
            view?.findViewById<ImageView>(R.id.imgUserProfilePic)?.load(uri)

            // Upload to S3 + Update Firestore
            vm.updateProfilePhoto(
                userId = userId,
                uri = uri,
                context = requireContext()
            ) { url ->
                requireActivity().runOnUiThread {
                    if (url != null) {
                        Toast.makeText(requireContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Upload failed!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

}
