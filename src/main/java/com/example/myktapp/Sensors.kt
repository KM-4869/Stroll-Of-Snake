package com.example.lbs

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import kotlin.math.PI

class AccelerometerSensor(
    context: Context
): AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_ACCELEROMETER,
    sensorType = Sensor.TYPE_ACCELEROMETER
)

class GyroscopeSensor(
    context: Context
): AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_GYROSCOPE,
    sensorType = Sensor.TYPE_GYROSCOPE
)

class MagnetometerSensor(
    context: Context
): AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_COMPASS,
    sensorType = Sensor.TYPE_MAGNETIC_FIELD
)

data class MySensors(val context: Context) {
    val accelerometerSensor = AccelerometerSensor(context)
    val gyroscopeSensor = GyroscopeSensor(context)
    val magnetometerSensor = MagnetometerSensor(context)
    val locationUtils = LocationUtils(context)

    fun calculateOrientation(gravity:FloatArray,magn:FloatArray,getOrientation:(FloatArray)->Unit) {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            gravity,
            magn
        )
        var orientationAngles = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        for (i in 0..2) orientationAngles[i] = (orientationAngles[i] * 180.0 / PI).toFloat()
        if (orientationAngles[0] < 0) orientationAngles[0] =
            (orientationAngles[0] + 360.0).toFloat()

        getOrientation(orientationAngles)
    }
}

data class SensorsData(
    val accelerometerData: List<Float> = List(3) { 0f },
    val gyroscopeData: List<Float> = List(3) { 0f },
    val magnetometerData: List<Float> = List(3) { 0f },
    val locationData: List<Double> = List(3) { 0.0 },//传感器读取
    val orientationAngles: FloatArray = FloatArray(3){0.0F},
    val maplocationData: DoubleArray = DoubleArray(3){0.0}//高德地图获取
)

data class SensorsDataTime(
    val accelerometerDataTime: Long = 0,
    val gyroscopeDataTime: Long = 0,
    val magnetometerDataTime: Long = 0,
    val locationDataTime: Long = 0
)

data class SensorsDataDate(
    val accelerometerDataDate: String = "",
    val gyroscopeDataDate: String = "",
    val magnetometerDataDate: String = "",
    val locationDataDate: String = ""
)