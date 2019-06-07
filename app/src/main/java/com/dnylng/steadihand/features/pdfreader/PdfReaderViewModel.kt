package com.dnylng.steadihand.features.pdfreader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dnylng.steadihand.util.LiveEvent
import javax.inject.Inject

class PdfReaderViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    val errorMessage: LiveEvent<String> = LiveEvent()
    var pageIndex = 0
}