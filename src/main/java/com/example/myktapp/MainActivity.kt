package com.example.myktapp

import android.Manifest
import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.LocationSource
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.PolylineOptions
import com.example.lbs.MySensors
import com.example.lbs.SensorsData
import com.example.myktapp.ui.theme.*
import pub.devrel.easypermissions.EasyPermissions
import com.example.lbs.hasLocationPermission
import pub.devrel.easypermissions.AfterPermissionGranted
import kotlin.math.sqrt


private const val REQUEST_PERMISSIONS = 9527
//地图控制器
var aMap: AMap? = null
//地图上绘制轨迹
//位置更改监听
var mapListener: LocationSource.OnLocationChangedListener? = null
var myaMaplocation:AMapLocation?=null
//绘制线
val GNSSlatLngs: MutableList<LatLng> = ArrayList()
val PDRlatLngs: MutableList<LatLng> = ArrayList()

var DrawMapLineAndGetMapLocationOnLocationChange:((Double ,Double, Double)->Unit)?=null
//数组保存
const val dataNum:Int=50//数组数据量
const val smoothWindowInterval=16//平滑窗口历元数

var StepLength=0.75F

var count = 0

class MainActivity : ComponentActivity(), AMapLocationListener, LocationSource {

    //定位
    //声明AMapLocationClient类对象
    var amapLocationClient: AMapLocationClient? = null
    //声明AMapLocationClientOption对象
    var amapLocationOption: AMapLocationClientOption? = null

    private val myLocationStyle = MyLocationStyle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mySensors = MySensors(this)
        initLocation()

        if(this.hasLocationPermission()) {
            showMsg("已获得权限，可以定位啦！！(>_<)")
            amapLocationClient!!.startLocation()
        }
        else{
            requestPermission()
            showMsg("没有权限，无法定位！！(T^T)")
        }

