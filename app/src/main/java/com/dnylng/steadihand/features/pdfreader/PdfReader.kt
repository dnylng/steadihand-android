package com.dnylng.steadihand.features.pdfreader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfReader(
    private val filename: String = FILENAME
) {

    companion object {
        private const val FILENAME = "scottpilgrim.pdf"
    }

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor

    @Throws(IOException::class)
    fun openRenderer(context: Context?) {
        if (context == null) return
        val file = File(context.cacheDir, filename)
        if (!file.exists()) {
            val asset = context.assets.open(filename)
            val output = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var size = asset.read(buffer)
            while (size != -1) {
                output.write(buffer, 0, size)
                size = asset.read(buffer)
            }
            asset.close()
            output.close()
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(parcelFileDescriptor)
        currentPage = pdfRenderer.openPage(0)
    }

    @Throws(IOException::class)
    fun closeRenderer() {
        currentPage.close()
        pdfRenderer.close()
        parcelFileDescriptor.close()
    }

    fun renderPage(pdf: ImageView, index: Int) {
        if (pdfRenderer.pageCount <= index) return
        currentPage.close()
        currentPage = pdfRenderer.openPage(index)
        val bitmap = createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
        pdf.setImageBitmap(bitmap)
    }

    fun getCurrentPage() = currentPage

    fun getPageCount() = pdfRenderer.pageCount
}