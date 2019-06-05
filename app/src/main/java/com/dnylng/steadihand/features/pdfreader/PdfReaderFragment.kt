package com.dnylng.steadihand.features.pdfreader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import com.dnylng.steadihand.util.Utils
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class PdfReaderFragment : Fragment() {

    companion object {
        private const val KEY = "FragmentKey"
        private const val SAVED_STATE_PAGE_IDX_KEY = "PageIndexKey"
        private const val FILENAME = "scottpilgrim.pdf"
        private val TAG = PdfReaderFragment::class.java.simpleName
        fun newInstance(key: String): Fragment {
            val fragment = PdfReaderFragment()
            val arguments = Bundle()
            arguments.putString(KEY, key)
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var pdf: ImageView
    private lateinit var prevPdfBtn: View
    private lateinit var nextPdfBtn: View
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private var pageIdx = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (savedInstanceState != null) {
            pageIdx = savedInstanceState.getInt(SAVED_STATE_PAGE_IDX_KEY)
        }

        val view = inflater.inflate(com.dnylng.steadihand.R.layout.fragment_pdfreader, container, false)
        view.apply {
            pdf = findViewById<ImageView>(com.dnylng.steadihand.R.id.pdf).also {
                setOnLongClickListener {
                isInitReading = true
                resetPosition(referenceAngles)
                true
            }
            }
            prevPdfBtn = findViewById<View>(com.dnylng.steadihand.R.id.prev_pdf_btn).also {
                it.setOnClickListener { showPage(currentPage.index - 1) }
            }
            nextPdfBtn = findViewById<View>(com.dnylng.steadihand.R.id.next_pdf_btn).also {
                it.setOnClickListener { showPage(currentPage.index + 1) }
            }
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        try {
            openRenderer(activity)
            showPage(pageIdx)
        } catch (e: IOException) {
            Log.d(TAG, e.toString())
        }

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)?.let {
            this.rotationSensor = it
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.let {
            this.accelerometer = it
        }
    }

    override fun onStop() {
        try {
            closeRenderer()
        } catch (e: IOException) {
            Log.d(TAG, e.toString())
        }
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SAVED_STATE_PAGE_IDX_KEY, currentPage.index)
        super.onSaveInstanceState(outState)
    }

    @Throws(IOException::class)
    private fun openRenderer(context: Context?) {
        if (context == null) return
        val file = File(context.cacheDir, FILENAME)
        if (!file.exists()) {
            val asset = context.assets.open(FILENAME)
            val output = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var size = asset.read(buffer)
            while (size != -1) {
                output.write(buffer, 0, size)
                size = asset.read(buffer)
            }
            asset.close()
            output.close()
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(parcelFileDescriptor)
        currentPage = pdfRenderer.openPage(pageIdx)
    }

    @Throws(IOException::class)
    private fun closeRenderer() {
        currentPage.close()
        pdfRenderer.close()
        parcelFileDescriptor.close()
    }

    private fun showPage(index: Int) {
        if (pdfRenderer.pageCount <= index) return
        currentPage.close()
        currentPage = pdfRenderer.openPage(index)
        val bitmap = createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
        pdf.setImageBitmap(bitmap)
        updateUi()
    }

    private fun updateUi() {
        val index = currentPage.index
        val pageCount = pdfRenderer.pageCount
        prevPdfBtn.isEnabled = (0 != index)
        nextPdfBtn.isEnabled = (index + 1 < pageCount)
    }

    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null
    private var accelerometer: Sensor? = null
    private var isInitReading = true
    private val referenceAngles = FloatArray(4)
    private val velocity = floatArrayOf(0f, 0f, 0f)
    private val position = floatArrayOf(0f, 0f, 0f)
    private var acceleration = floatArrayOf(0f, 0f, 0f)
    private var timestamp = 0L
    private val referencePosition = intArrayOf(0, 0, 0)
    private var sensitivity = 0.2f

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(rotationSensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_GAME_ROTATION_VECTOR -> {
                    if (isInitReading) {
                        calcOrientaionAngles(event.values).copyInto(referenceAngles)
                        isInitReading = false
                    } else {
                        val orientationAngles = calcOrientaionAngles(event.values)
                        val orientation = Quaternion(orientationAngles[1], orientationAngles[2], orientationAngles[0], 1f)
                        val rotate = Vector3(orientationAngles[1], orientationAngles[2], orientationAngles[0])
                        val result = Quaternion.rotateVector(orientation, rotate)

                        Log.d(TAG, "ROT -> x:${result.x}, y: ${result.y}, and z: ${result.z}")

                        pdf.rotation = (referenceAngles[0] - result.z) * sensitivity
                        pdf.rotationX = (referenceAngles[1] - result.x) * sensitivity
                        pdf.rotationY = (referenceAngles[2] - result.y) * sensitivity
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
                        pdf.getLocationOnScreen(referencePosition)
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
                    pdf.translationX = -position[0]
                    pdf.translationY = position[1]
                }
            }
        }
    }

    private fun calcOrientaionAngles(rotationVector: FloatArray): FloatArray {
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
        calcOrientaionAngles(rotationVector).copyInto(referenceAngles)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(sensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorEventListener)
    }
}