        setContent {
            val InitialTheme = if(isSystemInDarkTheme()) DarkMode else LightMode
            var ThemeOption by remember{ mutableStateOf(InitialTheme) }
            var sensorsData by remember { mutableStateOf(SensorsData()) }
            mySensors.accelerometerSensor.setOnSensorValuesChangedListener(
                listener = { values, time ->
                    sensorsData = sensorsData.copy(
                        accelerometerData = values
                    )})
            mySensors.gyroscopeSensor.setOnSensorValuesChangedListener(
                listener = { values, time ->
                    sensorsData = sensorsData.copy(
                        gyroscopeData = values
                    )})
            mySensors.magnetometerSensor.setOnSensorValuesChangedListener(
                listener = { values, time ->
                    sensorsData = sensorsData.copy(
                        magnetometerData = values
                    )})
            mySensors.locationUtils.setOnLocationValuesChangedListener(
                listener = { values, time ->
                    sensorsData = sensorsData.copy(
                        locationData = values
                    )})
            mySensors.calculateOrientation(sensorsData.accelerometerData.toFloatArray(),sensorsData.magnetometerData.toFloatArray()) { Floats ->
                sensorsData = sensorsData.copy(
                    orientationAngles = Floats
                )
            }
            val context = LocalContext.current
            val mapView = remember { MapView(context)}
            MapLifecycle(mapView = mapView)
            initMap(mapView = mapView)
            MyKtAppTheme (ThemeOption){
                val navController = rememberNavController()
                var LanguageOption by remember{ mutableStateOf(Chinese) }
                var page4switch1 by remember { mutableStateOf(false) }
                var page4switch2 by remember{ mutableStateOf(false) }
                var page4switch3 by remember{ mutableStateOf(false) }
                var page4selector1 by remember{ mutableStateOf(1) }
                var page4selector2 by remember{ mutableStateOf(1) }
                //当关闭page4switch3时使用GNSS画轨迹
                DrawMapLineAndGetMapLocationOnLocationChange={B,L,H->
                    sensorsData.maplocationData[0] = B
                    sensorsData.maplocationData[1] = L
                    sensorsData.maplocationData[2] = H
                    if(!page4switch3){
                        if(mapListener!=null){
                            //显示系统图标
                            myaMaplocation!!.latitude = B
                            myaMaplocation!!.longitude = L
                            mapListener!!.onLocationChanged(myaMaplocation)
                        }
                        GNSSlatLngs.add(LatLng(B, L))
                        aMap!!.addPolyline(PolylineOptions().addAll(GNSSlatLngs).width(30f).color(Color.argb(255, 255, 0, 0)))
                    }
                    else{
                        GNSSlatLngs.clear()
                    }
                }
                DrawPDRinMap(sensorsData = sensorsData, isPDR = page4switch3)
                setLanguage(LanguageOption)
                Scaffold(bottomBar = {TheBottomNavigationBar(navController = navController, LanguageOption = LanguageOption)}) { padding->
                    NavHost(navController = navController, modifier = Modifier.padding(padding), startDestination = "PageOne"){
                        composable(route = "PageOne"){
                            PageOne(ThemeOption = ThemeOption, sensorsData = sensorsData)
                        }
                        composable(route = "PageTwo"){
                            PageTwo(mapView = mapView)
                        }
                        composable(route = "PageThree"){
                            PageThree(sensorsData = sensorsData, context = context)
                        }
                        composable(route = "PageFour"){
                            PageFour(selector1 = page4selector1, selector2 = page4selector2, switcher1 = page4switch1, switcher2 = page4switch2, switcher3 = page4switch3,
                                Switch1 = {a->
                                          page4switch1 = !page4switch1
                                    if(!a)mySensors.locationUtils.startListening()
                                    else mySensors.locationUtils.stopListening()
                                },
                                Switch2 = {a->
                                          page4switch2 = !page4switch2
                                    if(!a){
                                        mySensors.accelerometerSensor.startListening()
                                        mySensors.gyroscopeSensor.startListening()
                                        mySensors.magnetometerSensor.startListening()
                                }else{
                                        mySensors.accelerometerSensor.stopListening()
                                        mySensors.gyroscopeSensor.stopListening()
                                        mySensors.magnetometerSensor.stopListening()
                                }
                                          },
                                Switch3 = {a->
                                    page4switch3 = !page4switch3
                                },
                                Click1 = {page4selector1 = 1
                                         LanguageOption = Chinese},
                                Click2 = {page4selector1 = 2
                                         LanguageOption = English},
                                Click3 = {page4selector1 = 3
                                         LanguageOption = Japanese},
                                Click4 = {page4selector2 = 1
                                         ThemeOption = LightMode},
                                Click5 = {page4selector2 = 2
                                         ThemeOption = DarkMode},
                                Click6 = {page4selector2 = 3
                                         ThemeOption = KMMode})
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查Android版本
     */
    private fun checkingAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android6.0及以上先获取权限再定位
            requestPermission()
        } else {
            //Android6.0以下直接定位
            amapLocationClient!!.startLocation()
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    private fun startlocate(){
        amapLocationClient!!.startLocation()
        showMsg("已获得权限，可以定位啦！！(>_<)")
    }

    private fun requestPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        EasyPermissions.requestPermissions(this, "需要权限", REQUEST_PERMISSIONS, *permissions)


    }

    /**
     * 请求权限结果
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    //@Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //设置权限请求结果
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun showMsg(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun initLocation() {
        //初始化定位
        try {
            amapLocationClient = AMapLocationClient(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (amapLocationClient != null) {
            //设置定位回调监听
            amapLocationClient!!.setLocationListener(this)
            //初始化AMapLocationClientOption对象
            amapLocationOption = AMapLocationClientOption()
            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
            amapLocationOption!!.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            //获取最近3s内精度最高的一次定位结果：
            //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
            amapLocationOption!!.isOnceLocationLatest = true
            //设置是否返回地址信息（默认返回地址信息）
            amapLocationOption!!.isNeedAddress = true
            //设置定位请求超时时间，单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
            amapLocationOption!!.httpTimeOut = 20000
            //关闭缓存机制，高精度定位会产生缓存。
            amapLocationOption!!.isLocationCacheEnable = false
            //给定位客户端对象设置定位参数
            amapLocationClient!!.setLocationOption(amapLocationOption)
        }
    }

    private fun initMap(mapView: MapView) {
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        //mapBinding!!.mapView.onCreate(savedInstanceState)
        //初始化地图控制器对象
        aMap = mapView.map
        //设置最小缩放等级为16 ，缩放级别范围为[3, 20]
        aMap!!.minZoomLevel = 10F
        // 自定义定位蓝点图标
        //myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.point));
        // 自定义精度范围的圆形边框颜色  都为0则透明
        //myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
        // 自定义精度范围的圆形边框宽度  0 无宽度
        //myLocationStyle.strokeWidth(0F);

        // 设置圆形的填充颜色  都为0则透明
        //myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
        //设置定位模式（连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。）
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)
        //设置定位蓝点的Style(为什么一设置定位样式就会卡？？？)
        myLocationStyle.interval(2000)

        //aMap!!.myLocationStyle = myLocationStyle;
        //aMap!!.setMyLocationStyle(myLocationStyle);

        //开启室内地图
        aMap!!.showIndoorMap(true)
        // 设置定位监听
        aMap!!.setLocationSource(this)
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap!!.isMyLocationEnabled = true
        //实例化UiSettings类对象
//        mUiSettings = aMap!!.uiSettings
//        //隐藏缩放按钮
//        mUiSettings!!.isZoomControlsEnabled = false
//        //显示比例尺 默认不显示
//        mUiSettings!!.isScaleControlsEnabled = true;
//        //设置地图点击事件
        //aMap!!.setOnMapClickListener(this)
        //设置地图长按事件
        //aMap!!.setOnMapLongClickListener(this)


    }

    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        myaMaplocation=aMapLocation
        if (myaMaplocation != null) {
            if (myaMaplocation!!.errorCode == 0) {


                DrawMapLineAndGetMapLocationOnLocationChange?.invoke(myaMaplocation!!.latitude, myaMaplocation!!.longitude, myaMaplocation!!.altitude)



            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e(
                    "AmapError", "location Error, ErrCode:"
                            + myaMaplocation!!.errorCode + ", errInfo:"
                            + myaMaplocation!!.errorInfo
                )
            }
        }
    }

    /**
     * 激活定位
     */
    override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener) {
        mapListener = onLocationChangedListener
        if (amapLocationClient == null) {
            amapLocationClient!!.startLocation() //启动定位
        }
    }

    /**
     * 停止定位
     */
    override fun deactivate() {
        mapListener = null
        if (amapLocationClient != null) {
            amapLocationClient!!.stopLocation()
            amapLocationClient!!.onDestroy()
        }
        amapLocationClient = null
    }


}


@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DefaultPreview() {
//    val InitialTheme = if(isSystemInDarkTheme()) DarkMode else LightMode
//    var ThemeOption by remember{ mutableStateOf(InitialTheme) }
//    MyKtAppTheme(ThemeOption){
//        val navController = rememberNavController()
//        val accl=FloatArray(3)
//        var LanguageOption by remember{ mutableStateOf(Chinese) }
//        var page4switch1 by remember { mutableStateOf(true) }
//        var page4switch2 by remember{ mutableStateOf(true) }
//        var page4selector1 by remember{ mutableStateOf(1) }
//        var page4selector2 by remember{ mutableStateOf(1) }
//        setLanguage(LanguageOption)
//        Scaffold(bottomBar = {TheBottomNavigationBar(navController = navController, LanguageOption = LanguageOption)}) { padding->
//            NavHost(navController = navController, modifier = Modifier.padding(padding), startDestination = "PageOne"){
//                composable(route = "PageOne"){
//                    val x=0f
//                    PageOne(ThemeOption = ThemeOption,accl=accl,x=x)
//                }
//                composable(route = "PageTwo"){
//                    PageTwo(ThemeOption = ThemeOption)
//                }
//                composable(route = "PageThree"){
//                    PageThree(ThemeOption = ThemeOption)
//                }
//                composable(route = "PageFour"){
//                    PageFour(selector1 = page4selector1, selector2 = page4selector2, switcher1 = page4switch1, switcher2 = page4switch2,
//                        Switch1 = {a->
//                                  page4switch1 = !page4switch1
//                        },
//                        Switch2 = {a->
//                                  page4switch2 = !page4switch2
//                        },
//                        Click1 = {page4selector1 = 1
//                            LanguageOption = Chinese},
//                        Click2 = {page4selector1 = 2
//                            LanguageOption = English},
//                        Click3 = {page4selector1 = 3
//                            LanguageOption = Japanese},
//                        Click4 = {page4selector2 = 1
//                                 ThemeOption = LightMode},
//                        Click5 = {page4selector2 = 2
//                                 ThemeOption = DarkMode},
//                        Click6 = {page4selector2 = 3
//                        ThemeOption = KMMode})
//                }
//            }
//        }
//    }
}

@Composable
fun PageOne(modifier: Modifier=Modifier, ThemeOption:Int, sensorsData: SensorsData) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background ){
        if(ThemeOption == KMMode) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.km17), contentDescription = null,
                alpha = 0.7f
            )
        }
        Column (modifier = Modifier.verticalScroll(rememberScrollState())){
            Row (verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
                Text(modifier = Modifier.padding(top = 5.dp,start = 15.dp),text = TitleOne,fontSize = 25.sp, fontFamily = FontFamily.Serif)
                Divider(thickness = 1.dp, color = MaterialTheme.colors.onBackground, startIndent = 8.dp)
            }
            DataShowBlock(figureName = R.drawable._speed8, description = DataShowText1, dataunit = "m/s^2", sensorData = sensorsData.accelerometerData)
            DataShowBlock(figureName = R.drawable.gyro, description = DataShowText2, dataunit ="rad/s", sensorData = sensorsData.gyroscopeData)
            DataShowBlock(figureName = R.drawable.compass2, description = DataShowText3, dataunit ="μT", sensorData = sensorsData.magnetometerData)
            Row (verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(modifier = Modifier.padding(top = 5.dp, start = 15.dp), text = TitleTwo, fontSize = 25.sp, fontFamily = FontFamily.Serif)
                Divider(thickness = 1.dp, color = MaterialTheme.colors.onBackground, startIndent = 8.dp)
            }
            PositionShowBlock(sensorData = sensorsData.locationData)
            AttitudeShowBlock(sensorData = sensorsData.orientationAngles)
        }

    }
}

