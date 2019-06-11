package com.dnylng.steadihand.util

import android.hardware.SensorEvent

class Utils {

    companion object {

        const val NANO_TO_SEC = 1.0f / 1000000000.0f

        @JvmStatic
        fun lowPassFilter(input: FloatArray, alpha: Float = 0.8f): FloatArray {
            val output = FloatArray(input.size)
            for (i in input.indices)
                output[i] = output[i] + alpha * (input[i] - output[i]) // TODO the filter is intended for input to be 3 values that t-1 and output to be values at t http://utd.edu/~john.cole/ProgrammingTips/LowPassFilter.html
            return output
        }

        fun lowPassFilter(inputArrays: List<FloatArray>, alpha: Float = 0.8f): List<FloatArray> {
            val outputList = mutableListOf<FloatArray>()
            for (element in inputArrays) {
                outputList.add(lowPassFilter(element))
            }
            return outputList
        }

        @JvmStatic
        fun separateFloatArrays(list: List<SensorEvent>): List<FloatArray> {
            val floatArrayX = FloatArray(list.size)
            val floatArrayY = FloatArray(list.size)
            val floatArrayZ = FloatArray(list.size)
            for ((counter, event) in list.withIndex()) {
                floatArrayX[counter] = event.values[0]
                floatArrayY[counter] = event.values[1]
                floatArrayZ[counter] = event.values[2]
            }
            return listOf(floatArrayX, floatArrayY, floatArrayZ)
        }
    }
}