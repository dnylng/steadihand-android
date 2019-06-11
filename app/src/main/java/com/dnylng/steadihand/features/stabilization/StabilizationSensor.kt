package com.dnylng.steadihand.features.stabilization

import android.hardware.Sensor
import android.hardware.SensorEventListener

interface StabilizationSensor {

    fun registerListener(listener: SensorEventListener, sensor: Sensor?, sampling: Int)

    fun unregisterListener(listener: SensorEventListener, sensor: Sensor?)

    fun getSensor(type: Int): Sensor?
}