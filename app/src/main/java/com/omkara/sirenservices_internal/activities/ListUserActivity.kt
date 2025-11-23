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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.adapters.UserAdapter
import com.omkara.sirenservices_internal.fragments.UserListFragment
import com.omkara.sirenservices_internal.loginsignup.UserRegistation
import com.omkara.sirenservices_internal.models.UserModel

class ListUserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_user)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarListUsers)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Load UserListFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerUsers, UserListFragment())
            .commit()

        findViewById<FloatingActionButton>(R.id.fabAddUser).setOnClickListener {
            startActivity(Intent(this, UserRegistation::class.java))
        }
    }
}
