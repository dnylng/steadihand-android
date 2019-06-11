package com.dnylng.steadihand.features.pdfreader

import android.app.Application
import android.hardware.Sensor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dnylng.steadihand.features.stabilization.StabilizationService
import com.dnylng.steadihand.util.LiveEvent
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfReaderViewModel @Inject constructor(
    application: Application,
    val stabilizationService: StabilizationService
) : AndroidViewModel(application) {

    private lateinit var stabilizationDisposable: Disposable
    val errorMessage: LiveEvent<String> = LiveEvent()
    val stabilizedRotation: MutableLiveData<FloatArray> = MutableLiveData()
    val stabilizedPosition: MutableLiveData<FloatArray> = MutableLiveData()
    var pageIndex = 0

    init {
        stabilizationSubscribe()
    }

    override fun onCleared() {
        super.onCleared()
        stabilizationUnsubscribe()
    }

    fun onStart() {

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
    }

    private fun stabilizationSubscribe() {
        stabilizationDisposable = stabilizationService.stabilizationObservable
            .subscribeOn(Schedulers.computation())
            .subscribe {
                when (it.first) {
                    Sensor.TYPE_GAME_ROTATION_VECTOR -> stabilizedRotation.postValue(it.second)
                    Sensor.TYPE_LINEAR_ACCELERATION -> stabilizedPosition.postValue(it.second)
                }
            }
    }

    private fun stabilizationUnsubscribe() {
        stabilizationDisposable.dispose()
    }
}