@Composable
fun PageTwo(modifier: Modifier = Modifier, mapView: MapView){

    AndroidView(factory = {mapView})

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(3.dp), horizontalAlignment = Alignment.End) {
        Spacer(modifier = Modifier.height(565.dp))
        FloatingActionButton(modifier = Modifier
            .alpha(0.7f)
            .size(52.dp), onClick = {
            aMap!!.clear()

            //if(LanguageOption == Chinese) aMap!!.setMapLanguage(AMap.CHINESE) else aMap!!.setMapLanguage(AMap.ENGLISH)
            GNSSlatLngs.clear()
            PDRlatLngs.clear()

        },
        backgroundColor = MaterialTheme.colors.primary
            ){
            Image(painter = painterResource(id = R.drawable.refresh), contentDescription = null)
        }
    }



}

private fun MapView.lifecycleObserver(): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> this.onCreate(Bundle())
            Lifecycle.Event.ON_RESUME -> this.onResume() // 重新制作加载地图
            Lifecycle.Event.ON_PAUSE -> this.onPause()  // 暂停地图的制作
            Lifecycle.Event.ON_DESTROY -> this.onDestroy() // 销毁地图
            else -> {}
        }
    }
private fun MapView.componentCallbacks(): ComponentCallbacks =
    object : ComponentCallbacks {
        // 设备装备产生改变，组件还在运转时
        override fun onConfigurationChanged(config: Configuration) {}
        // 系统运转的内存不足时，能够通过完成该办法去开释内存或不需求的资源
        override fun onLowMemory() {
            // 调用地图的onLowMemory
            this@componentCallbacks.onLowMemory()
        }
    }


