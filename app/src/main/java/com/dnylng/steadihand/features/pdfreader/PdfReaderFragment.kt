package com.dnylng.steadihand.features.pdfreader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dnylng.steadihand.SteadihandApplication
import com.dnylng.steadihand.di.viewmodel.ViewModelFactory
import com.dnylng.steadihand.util.snack
import javax.inject.Inject


class PdfReaderFragment : Fragment() {

    companion object {
        private val TAG = PdfReaderFragment::class.java.simpleName

        fun newInstance(): Fragment {
            return PdfReaderFragment()
        }
    }

    /**
     * Vars for the views
     */
    private lateinit var pdf: ImageView
    private lateinit var prevPdfBtn: View
    private lateinit var nextPdfBtn: View

    /**
     * Vars for the view model
     */
    @Inject
    lateinit var factory: ViewModelFactory
    private lateinit var viewModel: PdfReaderViewModel

    /**
     * Life Cycle methods
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SteadihandApplication.component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this, factory).get(PdfReaderViewModel::class.java)

        observePageIndex()
        observeError()
        observeRotation()
        observePosition()

        return inflater.inflate(com.dnylng.steadihand.R.layout.fragment_pdfreader, container, false).apply {
            pdf = findViewById<ImageView>(com.dnylng.steadihand.R.id.pdf).also {
                setOnLongClickListener {
                    viewModel.stabilizationService.reset()
                    true
                }
            }
            prevPdfBtn = findViewById<View>(com.dnylng.steadihand.R.id.prev_pdf_btn).also {
                it.setOnClickListener { viewModel.pageIndex.value = viewModel.pageIndex.value?.minus(1) }
            }
            nextPdfBtn = findViewById<View>(com.dnylng.steadihand.R.id.next_pdf_btn).also {
                it.setOnClickListener { viewModel.pageIndex.value = viewModel.pageIndex.value?.plus(1) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
        showPage(viewModel.pageIndex.value ?: 0)
    }

    override fun onStop() {
        viewModel.onStop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    /**
     * Observers
     */
    private fun observePageIndex() {
        viewModel.pageIndex.observe(this, Observer { index ->
            showPage(index)
            updateUi()
        })
    }

    private fun observeError() {
        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            view?.snack(errorMessage)
        })
    }

    private fun observeRotation() {
        viewModel.stabilizedRotation.observe(this, Observer { rotation ->
            if (rotation == null) return@Observer
            pdf.rotation = rotation[0]
            pdf.rotationX = rotation[1]
            pdf.rotationY = rotation[2]
        })
    }

    private fun observePosition() {
        viewModel.stabilizedPosition.observe(this, Observer { position ->
            if (position == null) return@Observer
            pdf.translationX = position[0]
            pdf.translationY = position[1]
        })
    }

    /**
     * Update UI methods
     */
    private fun showPage(index: Int) {
        val bitmap = viewModel.getBitmap(index) ?: return
        pdf.setImageBitmap(bitmap)
    }

    private fun updateUi() {
        val index = viewModel.pageIndex.value ?: return
        val pageCount = viewModel.getPageCount()
        prevPdfBtn.isEnabled = 0 != index
        nextPdfBtn.isEnabled = index + 1 < pageCount
    }
}
