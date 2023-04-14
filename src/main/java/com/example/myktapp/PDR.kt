package com.example.myktapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import java.lang.String
import kotlin.math.*

//WMA平滑
fun wmaSmooth(data:FloatArray,epch:Int):FloatArray{
    var array=data.copyOf()
    for(i in epch until  dataNum){
        var sum=0F
        for(j in 0..epch-1){
            sum+=(epch-j)*data[i-1-j]
        }
        sum=(2*sum)/(epch*(epch+1))
        array[i]=sum
    }
    return array
}

//HWA平滑
fun hwaSmooth(data: FloatArray, epch:Int):FloatArray{
    var array1=wmaSmooth(data,epch/2)
    var array2=wmaSmooth(data,epch)
    var array3=FloatArray(data.size)
    for(i in 0..data.size-1){
        array3[i]=array1[i]*2.0F-array2[i]
    }
    return wmaSmooth(array3, sqrt(epch.toFloat()).toInt())
}

fun windowSmooth(data: FloatArray, epch:Int):FloatArray{
    var sum=0F
    var array=data.copyOf()
    for(i in data.size-epch..data.size-1)sum+=data[i]
    sum=sum/epch
    array[data.size-1]=sum
    return array
}

//脚步探测
fun footstepsDetection(data:FloatArray):Boolean{
    var step=FloatArray(data.size)
    //实际会滞后一个历元
    //使用数组存储探测到的脚步的加速度，实际上只能记录到data.size-2
    //如若step[data.size-2]有数据，那就粗略认为t时刻有脚步(采样率高，一个时刻的滞后可以忽略)
    for(i in 1..data.size-2){
        if(data[i]>12F&&data[i]>data[i-1]&&data[i]>data[i+1]){
            //此处应加阈值限制假脚步
            step[i]=data[i]
        }
    }
    if(step[data.size-2]!=0.0F) return true
    else return false

}

//步长估计
fun stepLengthEstimate(time:FloatArray,H:Float):Float{
    //var Sf=1.0F/(0.8F*(time[2]-time[1])+0.2F*(time[1]-time[0]))
    var SF=1.0F/(time[2]-time[1])
    return (0.7*0.371*(H-1.6)+0.227*(SF-1.79)*H/1.6).toFloat() //H单位是米？
}

//航向估计
fun yaw(accl:FloatArray,magn:FloatArray):Float{
    var theta= atan2(accl[0], sqrt(accl[1]*accl[1]+accl[2]*accl[2]))
    var roll= atan2(-accl[1],-accl[2])
    var mx=magn[0]* cos(theta)+magn[1]* sin(roll)* sin(theta)+magn[2]* cos(roll)* sin(theta)
    var my=magn[1]* cos(roll)-magn[2]* sin(roll)
    var phim=-atan2(my,mx)
    var magneticDeclination=-(4+55F/60F)    //磁偏角：-4°55′
    return (phim+magneticDeclination* PI/180.0F ).toFloat()
}

//航迹推算
fun pdr(NE:FloatArray, SL:Float,yaw:Float):FloatArray{
    var NE1=FloatArray(2)
    NE1[0]=NE[0]+SL* cos(yaw)
    NE1[1]=NE[1]+SL* sin(yaw)
    return NE1
}

//ENU转BL
fun ne2blh(NE:FloatArray,blh:FloatArray):FloatArray{
    var bl=FloatArray(2)
    val a=6378137F
    val e2=0.00669437999013F
    var rm=a*(1-e2)/ sqrt((1-e2*sin(blh[0]*PI/180F)*sin(blh[0]*PI/180F))*(1-e2*sin(blh[0]*PI/180F)*sin(blh[0]*PI/180F))*(1-e2*sin(blh[0]*PI/180F)*sin(blh[0]*PI/180F))).toFloat()
    var rn=a/ sqrt((1-e2*sin(blh[0]*PI/180F)*sin(blh[0]*PI/180F))).toFloat()
    bl[0]=((NE[0]/rm+(blh[0]*PI/180F))*180F/ PI).toFloat()
    bl[1]=((NE[1]/rn+(blh[1]*PI/180F))*180F/ PI).toFloat()
    return bl
}