@Composable
private fun MapLifecycle(mapView: MapView) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(context, lifecycle, mapView) {
        val mapLifecycleObserver = mapView.lifecycleObserver()
        val callbacks = mapView.componentCallbacks()
        // 增加生命周期观察者
        lifecycle.addObserver(mapLifecycleObserver)
        // 注册ComponentCallback
        context.registerComponentCallbacks(callbacks)
        onDispose {
            // 删除生命周期观察者
            lifecycle.removeObserver(mapLifecycleObserver)
            // 取消注册ComponentCallback
            context.unregisterComponentCallbacks(callbacks)
        }
    }
}

@Composable
fun PageThree(modifier: Modifier = Modifier, sensorsData: SensorsData, context: Context){
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colors.background, modifier = Modifier.fillMaxSize()) {
            Column(modifier = modifier
                .fillMaxSize()
                .padding(5.dp)) {
                CircularBubble(x = sensorsData.accelerometerData[0], y = sensorsData.accelerometerData[1], z = sensorsData.accelerometerData[2])
                Accldatachart(sensorsData = sensorsData)

            }
        }
    }
}

@Composable
fun CircularBubble(x:Float, y:Float, z:Float){

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = /*androidx.compose.ui.graphics.Color.White*/MidnightBlue,
                        radius = size.minDimension / 2,
                        center = center
                    )
                    drawCircle(
                        color = /*androidx.compose.ui.graphics.Color.LightGray*/GhostWhite,
                        radius = size.minDimension / 8,
                        center = Offset(x = center.x + x * size.minDimension / 20, y = center.y - y * size.minDimension / 20)
                    )
                }
            }
        //}
    //}
}

