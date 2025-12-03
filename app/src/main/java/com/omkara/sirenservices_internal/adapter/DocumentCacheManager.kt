package com.omkara.sirenservices_internal.adapter

import android.content.Context
import android.os.Environment
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class DocumentCacheManager(private val context: Context) {

    private val client = OkHttpClient()

    fun getLocalFileIfExists(fileName: String): File? {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        return if (file.exists()) file else null
    }

    fun downloadToLocal(url: String, fileName: String, callback: (String?) -> Unit) {
        Thread {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    callback(null)
                    return@Thread
                }

                val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val file = File(dir, fileName)

                file.outputStream().use { fos ->
                    fos.write(response.body?.bytes())
                }

                callback(file.absolutePath)

            } catch (e: Exception) {
                callback(null)
            }
        }.start()
    }
}
