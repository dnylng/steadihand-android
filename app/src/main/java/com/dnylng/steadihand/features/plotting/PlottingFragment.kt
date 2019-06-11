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
import com.dnylng.steadihand.util.Utils
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.DataPointInterface
import com.jjoe64.graphview.series.LineGraphSeries
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import uk.me.berndporr.iirj.Butterworth




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

    // Reactive stuff inspired by https://www.kotlindevelopment.com/reactive-sensor-monitoring/
    private val linearProxy = BehaviorSubject.create<SensorEvent>()
    private val rotationProxy = BehaviorSubject.create<SensorEvent>()

    private var disposableLinear: Disposable? = null

    var butterworth = Butterworth()

    companion object {
        private val TAG = PlottingFragment::class.java.simpleName
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

        disposableLinear = linearProxy.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .buffer(10)
            .subscribe {
                val floatArrays = Utils.separateFloatArrays(it)
                val filteredArrays = Utils.lowPassFilter(floatArrays)
                Log.d(TAG, "Sensor Event from Observable: ${it.size} - Raw: $floatArrays - Filtered: $filteredArrays")
            }


        butterworth.lowPass(10, 100.0, 50.0)

        super.onStart()
    }

    override fun onResume() {
        register()
        super.onResume()
    }

    override fun onPause() {
        unregister()
        super.onPause()
    }

    override fun onDestroy() {
        disposableLinear?.dispose()
        super.onDestroy()
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

                    linearProxy.onNext(event)

                    val dataPointX = DataPoint(event.timestamp.toDouble() - startTime, event.values[0].toDouble())
                    val dataPointY = DataPoint(event.timestamp.toDouble() - startTime, event.values[1].toDouble())
                    val dataPointZ = DataPoint(event.timestamp.toDouble() - startTime, event.values[2].toDouble())

                    val filteredX = butterworth.filter(event.values[0].toDouble())
                    val filteredDataPointX = DataPoint(event.timestamp.toDouble() - startTime, filteredX)

                    dataSeriesX.appendData(dataPointX, true, 100)
                    dataSeriesY.appendData(filteredDataPointX, true, 100)

//                    dataSeriesY.appendData(dataPointY, true, 100)
//                    dataSeriesZ.appendData(dataPointZ, true, 100)
                    Log.d(TAG, "Event values: ${event.values} -- Data point: $dataPointX")

                }
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                    rotationProxy.onNext(event)
                   // TODO the same for gyro
                }
            }
        }
    }

}