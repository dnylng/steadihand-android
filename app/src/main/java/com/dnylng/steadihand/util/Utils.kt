package com.dnylng.steadihand.util

class Utils {

    companion object {

        const val NANO_TO_SEC = 1.0f / 1000000000.0f

        @JvmStatic
        fun lowPassFilter(input: FloatArray, alpha: Float): FloatArray {
            val output = FloatArray(input.size)
            for (i in input.indices)
                output[i] = output[i] + alpha * (input[i] - output[i]) // TODO the index on output should be i-1, maybe according to some sources (is there a library for applying another sort of filter)
            return output
        }
    }
}