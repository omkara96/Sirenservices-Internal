package com.omkara.sirenservices_internal.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.models.UserModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    val users = MutableLiveData<List<UserModel>>()

    private var cachedUsers: List<UserModel>? = null

    fun loadUsers(forceRefresh: Boolean = false) {

        if (!forceRefresh && cachedUsers != null) {
            users.value = cachedUsers!!.toList()
        }

        viewModelScope.launch {
            val fresh = fetchUsersFromFirestore()
            cachedUsers = fresh
            users.value = fresh.toList()
        }
    }

    private suspend fun fetchUsersFromFirestore(): List<UserModel> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val result = mutableListOf<UserModel>()

        return try {
            val userSnaps = db.collection("users").get().await()

            for (snap in userSnaps) {
                val u = snap.toObject(UserModel::class.java)
                u.id = snap.id

                // Attendance
                try {
                    val attSnap = db.collection("users")
                        .document(snap.id)
                        .collection("attendance")
                        .document(today)
                        .get()
                        .await()

                    u.attendanceToday = attSnap.getString("status") == "present"
                } catch (_: Exception) {
                    u.attendanceToday = false
                }

                // Trips
                try {
                    val tripSnap = db.collection("users")
                        .document(snap.id)
                        .collection("trips")
                        .whereEqualTo("date", today)
                        .get()
                        .await()

                    u.tripsToday = tripSnap.size()
                } catch (_: Exception) {
                    u.tripsToday = 0
                }

                // Availability (single source of truth)
                u.availability = u.driver?.availability ?: "available"

                result.add(u)
            }

            result

        } catch (e: Exception) {
            cachedUsers?.toList() ?: emptyList()
        }
    }

    fun updateUserDocument(userId: String, key: String, url: String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update("driver.documents.$key", url)
    }

}
