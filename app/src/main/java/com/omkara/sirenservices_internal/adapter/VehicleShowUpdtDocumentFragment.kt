package com.omkara.sirenservices_internal.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.DocumentCacheManager
import com.omkara.sirenservices_internal.adapter.VehicleDocumentsAdapter
import com.omkara.sirenservices_internal.models.VehicleDocument
import java.io.File

class VehicleShowUpdtDocumentFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: VehicleDocumentsAdapter
    private val db = FirebaseFirestore.getInstance()

    private var vehicleId = ""

    companion object {
        fun newInstance(id: String): VehicleShowUpdtDocumentFragment {
            val f = VehicleShowUpdtDocumentFragment()
            f.arguments = Bundle().apply { putString("vehicle_id", id) }
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vehicleId = arguments?.getString("vehicle_id") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_vehicle_show_updt_document, container, false)

        recycler = v.findViewById(R.id.recyclerDocuments)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = VehicleDocumentsAdapter(
            list = emptyList(),
            onOpenClicked = { doc -> openDocument(doc) },
            onDownloadClicked = { doc -> downloadDocument(doc) }
        )
        recycler.adapter = adapter

        loadDocuments()

        return v
    }

    // ---------------------------------------------------------------------
    // LOAD DOCUMENT LIST FROM FIRESTORE
    // ---------------------------------------------------------------------
    private fun loadDocuments() {
        db.collection("vehicles").document(vehicleId)
            .get()
            .addOnSuccessListener { doc ->

                val docsMap = doc.get("documents") as? Map<String, Any> ?: emptyMap()

                if (docsMap.isEmpty()) {
                    Toast.makeText(requireContext(), "No documents uploaded", Toast.LENGTH_SHORT).show()
                    adapter.update(emptyList())
                    return@addOnSuccessListener
                }

                val list = docsMap.map { entry ->
                    VehicleDocument(
                        name = entry.key.uppercase(),               // e.g. INSURANCE / PUC
                        url = entry.value as? String ?: "",
                        fileName = "${entry.key}.pdf",               // auto-generate
                        uploadedAt = "-"                             // no date stored in DB
                    )
                }

                adapter.update(list)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load documents", Toast.LENGTH_SHORT).show()
            }
    }


    // ---------------------------------------------------------------------
    // OPEN DOCUMENT → CHECK LOCAL CACHE → OTHERWISE DOWNLOAD THEN OPEN
    // ---------------------------------------------------------------------
    private fun openDocument(document: VehicleDocument) {
        val cache = DocumentCacheManager(requireContext())

        // If already downloaded → open directly
        val existingFile = cache.getLocalFileIfExists(document.fileName)

        if (existingFile != null) {
            openWithExternalPdfViewer(existingFile)
        } else {
            // Download first time
            Toast.makeText(requireContext(), "Downloading...", Toast.LENGTH_SHORT).show()

            cache.downloadToLocal(document.url, document.fileName) { path ->
                requireActivity().runOnUiThread {
                    if (path != null) {
                        openWithExternalPdfViewer(File(path))
                    } else {
                        Toast.makeText(requireContext(), "Download failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // FORCE DOWNLOAD WHEN USER PRESSES THE BUTTON
    // ---------------------------------------------------------------------
    private fun downloadDocument(document: VehicleDocument) {

        val cache = DocumentCacheManager(requireContext())

        Toast.makeText(requireContext(), "Downloading...", Toast.LENGTH_SHORT).show()

        cache.downloadToLocal(document.url, document.fileName) { path ->
            requireActivity().runOnUiThread {
                if (path != null)
                    Toast.makeText(requireContext(), "Saved to Downloads", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(requireContext(), "Download failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------------------------------------------------------------------
    // OPEN PDF WITH ANY EXTERNAL VIEWER INSTALLED ON DEVICE
    // ---------------------------------------------------------------------
    private fun openWithExternalPdfViewer(file: File) {

        val uri = androidx.core.content.FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(Intent.createChooser(intent, "Open PDF With"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No PDF viewer installed", Toast.LENGTH_LONG).show()
        }
    }
}
