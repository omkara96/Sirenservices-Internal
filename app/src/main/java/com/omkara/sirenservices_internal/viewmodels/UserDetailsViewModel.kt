package com.omkara.sirenservices_internal.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.omkara.sirenservices_internal.S3Uploader
import com.omkara.sirenservices_internal.models.AttendanceModel
import com.omkara.sirenservices_internal.models.AttendanceSummary
import com.omkara.sirenservices_internal.models.SalaryEntry
import com.omkara.sirenservices_internal.models.SalarySummary
import com.omkara.sirenservices_internal.models.UserModel
import com.omkara.sirenservices_internal.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.sql.Timestamp

class UserDetailsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val repository = UserRepository()

    val user = MutableLiveData<UserModel?>()

    private val firestore = FirebaseFirestore.getInstance()


    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    val salaryEntries = MutableLiveData<List<SalaryEntry>>()
    val salarySummary = MutableLiveData<SalarySummary>()

    // -----------------------------
// ATTENDANCE LiveData
// -----------------------------
    private val _attendanceMonthly = MutableLiveData<List<AttendanceModel>>()
    val attendanceMonthly: LiveData<List<AttendanceModel>> = _attendanceMonthly

    private val _attendanceSummary = MutableLiveData<AttendanceSummary>()
    val attendanceSummary: LiveData<AttendanceSummary> = _attendanceSummary


    fun loadUser(userId: String) {
        viewModelScope.launch {
            val snap = db.collection("users").document(userId).get().await()
            user.value = snap.toObject(UserModel::class.java)
        }
    }

    fun updateUserProfile(
        userId: String,
        firstName: String,
        lastName: String,
        email: String,
        address: String,
        role: String,
        status: String,
        license: String?,
        aadhar: String?,
        pan: String?,
        newProfileUri: Uri?,
        context: Context
    ) {
        viewModelScope.launch {

            var profileUrl: String? = user.value?.profilePhoto

            // Upload new photo if selected
            if (newProfileUri != null) {
                val stream = context.contentResolver.openInputStream(newProfileUri)
                profileUrl = S3Uploader.uploadProfilePhoto(userId, stream!!)
            }

            val updateMap = mutableMapOf<String, Any?>(
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "address" to address,
                "role" to role,
                "status" to status,
                "profilePhoto" to profileUrl
            )

            // Driver fields
            if (role == "driver") {
                updateMap["driver"] = mapOf(
                    "licenseNo" to license,
                    "aadharNo" to aadhar,
                    "panNo" to pan
                )
            }

            db.collection("users").document(userId).update(updateMap)
        }
    }


    fun updateUserDocument(userId: String, key: String, url: String) {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val current = user.value

                // Ensure driver object exists
                val currentDriver = current?.driver
                val oldDocs = currentDriver?.documents ?: emptyMap()

                val newDocs = oldDocs.toMutableMap()
                newDocs[key] = url

                // Push update to Firestore
                firestore.collection("users")
                    .document(userId)
                    .update("driver.documents", newDocs)
                    .await()

                // Update livedata model
                val updatedModel = current?.copy(
                    driver = currentDriver?.copy(
                        documents = newDocs
                    )
                )

                user.postValue(updatedModel)

            } catch (e: Exception) {
                _error.postValue("Failed to update document: ${e.message}")
            }
        }
    }


    fun updateProfilePhoto(userId: String, uri: Uri, context: Context, onDone: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {

            try {
                val uploader = S3Uploader

                val stream = context.contentResolver.openInputStream(uri)
                val bytes = stream?.readBytes() ?: throw Exception("Could not read file!")
                stream.close()

                val phone = user.value?.mobile  // required field
                val timestamp = System.currentTimeMillis()

                val path = "uploads/profiles/${phone}_${timestamp}.jpg"


               // val path = "profile/$userId/profile.jpg"

                Log.e("S3UPLOAD", "Uploading $path (${bytes.size} bytes)")

                val url = uploader.uploadBytes(bytes, path, "image/jpeg")

                Log.e("S3UPLOAD", "Uploaded URL: $url")

                firestore.collection("users")
                    .document(userId)
                    .update("profilePhoto", url)
                    .await()

                Log.e("S3UPLOAD", "Firestore updated!")

                user.postValue(user.value?.copy(profilePhoto = url))
                onDone(url)

            } catch (e: Exception) {
                Log.e("S3UPLOAD", "Upload failed: ", e)  // 👈 PRINT REAL ERROR
                _error.postValue(e.message)
                onDone(null)
            }
        }
    }




    // ----------------------------
    // Load Salary Entries for Month
    // ----------------------------
    fun loadMonthlyEntries(userId: String, yearMonth: String) {
        viewModelScope.launch(Dispatchers.IO) {

            try {
                val colRef = firestore.collection("users")
                    .document(userId)
                    .collection("salary")
                    .document(yearMonth)
                    .collection("entries")

                val snaps = colRef.get().await()

                val list = snaps.documents.map { snap ->
                    snap.toObject(SalaryEntry::class.java)!!.copy(id = snap.id)
                }.sortedByDescending { it.timestamp }

                salaryEntries.postValue(list)

                calculateSummary(list)

            } catch (e: Exception) {
                salaryEntries.postValue(emptyList())
            }
        }
    }

    // ----------------------------
    // Add a new salary entry
    // ----------------------------
    fun addSalaryEntry(
        userId: String,
        yearMonth: String,
        type: String,
        amount: Double,
        note: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entry = SalaryEntry(
                    type = type,
                    amount = amount,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )

                firestore.collection("users")
                    .document(userId)
                    .collection("salary")
                    .document(yearMonth)
                    .collection("entries")
                    .add(entry)
                    .await()

                // Reload updated list
                loadMonthlyEntries(userId, yearMonth)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ----------------------------
    // Calculate summary values
    // ----------------------------
    private fun calculateSummary(list: List<SalaryEntry>) {

        var base = 0.0
        var allowance = 0.0
        var bonus = 0.0
        var overtime = 0.0
        var deductions = 0.0

        list.forEach { e ->
            when (e.type) {
                "base" -> base += e.amount
                "allowance" -> allowance += e.amount
                "bonus" -> bonus += e.amount
                "overtime" -> overtime += e.amount
                "deduction", "fine" -> deductions += e.amount
            }
        }

        val net = base + allowance + bonus + overtime - Math.abs(deductions)

        val summary = SalarySummary(
            totalBase = base,
            totalAllowances = allowance,
            totalBonus = bonus,
            totalOvertime = overtime,
            totalDeductions = Math.abs(deductions),
            netSalary = net
        )

        salarySummary.postValue(summary)
    }

    fun addSalaryEntriesBatch(userId: String, yearMonth: String, entries: List<SalaryEntry>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (entries.isEmpty()) return@launch

                val docRef = firestore.collection("users").document(userId)
                    .collection("salary").document(yearMonth)

                // create batch
                val batch: WriteBatch = firestore.batch()

                // ensure parent doc exists (we write an empty field if needed)
                batch.set(docRef, mapOf("month" to yearMonth), com.google.firebase.firestore.SetOptions.merge())

                // add each entry doc
                val entriesCol = docRef.collection("entries")
                entries.forEach { e ->
                    val newDoc = entriesCol.document()
                    val toSave = e.copy(id = newDoc.id)
                    batch.set(newDoc, toSave)
                }

                // commit
                batch.commit().await()

                // reload
                loadMonthlyEntries(userId, yearMonth)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markMonthPaid(userId: String, yearMonth: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val docRef = firestore.collection("users")
                    .document(userId)
                    .collection("salary")
                    .document(yearMonth)

                docRef.set(mapOf("isPaid" to true, "paidOn" to System.currentTimeMillis()), com.google.firebase.firestore.SetOptions.merge()).await()

                // reload to update month labels if UI checks again
                loadMonthlyEntries(userId, yearMonth)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markAttendance(
        userId: String,
        yearMonth: String,   // yyyy-MM
        date: String,        // yyyy-MM-dd
        status: String,
        note: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "status" to status,
                    "timestamp" to System.currentTimeMillis(),
                    "note" to (note ?: "")
                )

                firestore.collection("users")
                    .document(userId)
                    .collection("attendance")
                    .document(yearMonth)
                    .collection("days")
                    .document(date)
                    .set(data)
                    .await()

                // refresh month
                loadMonthlyAttendance(userId, yearMonth)

            } catch (e: Exception) {
                _error.postValue("Failed to mark attendance: ${e.message}")
            }
        }
    }

    fun loadMonthlyAttendance(userId: String, yearMonth: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snap = firestore.collection("users")
                    .document(userId)
                    .collection("attendance")
                    .document(yearMonth)
                    .collection("days")
                    .get()
                    .await()

                val list = snap.documents.map { d ->
                    AttendanceModel(
                        date = d.id,
                        status = d.getString("status") ?: "absent",
                        timestamp = d.getLong("timestamp") ?: 0L,
                        notes = d.getString("note") ?: ""
                    )
                }.sortedBy { it.date }

                _attendanceMonthly.postValue(list)
                calculateAttendanceSummary(list)

            } catch (e: Exception) {
                _error.postValue("Failed loading attendance: ${e.message}")
            }
        }
    }

    private fun calculateAttendanceSummary(list: List<AttendanceModel>) {
        var present = 0
        var absent = 0
        var late = 0
        var half = 0
        var leave = 0
        var holiday = 0

        list.forEach {
            when (it.status) {
                "present" -> present++
                "absent" -> absent++
                "late" -> late++
                "half" -> half++
                "leave" -> leave++
                "holiday" -> holiday++
            }
        }

        val summary = AttendanceSummary(
            present = present,
            absent = absent,
            late = late,
            half = half,
            leave = leave,
            holiday = holiday,
            totalDays = list.size
        )

        _attendanceSummary.postValue(summary)
    }





}
