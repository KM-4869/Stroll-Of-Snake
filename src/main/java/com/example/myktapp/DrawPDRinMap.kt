package com.example.myktapp

import android.annotation.SuppressLint
import android.app.appsearch.SetSchemaResponse.MigrationFailure
import android.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.PolylineOptions
import com.example.lbs.SensorsData
import com.example.myktapp.ui.theme.Gold
import com.example.myktapp.ui.theme.Green1
import com.example.myktapp.ui.theme.KM5
import com.example.myktapp.ui.theme.MidnightBlue
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

fun DrawPDRinMap(sensorsData: SensorsData, isPDR:Boolean, easterEgg_touchcount:Int):Unit{

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


            //val colorList = ColorCalculator(MidnightBlue.toArgb(), Gold.toArgb(), PDRlatLngs.size)


            //aMaplocation 全局变量
            myaMaplocation!!.latitude = latlon[0].toDouble();
            myaMaplocation!!.longitude = latlon[1].toDouble();

            mapListener!!.onLocationChanged(myaMaplocation)
            //latLngs.add(desLatLng)
            if(easterEgg_touchcount>=10) {
                val colorList = assignRainbowColors2(PDRlatLngs.size)
                aMap!!.addPolyline(
                    PolylineOptions().addAll(PDRlatLngs).width(30f)
                        .colorValues(colorList)
                        .useGradient(true)
                )
            }
            else{
                aMap!!.addPolyline(
                    PolylineOptions().addAll(PDRlatLngs).width(30f)
                        .color(Color.argb(255, 0, 0, 0))
                )
            }
        }

        for(j in 0..i-1){
            accl[j] = accl[j+1]
            yaw[j] = yaw[j+1]
        }
    }
}

@SuppressLint("RestrictedApi")
fun ColorCalculator(colorStart:Int, colorEnd:Int, pointSize:Int):MutableList<Int>{

    var colorList:MutableList<Int> = MutableList(pointSize){0}

    val argbEvaluator = ArgbEvaluator()

   for(j in 0..pointSize - 1){

       val currentColor = argbEvaluator.evaluate(j.toFloat() / pointSize.toFloat(), colorStart, colorEnd) as Int//线性渐变
       //val currentColor = getNonlinearGradientColor(colorStart, colorEnd, j.toFloat() / pointSize.toFloat())//非线性渐变

       colorList[j] = currentColor;

   }

    return colorList
}

/**
 * 获取起始颜色和终止颜色之间的非线性渐变颜色值。
 * @param startColor 起始颜色
 * @param endColor 终止颜色
 * @param ratio 渐变比例，取值范围为0到1
 * @return 渐变颜色值
 */
fun getNonlinearGradientColor(startColor: Int, endColor: Int, ratio: Float): Int {
    val r = getNonlinearValue(startColor shr 16 and 0xff, endColor shr 16 and 0xff, ratio)
    val g = getNonlinearValue(startColor shr 8 and 0xff, endColor shr 8 and 0xff, ratio)
    val b = getNonlinearValue(startColor and 0xff, endColor and 0xff, ratio)
    return 0xff000000.toInt() or (r shl 16) or (g shl 8) or b
}

/**
 * 获取起始值和终止值之间的非线性插值。
 * @param start 起始值
 * @param end 终止值
 * @param ratio 渐变比例，取值范围为0到1
 * @return 插值结果
 */
fun getNonlinearValue(start: Int, end: Int, ratio: Float): Int {

    val t = ratio * ratio * (3 - 2 * ratio)
    return (start + (end - start) * t).toInt()
}

//彩虹色渐变（使用色相，0度为红色，120为绿色，240为蓝色）
fun assignRainbowColors(size: Int): MutableList<Int> {
    val colors = MutableList(size){0}

    for (j in 0 until size) {
        val hue = j.toFloat() / size.toFloat() * 360.0f
        val saturation = 1f
        val brightness = 1f
        colors[j] = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
    }

    return colors
}
//为PDR轨迹颜色计算彩虹色
fun assignRainbowColors2(size: Int): MutableList<Int> {
    val colors = MutableList(size){0}

    for (j in 0 until size) {
        val H = j.toFloat() / size.toFloat() * 360.0f + 180.0f
        val hue = if (H>360.0f) H-360.0f else H
        val saturation = 1f
        val brightness = 1f
        colors[j] = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
    }

    return colors
}




