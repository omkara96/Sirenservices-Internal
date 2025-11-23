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

    val users = MutableLiveData<List<UserModel>?>()

    // In-memory cache for fast load
    private var cachedUsers: List<UserModel>? = null

    /** Load cached list immediately + refresh from Firestore in background */
    fun loadUsers(forceRefresh: Boolean = false) {

        // 1. Instant UI update from cache
        if (!forceRefresh && cachedUsers != null) {
            users.value = cachedUsers
        }

        // 2. Always refresh from Firestore in background
        viewModelScope.launch {
            val freshList = fetchUsersFromFirestore()
            cachedUsers = freshList
            users.value = freshList
        }
    }

    /** Fetch fresh list from Firestore */
    private suspend fun fetchUsersFromFirestore(): List<UserModel> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val resultList = mutableListOf<UserModel>()

        return try {
            val userSnaps = db.collection("users").get().await()

            for (snap in userSnaps) {
                val u = snap.toObject(UserModel::class.java)
                u.id = snap.id  // your model uses "id" for document id

                // ---- Load Attendance Today ----
                val attSnap = db.collection("users")
                    .document(snap.id)
                    .collection("attendance")
                    .document(today)
                    .get()
                    .await()

                u.attendanceToday = attSnap.getString("status") == "present"

                // ---- Load Trips Today ----
                val tripSnap = db.collection("users")
                    .document(snap.id)
                    .collection("trips")
                    .whereEqualTo("date", today)
                    .get()
                    .await()

                u.tripsToday = tripSnap.size()

                // ---- Load Availability ----
                // available / occupied (driver only), rest default to available
                u.availability = u.driver?.availability ?: "available"

                resultList.add(u)
            }

            resultList

        } catch (e: Exception) {
            // If Firestore fails, return old cache instead of empty screen
            cachedUsers ?: emptyList()
        }
    }
}
