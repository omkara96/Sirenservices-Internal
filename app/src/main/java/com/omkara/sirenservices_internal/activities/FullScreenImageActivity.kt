package com.omkara.sirenservices_internal.activities

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.omkara.sirenservices_internal.R
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.Toast
import coil.load
import com.google.android.material.floatingactionbutton.FloatingActionButton


class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var btnDownload: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val img = findViewById<ImageView>(R.id.fullImageView)
        val btnClose = findViewById<ImageButton>(R.id.btnCloseFullImage)
        btnDownload = findViewById(R.id.btnDownload)

        val url = intent.getStringExtra("image_url") ?: ""

        img.load(url) {
            crossfade(true)
            placeholder(R.drawable.ic_doc_placeholder)
        }

        btnClose.setOnClickListener {
            finish()
        }

        // Download
        btnDownload.setOnClickListener {
            downloadImage(url)
        }
    }

    private fun downloadImage(url: String) {
        try {
            val fileName = "doc_${System.currentTimeMillis()}.jpg"

            val request = DownloadManager.Request(Uri.parse(url))
                .setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
                )
                .setTitle("Downloading Document")
                .setDescription("Saving image...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setMimeType("image/jpeg")
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "SirenServices/$fileName"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            Toast.makeText(this, "Download started...", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
