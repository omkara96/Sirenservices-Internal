package com.omkara.sirenservices_internal.loginsignup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.omkara.sirenservices_internal.MainActivity
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.activities.DashboardActivity
import com.omkara.sirenservices_internal.dashboards.AdminDashboard

class LoginActivity : AppCompatActivity() {

    private lateinit var userName: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        analytics = Firebase.analytics
        auth = Firebase.auth
        userName = findViewById(R.id.edtuname)
        password = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener(View.OnClickListener {

            val uname = userName.editableText.toString();
            val upass = password.editableText.toString();

            if (uname.isEmpty() || upass.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "Logging User in...", Toast.LENGTH_SHORT).show();
                // Firebase Login
                auth.signInWithEmailAndPassword(uname, upass)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Login Success ✅", Toast.LENGTH_SHORT).show()
                        // Redirect to Main Activity
                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(
                            this,
                            "Login Failed ❌: ${error.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }

}