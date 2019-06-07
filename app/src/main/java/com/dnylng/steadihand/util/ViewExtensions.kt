package com.dnylng.steadihand.util

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.snack(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT) = Snackbar.make(
    this,
    message,
    duration
).show()