package com.tomorrowit.arshops.presentation.shops

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.tomorrowit.arshops.R
import com.tomorrowit.arshops.databinding.ActivityShopsBinding
import com.tomorrowit.arshops.presentation.shops.fragments.LoadingFragment
import com.tomorrowit.arshops.presentation.shops.fragments.ShopsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

private val TAG: String = ShopsActivity::class.java.simpleName

@AndroidEntryPoint
class ShopsActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityShopsBinding

    private val viewModel: ShopsViewModel by viewModels()

    //region Sensor
    lateinit var sensorManager: SensorManager
    val accelerometerReading: FloatArray = FloatArray(3)
    val magnetometerReading: FloatArray = FloatArray(3)
    val rotationMatrix: FloatArray = FloatArray(9)
    val orientationAngles: FloatArray = FloatArray(3)
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShopsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = this.getSystemService(SENSOR_SERVICE) as SensorManager

        supportFragmentManager.commit {
            add(R.id.containerFragment, LoadingFragment())
        }
        //sensorManager = getSystemService()!!
    }

    override fun onStart() {
        super.onStart()
        observeState()
    }

    private fun observeState() {
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when (state) {
                is ShopState.SuccessShops -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ShopsActivity,
                            "Shops ready",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    withContext(Dispatchers.Main){
                        supportFragmentManager.commit {
                            replace(R.id.containerFragment, ShopsFragment(state.list))
                        }
                    }
                }

                else -> {

                }
            }
        }.launchIn(lifecycleScope)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } ?: run {
            // Handle the case where the magnetometer sensor is not available
            Log.e(TAG, "Magnetometer sensor not available")
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } ?: run {
            // Handle the case where the accelerometer sensor is not available
            Log.e(TAG, "Accelerometer sensor not available")
        }

        // Initialize the arrays here
        accelerometerReading.fill(0.0f)
        magnetometerReading.fill(0.0f)
    }


    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> event.values.copyInto(accelerometerReading)
            Sensor.TYPE_MAGNETIC_FIELD -> event.values.copyInto(magnetometerReading)
        }

        // Check if the arrays are non-null before using them
        if (accelerometerReading.none { it.isNaN() } && magnetometerReading.none { it.isNaN() }) {
            // Update rotation matrix, which is needed to update orientation angles.
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerReading,
                magnetometerReading
            )
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}