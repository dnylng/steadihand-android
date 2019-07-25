package com.dnylng.steadihand.features.pdfreader

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dnylng.steadihand.features.stabilization.StabilizationService
import com.dnylng.steadihand.util.LiveEvent
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfReaderViewModel @Inject constructor(
    val app: Application,
    val stabilizationService: StabilizationService
) : AndroidViewModel(app) {

    companion object {
        private const val FILENAME = "scottpilgrim.pdf"
        private val TAG = PdfReaderViewModel::class.java.simpleName
    }

    private lateinit var stabilizationDisposable: Disposable
    private val pdfReader = PdfReader(FILENAME)
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
        openPdfReader(app.applicationContext)
    }

    fun onResume() {
        stabilizationSubscribe()
        stabilizationService.reset()
    }

    fun onPause() {
        stabilizationUnsubscribe()
    }

    fun onStop() {
        stabilizationUnsubscribe()
        closePdfReader()
    }

    private fun openPdfReader(context: Context) {
        try {
            pdfReader.openRenderer(context)
        } catch (e: IOException) {
            errorMessage.value = "Failed to load PDF"
            Log.e(TAG, e.toString())
        }
    }

    private fun closePdfReader() {
        try {
            pdfReader.closeRenderer()
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
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

    fun getPageCount() = pdfReader.getPageCount()

    fun getBitmap(index: Int) = pdfReader.getBitmap(index)

    fun updatePageIndex(value: Int) {
        pageIndexData.value = pageIndexData.value?.plus(value)
    }
}