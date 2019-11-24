package com.dnylng.steadihand.features.stabilization

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SteadihandSensor constructor(
    val app: Application
): StabilizationSensor {

    private val sensorManager = app.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    override fun registerListener(listener: SensorEventListener, sensor: Sensor?, sampling: Int) {
        sensorManager.registerListener(listener, sensor, sampling)
    }

    override fun unregisterListener(listener: SensorEventListener, sensor: Sensor?) {
        sensorManager.unregisterListener(listener, sensor)
    }

    override fun getSensor(type: Int): Sensor? = sensorManager.getDefaultSensor(type)
}