////绘图
//fun traceChart() {
//    //将imageView绑定到布局界面中id为image的IMageView
//    var imageView = myBinding!!.mapView
//    //以下内容的实现可以看我的另一个博客的详细解释，上有链接
////    val w = 1600
////    val h = 2800
////    var newb =
////        Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
//    var canvasTemp = Canvas(traceBitmap!!)
//    canvasTemp.drawColor(Color.TRANSPARENT)
//    var p = Paint()
//    //防锯齿
//    p.setAntiAlias(true)
//    p.setStyle(Paint.Style.STROKE) //STROKE,FILL
//    p.setStrokeWidth(5F)
//    p.setColor(Color.LTGRAY)
//    p.setTextAlign(Paint.Align.CENTER)
//    p.setTextSize(20F)
//    p.setColor(Color.BLACK)
//    p.setStyle(Paint.Style.FILL) //STROKE,FILL
//    p.setStrokeWidth(2F)
//    var p2 = p
//    /*绘制表格，表格的绘制是有规律的，我先确认原点和x，y轴的长度，然后通过原点的的坐标和x，y
//    轴的长度就可以得到x，y轴的终点坐标，最终通过两点确定一条直线就可以画出x，y轴了。*/
//    var startx1 = 100F
//    var starty1 = 2700F
//    var endx1 = 1500F
//    var endy1 = 100F
//    canvasTemp.drawText(
//        "-50/-50",
//        startx1 - 10,
//        starty1 + 30,
//        p
//    ) //原点
//    var allX = 100
//    val allY = 100
//    val fully= 50
//
//    val yinterval = (starty1-endy1)/allY
//    val xinterval = (endx1-startx1)/allX
//
//    canvasTemp.drawLine(startx1, starty1, endx1 + 50, starty1, p) // 绘制x轴
//    canvasTemp.drawText("E", endx1 + 50 + 10, starty1 + 35, p) //标记x轴
//    canvasTemp.drawLine(startx1, starty1, startx1, endy1 - 50, p) // 绘制y轴
//    canvasTemp.drawText("N", startx1 - 15, endy1 - 50, p) //标记y轴
//    //绘制x y轴箭头，与上同理，两点确定一条直线进行绘制
//    canvasTemp.drawLine(endx1 + 50, starty1, endx1 + 50 - 10, starty1 - 10, p)
//    canvasTemp.drawLine(endx1 + 50, starty1, endx1 + 50 - 10, starty1 + 10, p)
//    canvasTemp.drawLine(startx1, endy1 - 50, startx1 - 10, endy1 - 50 + 10, p)
//    canvasTemp.drawLine(startx1, endy1 - 50, startx1 + 10, endy1 - 50 + 10, p)
//    /*以下是绘制间隔线，间隔线可以理解为缩短了的平移了的x，y轴（也就是说是x，y轴的缩短和平
//    移——>两点确定一条直线中的两坐标变化），确定好x，y轴的间隔线间距就可以通过x或者y坐标增加
//    或减少相应间隔倍数就可以实现间隔线的规律绘制*/
//    //绘制x轴间隔线
//
//    for (i in 1..allX) {
//        canvasTemp.drawLine(
//            startx1 + xinterval * i,
//            starty1,
//            startx1 + xinterval * i,
//            starty1 - 10,
//            p
//        )
//        if (allX > 30) {
//            p2.setTextSize(20F)
//        }
//        canvasTemp.drawText((i-50).toString(), startx1 + xinterval * i, starty1 + 35, p2)
//    }
//    //绘制y轴间隔线
//    for (i in 1..allY) {
//        canvasTemp.drawLine(
//            startx1,
//            starty1 - yinterval * i,
//            startx1 + 10,
//            starty1 - yinterval * i,
//            p
//        )
//        if (allX > 25) {
//            p2.setTextSize(20F)
//        } else {
//            p2.setTextSize(35F)
//            canvasTemp.drawText(
//                String.valueOf(i * fully / allY),
//                startx1 - 55,
//                starty1 - yinterval * i + 10,
//                p2
//            )
//        }
//        canvasTemp.drawText((i-50).toString(), startx1 - 35, starty1 - yinterval * i, p2)
//    }
//    //将绘制的内容在布局文件中的IMageView中显示
//    imageView.setImageBitmap(traceBitmap)
//    //return newb
//}
//
//fun addPoint(NEk1:FloatArray,NEk:FloatArray){
//
//    var canvasTemp = Canvas(traceBitmap!!)
//    canvasTemp.drawColor(Color.TRANSPARENT)
//    var p = Paint()
//    //防锯齿
//    p.setAntiAlias(true)
//    p.setStyle(Paint.Style.STROKE) //STROKE,FILL
//    p.setStrokeWidth(10F)
//    p.setColor(Color.LTGRAY)
//    p.setTextAlign(Paint.Align.CENTER)
//    p.setTextSize(20F)
//    p.setColor(Color.BLACK)
//    p.setStyle(Paint.Style.FILL) //STROKE,FILL
//
//
//    var startx1 = 100F
//    var starty1 = 2700F
//    var endx1 = 1500F
//    var endy1 = 100F
//    var allX = 100
//    val allY = 100
//
//    val yinterval = (starty1-endy1)/allY
//    val xinterval = (endx1-startx1)/allX
//
//    var x1=startx1 + xinterval *(NEk1[1]+50)
//    var y1=starty1 - (NEk1[0]+50)*yinterval//北方向
//    var x2=startx1 + xinterval *(NEk[1]+50)
//    var y2=starty1 - (NEk[0]+50)*yinterval//北方向
//    canvasTemp.drawPoint(x2, y2, p)
//    canvasTemp.drawLine(x1, y1, x2, y2, p)
//
//    //将绘制的内容在布局文件中的IMageView中显示
//    myBinding!!.mapView.setImageBitmap(traceBitmap)
//
//}
@Composable
fun acclChart(accl:Array<FloatArray>, context: Context) {
    //将imageView绑定到布局界面中id为image的IMageView
    //var imageView = dataBinding!!.acclchart
    //var imageView = ImageView(context)
    val w = 1600
    val h = 1000
    var newb =
        Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    var canvasTemp = Canvas(newb)
    //Canvas canvasTemp2=new Canvas(newb);
    canvasTemp.drawColor(Color.BLACK)
    var p = Paint()
    //防锯齿
    p.setAntiAlias(true)
    p.setStyle(Paint.Style.STROKE) //STROKE,FILL
    p.setStrokeWidth(5F)
    p.setColor(Color.LTGRAY)
    p.setTextAlign(Paint.Align.CENTER)
    p.setTextSize(40F)
    p.setColor(Color.BLACK)
    p.setStyle(Paint.Style.FILL) //STROKE,FILL
    p.setStrokeWidth(2F)
    var p2 = p
    /*绘制表格，表格的绘制是有规律的，我先确认原点和x，y轴的长度，然后通过原点的的坐标和x，y
    轴的长度就可以得到x，y轴的终点坐标，最终通过两点确定一条直线就可以画出x，y轴了。*/
    var startx1 = 100F
    var starty1 = 900F
    var endx1 = 1500F
    var endy1 = 100F
    canvasTemp.drawText(
        "0",
        startx1 - 10,
        starty1 + 30,
        p
    ) //原点
    var allX = dataNum
    val allY = 20
    val fully= 20

    val yinterval = (starty1-endy1)/allY
    val xinterval = (endx1-startx1)/allX

    canvasTemp.drawLine(startx1, starty1, endx1 + 50, starty1, p) // 绘制x轴
    canvasTemp.drawText("x", endx1 + 50 + 10, starty1 + 35, p) //标记x轴
    canvasTemp.drawLine(startx1, starty1, startx1, endy1 - 50, p) // 绘制y轴
    canvasTemp.drawText("y", startx1 - 15, endy1 - 50, p) //标记y轴
    //绘制x y轴箭头，与上同理，两点确定一条直线进行绘制
    canvasTemp.drawLine(endx1 + 50, starty1, endx1 + 50 - 10, starty1 - 10, p)
    canvasTemp.drawLine(endx1 + 50, starty1, endx1 + 50 - 10, starty1 + 10, p)
    canvasTemp.drawLine(startx1, endy1 - 50, startx1 - 10, endy1 - 50 + 10, p)
    canvasTemp.drawLine(startx1, endy1 - 50, startx1 + 10, endy1 - 50 + 10, p)
    /*以下是绘制间隔线，间隔线可以理解为缩短了的平移了的x，y轴（也就是说是x，y轴的缩短和平
    移——>两点确定一条直线中的两坐标变化），确定好x，y轴的间隔线间距就可以通过x或者y坐标增加
    或减少相应间隔倍数就可以实现间隔线的规律绘制*/
    //绘制x轴间隔线

    for (i in 1..allX) {
        canvasTemp.drawLine(
            startx1 + xinterval * i,
            starty1,
            startx1 + xinterval * i,
            starty1 - 10,
            p
        )
        if (allX > 30) {
            p2.setTextSize(20F)
        }
        canvasTemp.drawText((i-i%10).toString().substring(0,1), startx1 + xinterval * i, starty1 + 35, p2)
        canvasTemp.drawText((i%10).toString().substring(0,1), startx1 + xinterval * i, starty1 + 50, p2)
    }
    //绘制y轴间隔线

    for (i in 1..allY) {
        canvasTemp.drawLine(
            startx1,
            starty1 - yinterval * i,
            startx1 + 10,
            starty1 - yinterval * i,
            p
        )
        if (allX > 25) {
            p2.setTextSize(20F)
        } else {
            p2.setTextSize(35F)
            canvasTemp.drawText(
                String.valueOf(i * fully / allY),
                startx1 - 55,
                starty1 - yinterval * i + 10,
                p2
            )
        }
        canvasTemp.drawText((i).toString(), startx1 - 35, starty1 - yinterval * i, p2)
    }
    var x2=startx1 + xinterval * 1
    var y2=starty1 - (accl[0][1])/fully*(starty1-endy1)
    var x1=x2
    var y1=y2
    canvasTemp.drawPoint(x2, y2, p)
    //canvasTemp.drawLine(x1, y1, x2, y2, p)
    for(i in 1..dataNum-1) {
        //将输入的数据通过和y轴坐标的关系得到输入数据绘制的屏幕坐标
        x2 = startx1 + xinterval * (i+1) //x轴按间隔递增就好（单单计数）
        y2 = starty1 - (accl[i][1]) /fully*(starty1-endy1) /*y轴需要通过y轴实际最大长度和设定的最*/
        p.setStrokeWidth(10F)
        canvasTemp.drawPoint(x2, y2, p)
        if (i > 0) {
            p.setStrokeWidth(5F)
            canvasTemp.drawLine(x1, y1, x2, y2, p)
        }
        x1 = x2
        y1 = y2
    }
    //将绘制的内容在布局文件中的IMageView中显示
    //imageView.setImageBitmap()
    ImageView(context).setImageBitmap(newb)

}