package studio.nox.renderer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/** Gravity-based parallax; does not require permissions or keep sensors on when hidden. */
class MotionSensor(context:Context,private val onMotion:(Float,Float)->Unit):SensorEventListener {
    private val manager=context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor=manager.getDefaultSensor(Sensor.TYPE_GRAVITY)?:manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var running=false
    fun start() {
        if(!running && sensor!=null)running=manager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_UI)
    }
    fun stop() { if(running)manager.unregisterListener(this);running=false }
    override fun onSensorChanged(event:SensorEvent) {
        if(!running)return
        onMotion((event.values[0]/9.81f).coerceIn(-1f,1f),(event.values[1]/9.81f).coerceIn(-1f,1f))
    }
    override fun onAccuracyChanged(sensor:Sensor?,accuracy:Int){}
}
