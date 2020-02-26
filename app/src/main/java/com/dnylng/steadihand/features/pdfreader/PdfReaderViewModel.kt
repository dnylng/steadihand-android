package com.dnylng.steadihand.features.pdfreader

import android.app.Application
import android.hardware.Sensor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dnylng.steadihand.features.stabilization.StabilizationService
import com.dnylng.steadihand.util.LiveEvent
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException

class PdfReaderViewModel constructor(
    private val app: Application,
    private val stabilizationService: StabilizationService,
    private val pdfReader: PdfReader
) : AndroidViewModel(app) {

    companion object {
        private const val FILENAME = "scottpilgrim.pdf"
        private val TAG = PdfReaderViewModel::class.java.simpleName
    }

    private lateinit var stabilizationDisposable: Disposable
    val errorMessage: LiveEvent<String> = LiveEvent()
    private val stabilizedRotationData: MutableLiveData<FloatArray> = MutableLiveData()
    val stabilizedRotation: LiveData<FloatArray> = stabilizedRotationData
    private val stabilizedPositionData: MutableLiveData<FloatArray> = MutableLiveData()
    val stabilizedPosition: LiveData<FloatArray> = stabilizedPositionData
    private val pageIndexData: MutableLiveData<Int> = MutableLiveData()
    val pageIndex: LiveData<Int> = pageIndexData

    init {
        pageIndexData.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        stabilizationUnsubscribe()
    }

    fun onStart() {
        stabilizationSubscribe()
        openPdfReader(FILENAME)
    }

    fun onResume() {
        stabilizationSubscribe()
        resetStabilization()
    }

    fun onPause() {
        stabilizationUnsubscribe()
    }

    fun onStop() {
        stabilizationUnsubscribe()
        closePdfReader()
    }

    private fun openPdfReader(filename: String = "") {
        try {
            if (pdfReader.isOpen) pdfReader.close()
            pdfReader.open(app, filename)
        } catch (e: IOException) {
            errorMessage.value = "Failed to load PDF"
            Timber.e(TAG, e.toString())
        }
    }

    private fun closePdfReader() {
        try {
            pdfReader.close()
        } catch (e: IOException) {
            Timber.e(TAG, e.toString())
        }
    }

    private fun stabilizationSubscribe() {
        stabilizationDisposable = stabilizationService.stabilizationObservable
            .subscribeOn(Schedulers.computation())
            .subscribe {
                when (it.first) {
                    Sensor.TYPE_GAME_ROTATION_VECTOR -> stabilizedRotationData.postValue(it.second)
                    Sensor.TYPE_LINEAR_ACCELERATION -> stabilizedPositionData.postValue(it.second)
                }
            }
    }

    private fun stabilizationUnsubscribe() {
        stabilizationDisposable.dispose()
    }

    fun resetStabilization() {
        stabilizationService.reset()
    }

    fun getPageCount() = pdfReader.getPageCount()

    fun getBitmap(index: Int) = pdfReader.getBitmap(index)

    fun updatePageIndex(value: Int) {
        pageIndexData.value = pageIndexData.value?.plus(value)
    }
}