@Composable
fun Accldatachart(sensorsData: SensorsData){

    var acclsmoothinchart /*by remember { mutableStateOf(*/=  FloatArray(dataNum)/* ) }*/
    val acclinchart by remember { mutableStateOf(FloatArray(dataNum)) }
    var accltemp = FloatArray(dataNum)
    //var acclinchart = FloatArray(dataNum)

    acclinchart[count] = sqrt( sensorsData.accelerometerData[0]*sensorsData.accelerometerData[0]+sensorsData.accelerometerData[1]*sensorsData.accelerometerData[1]+sensorsData.accelerometerData[2]*sensorsData.accelerometerData[2])

    val w = 1600
    val h = 1000
    val newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

    val canvasTemp = android.graphics.Canvas(newb)
    //Canvas canvasTemp2=new Canvas(newb);
    canvasTemp.drawColor(Color.TRANSPARENT)
    val p = Paint()
    //防锯齿
    p.setAntiAlias(true)
    p.setStyle(Paint.Style.STROKE) //STROKE,FILL
    p.setStrokeWidth(5F)
    p.setColor(Color.LTGRAY)
    p.setTextAlign(Paint.Align.CENTER)
    p.setTextSize(40F)
    p.setColor(MaterialTheme.colors.onBackground.toArgb())
    p.setStyle(Paint.Style.FILL) //STROKE,FILL
    p.setStrokeWidth(2F)
    val p2 = p
    /*绘制表格，表格的绘制是有规律的，我先确认原点和x，y轴的长度，然后通过原点的的坐标和x，y
    轴的长度就可以得到x，y轴的终点坐标，最终通过两点确定一条直线就可以画出x，y轴了。*/
    val startx1 = 100F
    val starty1 = 900F
    val endx1 = 1500F
    val endy1 = 100F
//    canvasTemp.drawText(
//        "0",
//        startx1 - 10,
//        starty1 + 30,
//        p
//    ) //原点
    val allX = dataNum
    val allY = 20
    val fully= 20

    val yinterval = (starty1-endy1)/allY
    val xinterval = (endx1-startx1)/allX

//    canvasTemp.drawLine(startx1, starty1, endx1 + 50, starty1, p) // 绘制x轴
//    canvasTemp.drawText("x", endx1 + 50 + 10, starty1 + 35, p) //标记x轴
//    canvasTemp.drawLine(startx1, starty1, startx1, endy1 - 50, p) // 绘制y轴
//    canvasTemp.drawText("y", startx1 - 15, endy1 - 50, p) //标记y轴
//    //绘制x y轴箭头，与上同理，两点确定一条直线进行绘制
//    canvasTemp.drawLine(endx1 + 50, starty1, endx1 + 50 - 10, starty1 - 10, p)
//    canvasTemp.drawLine(endx1 + 50, starty1, endx1 + 50 - 10, starty1 + 10, p)
//    canvasTemp.drawLine(startx1, endy1 - 50, startx1 - 10, endy1 - 50 + 10, p)
//    canvasTemp.drawLine(startx1, endy1 - 50, startx1 + 10, endy1 - 50 + 10, p)
    /*以下是绘制间隔线，间隔线可以理解为缩短了的平移了的x，y轴（也就是说是x，y轴的缩短和平
    移——>两点确定一条直线中的两坐标变化），确定好x，y轴的间隔线间距就可以通过x或者y坐标增加
    或减少相应间隔倍数就可以实现间隔线的规律绘制*/
    //绘制x轴间隔线

    for (k in 1..allX) {
        canvasTemp.drawLine(
            startx1 + xinterval * k,
            starty1,
            startx1 + xinterval * k,
            starty1 - 10,
            p
        )
        if (allX > 30) {
            p2.setTextSize(20F)
        }
        //canvasTemp.drawText((k-k%10).toString().substring(0,1), startx1 + xinterval * k, starty1 + 35, p2)
        //canvasTemp.drawText((k%10).toString().substring(0,1), startx1 + xinterval * k, starty1 + 50, p2)
    }
//    //绘制y轴间隔线
//
//    for (k in 1..allY) {
//        canvasTemp.drawLine(
//            startx1,
//            starty1 - yinterval * k,
//            startx1 + 10,
//            starty1 - yinterval * k,
//            p
//        )
//        if (allX > 25) {
//            p2.setTextSize(20F)
//        } else {
//            p2.setTextSize(35F)
//            canvasTemp.drawText(
//                java.lang.String.valueOf(k * fully / allY),
//                startx1 - 55,
//                starty1 - yinterval * k + 10,
//                p2
//            )
//        }
//        canvasTemp.drawText((k).toString(), startx1 - 35, starty1 - yinterval * k, p2)
//    }

    if (count< dataNum - 1) {
        count++

    }else {
        accltemp = acclinchart.copyOf()
        acclsmoothinchart = hwaSmooth(accltemp, smoothWindowInterval)

        var x2 = startx1 + xinterval * 1
        var y2 = starty1 - (acclinchart[0]) / fully * (starty1 - endy1)
        var x1 = x2
        var y1 = y2
        canvasTemp.drawPoint(x2, y2, p)
        //canvasTemp.drawLine(x1, y1, x2, y2, p)
        for (j in 1..dataNum - 1) {
            //将输入的数据通过和y轴坐标的关系得到输入数据绘制的屏幕坐标
            x2 = startx1 + xinterval * (j + 1) //x轴按间隔递增就好（单单计数）
            y2 = starty1 - (acclinchart[j]) / fully * (starty1 - endy1) /*y轴需要通过y轴实际最大长度和设定的最*/
            p.setStrokeWidth(10F)
            canvasTemp.drawPoint(x2, y2, p)
            p.setStrokeWidth(5F)
            canvasTemp.drawLine(x1, y1, x2, y2, p)

            x1 = x2
            y1 = y2
        }

        for(j in 0..48){
            acclinchart[j] = acclinchart[j+1]
        }

    }

    Image(painter = BitmapPainter(newb.asImageBitmap()), contentDescription = null, modifier = Modifier.size(500.dp))

}

