package com.omkara.sirenservices_internal.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val s3BucketName: String = "om-ambulance-app", // replace with your bucket
    private val s3Region: String = "ap-southeast-1" // replace if needed
) {
    private val okClient = OkHttpClient()

    /**
     * Upload bytes to S3 public bucket via PUT. filePath example: "uploads/drivers/{driverId}/passport.jpg"
     * Returns public S3 URL on success, or throws Exception.
     *
     * IMPORTANT: This assumes your bucket policy allows PUT into uploads/ as discussed.
     */
    suspend fun uploadBytesToS3(fileBytes: ByteArray, filePath: String, mimeType: String): String =
        withContext(Dispatchers.IO) {
            val url = "https://$s3BucketName.s3.$s3Region.amazonaws.com/$filePath"
            val body = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val request = Request.Builder().url(url).put(body).build()
            val resp = okClient.newCall(request).execute()
            if (!resp.isSuccessful) {
                throw Exception("S3 upload failed (${resp.code}): ${resp.message}")
            }
            return@withContext url
        }

    /**
     * Create or update user in Firestore. If userId empty, a new document is created and id returned.
     */
    suspend fun saveUser(userId: String?, userMap: Map<String, Any?>): String =
        withContext(Dispatchers.IO) {
            if (userId.isNullOrEmpty()) {
                val docRef = firestore.collection("users").document()
                firestore.collection("users").document(docRef.id).set(userMap).await()
                return@withContext docRef.id
            } else {
                firestore.collection("users").document(userId).set(userMap).await()
                return@withContext userId
            }
        }

    // helper function to read bytes from Uri inputStream in Activity -> pass InputStream
    suspend fun readBytesFromInputStream(input: InputStream): ByteArray =
        withContext(Dispatchers.IO) {
            val bytes = input.readBytes()
            input.close()
            bytes
        }
}
