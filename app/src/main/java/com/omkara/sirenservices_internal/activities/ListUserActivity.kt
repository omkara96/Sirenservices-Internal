package com.omkara.sirenservices_internal.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.omkara.sirenservices_internal.R
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.adapters.UserAdapter
import com.omkara.sirenservices_internal.models.UserModel


class ListUserActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerUsers: RecyclerView
    private lateinit var edtSearchUser: TextInputEditText

    private lateinit var adapter: UserAdapter
    private var userList = ArrayList<UserModel>()
    private var filteredList = ArrayList<UserModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_user)

        firestore = FirebaseFirestore.getInstance()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarListUsers)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerUsers = findViewById(R.id.recyclerUsers)
        edtSearchUser = findViewById(R.id.edtSearchUser)

        recyclerUsers.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(filteredList) { userId ->
            val intent = Intent(this, ViewUserActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
        recyclerUsers.adapter = adapter

        loadUsers()

        edtSearchUser.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterUsers(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadUsers() {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { docs ->
                userList.clear()

                for (doc in docs) {
                    val user = UserModel(
                        firstName = doc.getString("firstName") ?: "",
                        middleName = doc.getString("middleName") ?: "",
                        lastName = doc.getString("lastName") ?: "",
                        mobile = doc.getString("mobile") ?: "",
                        email = doc.getString("email") ?: "",
                        role = doc.getString("role") ?: "",
                        status = doc.getString("status") ?: ""
                    )
                    user.id = doc.id
                    userList.add(user)
                }

                filteredList.clear()
                filteredList.addAll(userList)
                adapter.notifyDataSetChanged()
            }
    }

    override fun onResume() {
        super.onResume()
        loadUsers()   // 🔥 Refresh every time user returns
    }

    private fun filterUsers(query: String) {
        val lowerQuery = query.lowercase()

        filteredList.clear()
        filteredList.addAll(
            userList.filter {
                it.firstName.lowercase().contains(lowerQuery) ||
                        it.lastName.lowercase().contains(lowerQuery) ||
                        it.mobile.contains(query)
            }
        )
        adapter.notifyDataSetChanged()
    }
}
