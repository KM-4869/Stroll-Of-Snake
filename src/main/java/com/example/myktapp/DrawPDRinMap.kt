package com.example.myktapp

import android.graphics.Color
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.PolylineOptions
import com.example.lbs.SensorsData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

var i = 0
var accl=FloatArray(dataNum){0.0f}//原始数据总的加速度数组
var yaw=FloatArray(dataNum){0.0f}//原始数据的航向角
var acclSmooth=FloatArray(dataNum){0.0f}//平滑后的加速度
var yawSmooth=FloatArray(dataNum){0.0f}//平滑后的航向角
var NE=FloatArray(2){0.0f}
var statrposition=FloatArray(3){0.0F}

fun DrawPDRinMap(sensorsData: SensorsData, isPDR:Boolean):Unit{

    if(!isPDR|| myaMaplocation==null){
        i = 0
        statrposition[0] = sensorsData.maplocationData[0].toFloat()
        statrposition[1] = sensorsData.maplocationData[1].toFloat()
        statrposition[2] = sensorsData.maplocationData[2].toFloat()
        NE[0] = 0f
        NE[1] = 0f
        PDRlatLngs.clear()
        return
    }


    accl[i] = sqrt( sensorsData.accelerometerData[0]*sensorsData.accelerometerData[0]+sensorsData.accelerometerData[1]*sensorsData.accelerometerData[1]+sensorsData.accelerometerData[2]*sensorsData.accelerometerData[2])

    yaw[i] = sensorsData.orientationAngles[0]

    if(i < dataNum - 1){
        i++
        return
    }else{

        acclSmooth = hwaSmooth(accl, smoothWindowInterval)
        //脚步探测
        val IsStep = footstepsDetection(acclSmooth)

        //yawSmooth = windowSmooth(yaw, smoothWindowInterval)

        val yawnow = yaw[i] * PI / 180.0

        if(IsStep){
            NE[0] += StepLength * cos(yawnow.toFloat())
            NE[1] += StepLength * sin(yawnow.toFloat())
            //绘制轨迹
            //定义一个经纬度
            val latlon= ne2blh(NE, statrposition)
            PDRlatLngs.add(LatLng(latlon[0].toDouble(), latlon[1].toDouble()))


                //aMaplocation 全局变量
                myaMaplocation!!.latitude = latlon[0].toDouble();
                myaMaplocation!!.longitude = latlon[1].toDouble();

                mapListener!!.onLocationChanged(myaMaplocation)
                //latLngs.add(desLatLng)
                aMap!!.addPolyline(
                    PolylineOptions().addAll(PDRlatLngs).width(30f).color(Color.argb(255, 0, 0, 0))
                )

        }

        for(j in 0..i-1){
            accl[j] = accl[j+1]
            yaw[j] = yaw[j+1]
        }
    }
}