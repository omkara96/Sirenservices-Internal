package com.omkara.sirenservices_internal

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.util.UUID

object S3Uploader {

    private const val TAG = "S3Uploader"

    // 🔧 Change these values if needed
    private const val BUCKET = "om-ambulance-app"
    private const val REGION = "ap-southeast-1"

    private val client = OkHttpClient()

    /** Build public S3 URL */
    private fun getS3Url(path: String): String =
        "https://$BUCKET.s3.$REGION.amazonaws.com/$path"

    /** Convert InputStream → ByteArray safely */
    private suspend fun readBytes(stream: InputStream): ByteArray =
        withContext(Dispatchers.IO) {
            val bytes = stream.readBytes()
            stream.close()
            bytes
        }

    /** Upload any file to S3 with public read-enabled bucket. */
    suspend fun uploadBytes(bytes: ByteArray, path: String, mime: String): String =
        withContext(Dispatchers.IO) {

            val url = getS3Url(path)
            Log.d(TAG, "Uploading to S3: $url")

            val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
            val req = Request.Builder()
                .url(url)
                .put(body)
              //  .addHeader("x-amz-acl", "public-read")
                .build()

            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) {
                throw Exception("S3 Upload Failed: ${resp.code} ${resp.message}")
            }
            return@withContext url
        }

    // --------------------------------------------------------------
    // PUBLIC UPLOAD METHODS (use these everywhere)
    // --------------------------------------------------------------

    /** Upload profile photo */
    suspend fun uploadProfilePhoto(userId: String, stream: InputStream): String {
        val file = "profile_${UUID.randomUUID()}.jpg"
        val path = "users/$userId/profile/$file"
        val bytes = readBytes(stream)
        return uploadBytes(bytes, path, "image/jpeg")
    }




    suspend fun uploadAmbulancePhoto(vehicleId: String, fileName: String, stream: InputStream): String {
        val path = "uploads/ambulance/$vehicleId/photos/$fileName"
        val bytes = readBytes(stream)
        return uploadBytes(bytes, path, "image/jpeg")
    }

    /** Upload any ambulance DOCUMENT with a fixed file name */
    suspend fun uploadAmbulanceDocument(
        ambulanceId: String,
        fileName: String,     // example: "doc_rc.jpg"
        stream: InputStream
    ): String = withContext(Dispatchers.IO) {

        val path = "uploads/ambulance/$ambulanceId/documents/$fileName"
        val bytes = readBytes(stream)

        uploadBytes(bytes, path, "image/jpeg")
    }



}
