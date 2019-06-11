package com.dnylng.steadihand.features.plotting

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.DataPointInterface
import com.jjoe64.graphview.series.LineGraphSeries
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject


/**
 * Created by WillowTree on 2019-06-11.
 */
class PlottingFragment : Fragment() {

    var sensorManager: SensorManager? = null
    private var rotationSensor: Sensor? = null
    private var accelerometer: Sensor? = null

    var dataSeriesX = LineGraphSeries<DataPointInterface>()
    var dataSeriesY = LineGraphSeries<DataPointInterface>()
    var dataSeriesZ = LineGraphSeries<DataPointInterface>()

    private val startTime = System.currentTimeMillis()

    private val proxy = BehaviorSubject.create<SensorEvent>()

    companion object {
        fun newInstance(): Fragment {
            return PlottingFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(com.dnylng.steadihand.R.layout.fragment_plotting, container, false)

        val graphView = rootView.findViewById<GraphView?>(com.dnylng.steadihand.R.id.graphView) // TODO alternatively we could do three different graphs
        graphView?.apply {
            addSeries(dataSeriesX)
            addSeries(dataSeriesY)
            addSeries(dataSeriesZ)
            viewport.isXAxisBoundsManual = true
//            viewport.setMinX(0.0)
//            viewport.setMaxX(100.0)
        }

        dataSeriesX.color = com.dnylng.steadihand.R.color.colorAccent
        dataSeriesZ.color = com.dnylng.steadihand.R.color.colorPrimary

        return rootView
    }

    override fun onStart() {
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensorManager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)?.let {
            this.rotationSensor = it
        }
        sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.let {
            this.accelerometer = it
        }

        val flowable = Flowable.just(0)

        super.onStart()
    }

    fun addToFlowable() {

    }
    override fun onResume() {
        register()
        super.onResume()
    }

    override fun onPause() {
        unregister()
        super.onPause()
    }

    fun register() {
        sensorManager?.registerListener(sensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        sensorManager?.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun unregister() {
        sensorManager?.unregisterListener(sensorEventListener)
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(rotationSensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_GAME_ROTATION_VECTOR -> {

                    val dataPointX = DataPoint(event.timestamp.toDouble() - startTime, event.values[0].toDouble())
                    val dataPointY = DataPoint(event.timestamp.toDouble() - startTime, event.values[1].toDouble())
                    val dataPointZ = DataPoint(event.timestamp.toDouble() - startTime, event.values[2].toDouble())

                    dataSeriesX.appendData(dataPointX, true, 100)
//                    dataSeriesY.appendData(dataPointY, true, 100)
//                    dataSeriesZ.appendData(dataPointZ, true, 100)
                    Log.d("PlottingFragment", "Event values: ${event.values} -- Data point: $dataPointX")

                }
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                   // TODO the same for gyro
                }
            }
        }
    }
}