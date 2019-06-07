package com.dnylng.steadihand.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * Variant of LiveData which emits only when data is set. It will not emit on resubscribe.
 */
class LiveEvent<T> : MutableLiveData<T>() {

    private var pending = false

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, Observer { t ->
            if (pending) {
                pending = false
                observer.onChanged(t)
            }
        })
    }

    override fun setValue(value: T?) {
        pending = true
        super.setValue(value)
    }

    /**
     * Used when we want a LiveEvent of type Void.
     */
    fun call() {
        value = null
    }

}