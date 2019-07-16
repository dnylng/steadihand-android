package com.dnylng.steadihand.features.pdfreader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfReader {

    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    val isOpen
        get() = pdfRenderer == null || currentPage == null || parcelFileDescriptor == null

    @Throws(IOException::class)
    fun open(context: Context?, filename: String = "") {
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
        parcelFileDescriptor?.let { pdfRenderer = PdfRenderer(it) }
        currentPage = pdfRenderer?.openPage(0)
    }

    @Throws(IOException::class)
    fun close() {
        currentPage?.close()
        pdfRenderer?.close()
        parcelFileDescriptor?.close()
    }

    fun getBitmap(index: Int): Bitmap? {
        val pageCount = pdfRenderer?.pageCount ?: return null
        if (pageCount <= index || index < 0) return null
        currentPage?.close()
        currentPage = pdfRenderer?.openPage(index) ?: return null
        val width = currentPage?.width ?: return null
        val height = currentPage?.height ?: return null
        val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        currentPage?.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
        return bitmap
    }

    fun getPageCount() = pdfRenderer?.pageCount
}