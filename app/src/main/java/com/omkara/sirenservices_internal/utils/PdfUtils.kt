package com.omkara.sirenservices_internal.utils

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.omkara.sirenservices_internal.models.BillComponent
import java.io.File
import java.io.FileOutputStream

object PdfUtils {

    fun generateBillPdf(
        context: Context,
        tripCost: Double,
        invoiceFor: String,
        components: List<BillComponent>,
        gstPercent: Double
    ): String {

        val file = File(context.getExternalFilesDir(null), "bill_${System.currentTimeMillis()}.pdf")
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        var y = 120f

        paint.textSize = 16f
        canvas.drawText("Invoice For: $invoiceFor", 40f, y, paint)
        y += 30
        canvas.drawText("OM AMBULANCE SERVICES", 40f, 40f, paint)
        canvas.drawText("Bill Summary", 40f, 80f, paint)

        //var y = 120f
        canvas.drawText("Trip Cost: ₹$tripCost", 40f, y, paint)
        y += 30

        components.forEach {
            canvas.drawText("${it.name}: ₹${it.amount}", 40f, y, paint)
            y += 25
        }

        if (gstPercent > 0) {
            canvas.drawText("GST ($gstPercent%) applied", 40f, y, paint)
            y += 25
        }

        canvas.drawText("Digitally Signed", 40f, 780f, paint)

        document.finishPage(page)
        document.writeTo(FileOutputStream(file))
        document.close()

        return file.absolutePath
    }

    fun openPdf(context: Context, path: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            FileProvider.getUriForFile(context, "${context.packageName}.provider", File(path)),
            "application/pdf"
        )
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.startActivity(intent)
    }
}
