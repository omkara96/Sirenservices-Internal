package com.omkara.sirenservices_internal.activities

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.loginsignup.LoginActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Correct SplashScreen API call
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val root = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootLayout)

        // Fade In Animation
        val fadeIn = ObjectAnimator.ofFloat(root, "alpha", 0f, 1f)
        fadeIn.duration = 700
        fadeIn.start()

        root.postDelayed({
            fadeOutAndLaunch()
        }, 2000)
    }

    private fun fadeOutAndLaunch() {
        val root = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootLayout)

        val fadeOut = ObjectAnimator.ofFloat(root, "alpha", 1f, 0f)
        fadeOut.duration = 500

        fadeOut.addListener(object : Animator.AnimatorListener {

            // Use correct signatures → including isReverse parameter
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                goNext()
            }

            override fun onAnimationEnd(animation: Animator) {
                goNext()
            }

            override fun onAnimationStart(animation: Animator, isReverse: Boolean) {}
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        fadeOut.start()
    }

    private fun goNext() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
