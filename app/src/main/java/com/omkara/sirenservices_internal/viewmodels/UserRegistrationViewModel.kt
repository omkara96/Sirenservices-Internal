package com.omkara.sirenservices_internal.viewmodels


import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.models.DriverData
import com.omkara.sirenservices_internal.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import androidx.lifecycle.viewModelScope

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val userId: String) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

class UserRegistrationViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _state = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val state: StateFlow<RegistrationState> = _state

    /**
     * High-level register function.
     * - profileInputStreamProvider: lambda that returns InputStream for profileUri (Activity provides)
     * - docInputProviders: map of (docKey->lambda InputStream) for driver documents
     */
    fun registerUser(
        userId: String? = null,
        firstName: String,
        middleName: String?,
        lastName: String?,
        dob: String?,
        mobile: String,
        email: String?,
        address: String?,
        role: String,
        status: String,
        profileUri: Uri?,
        profileInputStreamProvider: (suspend () -> InputStream?)?,
        // driver-specific
        isDriver: Boolean,
        licenseNo: String?,
        aadharNo: String?,
        panNo: String?,
        accountNo: String?,
        ifsc: String?,
        bankName: String?,
        branchName: String?,
        // documents: map of key -> (suspend () -> InputStream?)
        documentInputs: Map<String, suspend () -> InputStream?>?
    ) {
        _state.value = RegistrationState.Loading

        viewModelScope.launch {
            try {
                val createdAt = Timestamp.now()
                var profileUrl: String? = null

                // 1) upload profile if present
                if (profileUri != null && profileInputStreamProvider != null) {
                    val input = profileInputStreamProvider()
                    if (input != null) {
                        val bytes = userRepository.readBytesFromInputStream(input)
                        val filePath = "uploads/profiles/${mobile}_${System.currentTimeMillis()}.jpg"
                        profileUrl = userRepository.uploadBytesToS3(bytes, filePath, "image/jpeg")
                    }
                }

                // 2) If driver, upload documents map and compose DriverData
                var driverData: DriverData? = null
                if (isDriver) {
                    val docsMap = mutableMapOf<String, String>()
                    if (!documentInputs.isNullOrEmpty()) {
                        for ((key, provider) in documentInputs) {
                            val inStream = provider()
                            if (inStream != null) {
                                val bytes = userRepository.readBytesFromInputStream(inStream)
                                val ext = if (key.contains("pdf", true) ) "pdf" else "jpg"
                                val filePath = "uploads/drivers/$mobile/${key}_${System.currentTimeMillis()}.$ext"
                                val url = userRepository.uploadBytesToS3(bytes, filePath, if (ext=="pdf") "application/pdf" else "image/jpeg")
                                docsMap[key] = url
                            }
                        }
                    }
                    driverData = DriverData(
                        licenseNo = licenseNo,
                        aadharNo = aadharNo,
                        panNo = panNo,
                        accountNo = accountNo,
                        ifsc = ifsc,
                        bankName = bankName,
                        branchName = branchName,
                        documents = if (docsMap.isEmpty()) null else docsMap,
                        availability = "available"
                    )
                }

                // 3) compose user map
                val userDoc = hashMapOf<String, Any?>(
                    "firstName" to firstName,
                    "middleName" to middleName,
                    "lastName" to lastName,
                    "dob" to dob,
                    "mobile" to mobile,
                    "email" to email,
                    "address" to address,
                    "role" to role,
                    "status" to status,
                    "profilePhoto" to profileUrl,
                    "driver" to (driverData?.let { mapOf(
                        "licenseNo" to it.licenseNo,
                        "aadharNo" to it.aadharNo,
                        "panNo" to it.panNo,
                        "accountNo" to it.accountNo,
                        "ifsc" to it.ifsc,
                        "bankName" to it.bankName,
                        "branchName" to it.branchName,
                        "documents" to it.documents,
                        "availability" to it.availability
                    ) } ?: null),
                    "createdAt" to createdAt,
                    "updatedAt" to createdAt
                )

                // 4) save to Firestore
                val finalUserId = userRepository.saveUser(userId, userDoc)
                _state.value = RegistrationState.Success(finalUserId)

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = RegistrationState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
