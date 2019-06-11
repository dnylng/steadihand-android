package com.dnylng.steadihand.features.stabilization

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.dnylng.steadihand.util.Utils
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import io.reactivex.Observable


class StabilizationService(
    private val stabilizationSensor: StabilizationSensor
) {

    companion object {
        private val TAG = StabilizationService::class.java.simpleName
    }

    private var rotationSensor: Sensor? = null
    private var accelerometer: Sensor? = null
    private var isInitReading = true
    private val referenceAngles = FloatArray(4)
    private val velocity = floatArrayOf(0f, 0f, 0f)
    private val position = floatArrayOf(0f, 0f, 0f)
    private var acceleration = floatArrayOf(0f, 0f, 0f)
    private var timestamp = 0L
    private var sensitivity = 0.2f

    var stabilizationObservable: Observable<Pair<Int, FloatArray>> = Observable.create { e ->
        val sensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_GAME_ROTATION_VECTOR -> {
                        if (isInitReading) {
                            calcOrientationAngles(event.values).copyInto(referenceAngles)
                            isInitReading = false
                        } else {
                            val orientationAngles = calcOrientationAngles(event.values)
                            val orientation =
                                Quaternion(orientationAngles[1], orientationAngles[2], orientationAngles[0], 1f)
                            val rotate = Vector3(orientationAngles[1], orientationAngles[2], orientationAngles[0])
                            val result = Quaternion.rotateVector(orientation, rotate)

                            Log.d(TAG, "ROT -> x:${result.x}, y: ${result.y}, and z: ${result.z}")

                            e.onNext(Pair(event.sensor.type, floatArrayOf(
                                (referenceAngles[0] - result.z) * sensitivity,
                                (referenceAngles[1] - result.x) * sensitivity,
                                (referenceAngles[2] - result.y) * sensitivity
                            )))
                        }
                    }
                    Sensor.TYPE_LINEAR_ACCELERATION -> {
                        val eventValues = Utils.lowPassFilter(event.values, 0.8f)
                        if (timestamp == 0L) {
                            floatArrayOf(0f, 0f, 0f).apply {
                                copyInto(position)
                                copyInto(velocity)
                            }
                            eventValues.copyInto(acceleration)
                        } else {
                            val dt = (event.timestamp - timestamp) * Utils.NANO_TO_SEC
                            eventValues.copyInto(acceleration)

                            for (index in 0..1) {
                                velocity[index] += acceleration[index] * dt - sensitivity * velocity[index]
                                position[index] += velocity[index] * 12500 * dt - sensitivity * position[index]
                            }

                            Log.d(TAG, "VEL -> x:${velocity[0]}, and y: ${velocity[1]}")
                            Log.d(TAG, "POS -> x:${position[0]}, and y: ${position[1]}")
                        }
                        timestamp = event.timestamp

                        e.onNext(Pair(event.sensor.type, floatArrayOf(-position[0], position[1], 0f)))
                    }
                }
            }
        }
        stabilizationSensor.registerListener(sensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST)
        stabilizationSensor.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
    }

    init {
        stabilizationSensor.getSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)?.let {
            this.rotationSensor = it
        }
        stabilizationSensor.getSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.let {
            this.accelerometer = it
        }
    }

    fun reset() {
        isInitReading = true
        resetPosition(referenceAngles)
    }

    private fun calcOrientationAngles(rotationVector: FloatArray): FloatArray {
        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        val yawInDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        val pitchInDegrees = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
        val rollInDegrees = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

        return floatArrayOf(yawInDegrees, pitchInDegrees, rollInDegrees)
    }

    private fun resetPosition(rotationVector: FloatArray) {
        calcOrientationAngles(rotationVector).copyInto(referenceAngles)
    }
}