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
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var prevPdfBtn: FloatingActionButton
    private lateinit var nextPdfBtn: FloatingActionButton
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private var pageIdx = 0

    private val onLongClickListener = View.OnLongClickListener {
        isInitReading = true
        resetPosition(referenceAngles)
        true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (savedInstanceState != null) {
            pageIdx = savedInstanceState.getInt(SAVED_STATE_PAGE_IDX_KEY)
        }

        val view = inflater.inflate(com.dnylng.steadihand.R.layout.fragment_pdfreader, container, false)
        view.apply {
            pdf = findViewById(com.dnylng.steadihand.R.id.pdf)
            prevPdfBtn = findViewById<FloatingActionButton>(com.dnylng.steadihand.R.id.prev_pdf_btn).also { it.setOnClickListener { showPage(currentPage.index - 1) } }
            nextPdfBtn = findViewById<FloatingActionButton>(com.dnylng.steadihand.R.id.next_pdf_btn).also { it.setOnClickListener { showPage(currentPage.index + 1) } }
        }
        pdf.setOnLongClickListener(onLongClickListener)
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

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(rotationSensor: Sensor?, accuracy: Int) { }

        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null || rotationSensor == null || accelerometer == null) return

            when (event.sensor.type) {
                Sensor.TYPE_GAME_ROTATION_VECTOR -> {
                    if (isInitReading) {
                        calcOrientaionAngles(event.values).copyInto(referenceAngles)
                        isInitReading = false
                    } else {
                        val orientationAngles = calcOrientaionAngles(event.values)
                        val yaw = orientationAngles[0]
                        val pitch = orientationAngles[1]
                        val roll = orientationAngles[2]

                        Log.d(TAG, "ROT VEC -> Yaw: $yaw, Pitch: $pitch, and Roll: $roll")

                        pdf.rotation = -yaw + referenceAngles[0]
                        pdf.rotationX = -pitch + referenceAngles[1]
                        pdf.rotationY = -roll + referenceAngles[2]
                    }
                }
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    Log.d(TAG, "LIN ACC -> x: $x, y: $y, and z: $z")
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