@Composable
fun PageFour(modifier: Modifier = Modifier, switcher1:Boolean, switcher2:Boolean, switcher3:Boolean, selector1: Int, selector2:Int,Switch1:(Boolean)->Unit, Switch2:(Boolean)->Unit, Switch3:(Boolean)->Unit, Click1:()->Unit, Click2:()->Unit, Click3:()->Unit, Click4:()->Unit, Click5:()->Unit, Click6:()->Unit){
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        if(selector2 == 3) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 165.dp),
                painter = painterResource(id = R.drawable.km16), contentDescription = null,
                alpha = 0.7f
            )
        }
        Column (modifier = Modifier.verticalScroll(rememberScrollState())){
            TextAndSwitch(Text = settingOption1,switcher = switcher1, OnSwitched = {Switch1(switcher1)})
            Divider(thickness = 1.dp)
            TextAndSwitch(Text = settingOption2, switcher = switcher2, OnSwitched = {Switch2(switcher2)})
            Divider(thickness = 1.dp)
            TextAndSwitch(Text = if (switcher3) settingOption3PDR else settingOption3GNSS, switcher = switcher3, OnSwitched = {Switch3(switcher3)})
            Divider(thickness = 1.dp)
            val list1 = listOf(LanguageOptionText,"中文","English","日本語")
            val list2 = listOf(ThemeOptionText, themeoptiontext1, themeoptiontext2, themeoptiontext3)
            AButtonWithThreeButton(selector = selector1, list1, {Click1()}, {Click2()}, {Click3()})
            Divider(thickness = 1.dp)
            AButtonWithThreeButton(selector = selector2, list2, {Click4()}, {Click5()}, {Click6()})
            Divider(thickness = 1.dp)
            TextExpansion(title = abouttitle, content = aboutcontent)
            Divider(thickness = 1.dp)
            TextExpansion(title = contactustitle, content = contactuscontent)
        }
    }
}

