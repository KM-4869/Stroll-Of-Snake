package com.example.lbs

abstract class MeasurableSensor(
    protected val sensorType: Int
) {

    protected var onSensorValuesChanged: ((List<Float>, Long) -> Unit)? = null

    abstract val doesSensorExist: Boolean

    abstract fun startListening()
    abstract fun stopListening()

    fun setOnSensorValuesChangedListener(listener: (List<Float>, Long) -> Unit) {
        onSensorValuesChanged = listener
    }
}