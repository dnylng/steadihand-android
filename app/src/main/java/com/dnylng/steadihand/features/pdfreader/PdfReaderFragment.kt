package com.dnylng.steadihand.features.pdfreader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import com.dnylng.steadihand.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class PdfReaderFragment : Fragment() {

    companion object {
        private const val KEY = "FragmentKey"
        private const val SAVED_STATE_PAGE_IDX_KEY = "PageIndexKey"
        private const val FILENAME = "scottpilgrim.pdf"
        private val TAG = PdfReaderFragment::class.java.simpleName
        fun newInstance(key: String): Fragment {
            val fragment = PdfReaderFragment()
            val arguments = Bundle()
            arguments.putString(KEY, key)
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var pdf: ImageView
    private lateinit var prevPdfBtn: FloatingActionButton
    private lateinit var nextPdfBtn: FloatingActionButton
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private var pageIdx = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pdfreader, container, false)
        view.apply {
            pdf = findViewById(R.id.pdf)
            prevPdfBtn = findViewById<FloatingActionButton>(R.id.prev_pdf_btn).also { it.setOnClickListener { showPage(currentPage.index - 1) } }
            nextPdfBtn = findViewById<FloatingActionButton>(R.id.next_pdf_btn).also { it.setOnClickListener { showPage(currentPage.index + 1) } }
        }
        return view
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        try {
            openRenderer(activity)
            showPage(pageIdx)
        } catch (e: IOException) {
            Log.d(TAG, e.toString())
        }
    }

    override fun onStop() {
        try {
            closeRenderer()
        } catch (e: IOException) {
            Log.d(TAG, e.toString())
        }
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SAVED_STATE_PAGE_IDX_KEY, currentPage.index)
        super.onSaveInstanceState(outState)
    }

    @Throws(IOException::class)
    private fun openRenderer(context: Context?) {
        if (context == null) return
        val file = File(context.cacheDir, FILENAME)
        if (!file.exists()) {
            val asset = context.assets.open(FILENAME)
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
        currentPage = pdfRenderer.openPage(pageIdx)
    }

    @Throws(IOException::class)
    private fun closeRenderer() {
        currentPage.close()
        pdfRenderer.close()
        parcelFileDescriptor.close()
    }

    private fun showPage(index: Int) {
        if (pdfRenderer.pageCount <= index) return
        currentPage.close()
        currentPage = pdfRenderer.openPage(index)
        val bitmap = createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
        pdf.setImageBitmap(bitmap)
        updateUi()
    }

    private fun updateUi() {
        val index = currentPage.index
        val pageCount = pdfRenderer.pageCount
        prevPdfBtn.isEnabled = (0 != index)
        nextPdfBtn.isEnabled = (index + 1 < pageCount)
//        activity?.title = getString(R.string.app_name_with_index, index + 1, pageCount)
    }
}