@Composable
fun DataShowBlock(modifier: Modifier=Modifier, figureName:Int, description:String, dataunit:String, sensorData:List<Float>) {
    var unfold by remember { mutableStateOf(false) }
    Box(modifier = Modifier
        .padding(all = 8.dp)
        .animateContentSize { initialValue, targetValue -> }
    ) {
        Surface(modifier = Modifier.clickable { unfold = !unfold } ,shape = RoundedCornerShape(15), color = MaterialTheme.colors.secondary.copy(alpha = 0.3f), border = BorderStroke(3.dp, color = MaterialTheme.colors.secondaryVariant)) {
            Column() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .size(75.dp)
                            .clip(CircleShape)
                            .border(5.dp, MaterialTheme.colors.primary, CircleShape),
                        painter = painterResource(id = figureName),
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(45.dp))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.h5,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colors.onSurface
                    )
                }

                if(unfold)
                {
                    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$XaxisText:${sensorData[0]} $dataunit", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = "$YaxisText:${sensorData[1]} $dataunit", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = "$ZaxisText:${sensorData[2]} $dataunit", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface)
                    }
                }
            }

        }
    }
}


@Composable
fun PositionShowBlock(modifier: Modifier = Modifier,sensorData: List<Double>){
    var unfold by remember { mutableStateOf(false) }
    Box(modifier = Modifier
        .padding(all = 8.dp)
        .animateContentSize { initialValue, targetValue -> }
    ) {
        Surface(modifier = Modifier.clickable { unfold = !unfold } ,shape = RoundedCornerShape(15), color = MaterialTheme.colors.secondary.copy(0.3f), border = BorderStroke(3.dp, color = MaterialTheme.colors.secondaryVariant)) {
            Column() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .size(75.dp)
                            .clip(CircleShape)
                            .border(5.dp, MaterialTheme.colors.primary, CircleShape),
                        painter = painterResource(id = R.drawable.location),
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(45.dp))

                    Text(
                        text = PositionShowText,
                        style = MaterialTheme.typography.h5,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colors.onSurface
                    )
                }

                if(unfold)
                {
                    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$LatText:${sensorData[0]}°", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = "$LonText:${sensorData[1]}°", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = "$HeightText:${sensorData[2]}m", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface)
                    }
                }
            }

        }
    }
}

@Composable
fun AttitudeShowBlock(modifier: Modifier = Modifier, sensorData: FloatArray){
    var unfold by remember { mutableStateOf(false) }
    Box(modifier = Modifier
        .padding(all = 8.dp)
        .animateContentSize { initialValue, targetValue -> }
    ) {
        Surface(modifier = Modifier.clickable { unfold = !unfold } ,shape = RoundedCornerShape(15), color = MaterialTheme.colors.secondary.copy(0.3f), border = BorderStroke(3.dp, color = MaterialTheme.colors.secondaryVariant)) {
            Column() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .size(75.dp)
                            .clip(CircleShape)
                            .border(5.dp, MaterialTheme.colors.primary, CircleShape),
                        painter = painterResource(id = R.drawable.car),
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(45.dp))

                    Text(
                        text = AttitudeShowText,
                        style = MaterialTheme.typography.h5,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colors.onSurface
                    )
                }

                if(unfold)
                {
                    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$yawText:${sensorData[0]}°", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = "$pitchText:${sensorData[1]}°", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = "$rollText:${sensorData[2]}°", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface)
                    }
                }
            }

        }
    }
}

@Composable
fun TextAndSwitch(Text: String, switcher:Boolean, OnSwitched: (Boolean)->Unit){
    Box() {
        Surface(color = MaterialTheme.colors.surface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = Text, style = MaterialTheme.typography.h6)
                Switch(checked = switcher, onCheckedChange = { OnSwitched(switcher)})
            }
        }
    }
}

@Composable
fun AButtonWithThreeButton( selector: Int, stringList: List<String>, onClicked1:()->Unit, onClicked2:()->Unit, onClicked3:()->Unit){
     var isClicked by rememberSaveable{ mutableStateOf(false) }
    Column() {
        Box() {
            Surface(color = MaterialTheme.colors.surface, modifier = Modifier.clickable { isClicked = !isClicked }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringList[0], style = MaterialTheme.typography.h6)
                }
            }
        }


        if(isClicked) {
            Box() {
                Surface(color = if(selector == 1) MaterialTheme.colors.primary.copy(alpha = 0.7f) else MaterialTheme.colors.surface.copy(alpha = 0.9f), modifier = Modifier.clickable {/*selector = 1*/
                onClicked1()}) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = stringList[1], style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface)
                    }
                }
            }
            Box() {
                Surface(color = if(selector == 2) MaterialTheme.colors.primary.copy(alpha = 0.7f) else MaterialTheme.colors.surface.copy(alpha = 0.9f), modifier = Modifier.clickable {/*selector = 2*/
                onClicked2()}) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = stringList[2], style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface)
                    }
                }
            }
            Box() {
                Surface(color = if(selector == 3) MaterialTheme.colors.primary.copy(alpha = 0.7f) else MaterialTheme.colors.surface.copy(alpha = 0.9f), modifier = Modifier.clickable {/*selector = 3*/
                onClicked3()}) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = stringList[3], style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun TextExpansion(modifier: Modifier = Modifier, title:String, content:String) {
    var isClicked by rememberSaveable { mutableStateOf(false) }
    Column() {
        Box() {
            Surface(
                color = MaterialTheme.colors.surface,
                modifier = Modifier.clickable { isClicked = !isClicked }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = title, style = MaterialTheme.typography.h6)
                }
            }
        }
        if (isClicked) {
            Box() {
                Surface(
                    color = MaterialTheme.colors.surface.copy(0.9f),
                    modifier = Modifier.clickable { isClicked = !isClicked }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 10.dp)
                            /*.verticalScroll(rememberScrollState())*/,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = content, style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface)
                    }
                }
            }
        }

    }
}



    @Composable
    fun TheBottomNavigationBar(
        modifier: Modifier = Modifier,
        navController: NavHostController,
        LanguageOption: Int
    ) {
        BottomNavigation(
            modifier = Modifier.height(70.dp),
            backgroundColor = MaterialTheme.colors.primary
        ) {
            var selector by remember { mutableStateOf(1) }
            BottomNavigationItem(
                selected = selector == 1,
                onClick = {
                    selector = 1
                    navController.navigate("PageOne")
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.data_view),
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = when (LanguageOption) {//为什么这个导航栏的语言变换要硬写，不能直接用函数改啊啊啊？？？
                            Chinese -> "数据"
                            English -> "Data"
                            else -> "データ"
                        }
                    )
                },
                enabled = if (selector == 1) false else true
            )
            BottomNavigationItem(
                selected = selector == 2,
                onClick = {
                    selector = 2
                    navController.navigate("PageTwo")
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.navigation),
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = when (LanguageOption) {//为什么这个导航栏的语言变换要硬写，不能直接用函数改啊啊啊？？？
                            Chinese -> "轨迹"
                            English -> "Track"
                            else -> "軌跡"
                        }
                    )
                },
                enabled = if (selector == 2) false else true
            )

            BottomNavigationItem(
                selected = selector == 3,
                onClick = {
                    selector = 3
                    navController.navigate("PageThree")
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.view2),
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = when (LanguageOption) {//为什么这个导航栏的语言变换要硬写，不能直接用函数改啊啊啊？？？
                            Chinese -> "视图"
                            English -> "View"
                            else -> "ビュー"
                        }
                    )
                },
                enabled = if (selector == 3) false else true
            )
            BottomNavigationItem(
                selected = selector == 4,
                onClick = {
                    selector = 4
                    navController.navigate("PageFour")
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.setting),
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = when (LanguageOption) {//为什么这个导航栏的语言变换要硬写，不能直接用函数改啊啊啊？？？
                            Chinese -> "设置"
                            English -> "Setting"
                            else -> "設定"
                        }
                    )
                },
                enabled = if (selector == 4) false else true
            )
        }
    }


















