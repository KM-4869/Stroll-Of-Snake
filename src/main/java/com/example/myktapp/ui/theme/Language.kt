package com.example.myktapp.ui.theme

const val Chinese = 0
const val English = 1
const val Japanese = 2

var TitleOne = "传感器数据"
var TitleTwo = "位姿信息"
var DataShowText1 = "加速度计数据"
var DataShowText2 = "陀螺仪数据"
var DataShowText3 = "磁强计"
var PositionShowText = "经纬高"
var AttitudeShowText = "当前姿态"
var XaxisText = "X轴"
var YaxisText = "Y轴"
var ZaxisText = "Z轴"
var LatText = " "
var LonText = " "
var HeightText = " "
var yawText = " "
var pitchText = " "
var rollText = " "
var NavigationItemText1 = "数据"
var NavigationItemText2 = "轨迹"
var NavigationItemText3 = "游戏"
var NavigationItemText4 = "设置"
var LanguageOptionText = " "
var ThemeOptionText = " "
var themeoptiontext1 = " "
var themeoptiontext2 = " "
var themeoptiontext3 = " "
var settingOption1 = " "
var settingOption2 = " "
var settingOption3GNSS = " "
var settingOption3PDR = " "
var abouttitle = " "
var aboutcontent = " "
var contactustitle = " "
var contactuscontent = " "
val TAG="Sensor data"




fun setLanguage(LanguageOption:Int){

    TitleOne= when(LanguageOption){
        Chinese-> "传感器数据"
        English->"Sensor Data"
        else -> "センサデータ"
        }

    TitleTwo= when(LanguageOption){
        Chinese-> "位姿信息"
        English->" Attitude and Position"
        else -> "位姿情報"
    }

    DataShowText1 = when(LanguageOption){
        Chinese-> "加速度计数据"
        English->"Accelerometer Data"
        else -> "加速度計データ"
    }

    DataShowText2 = when(LanguageOption){
        Chinese-> "陀螺仪数据"
        English->"Gyroscope Data"
        else -> "ジャイロデータ"
    }

    DataShowText3 = when(LanguageOption){
        Chinese-> "磁强计数据"
        English->"Magnetometer Data"
        else -> "磁力計データ"
    }

    PositionShowText = when(LanguageOption){
        Chinese-> "经纬高"
        English->"Position"
        else -> "経緯高"
    }

    AttitudeShowText = when(LanguageOption){
        Chinese-> "当前姿态"
        English->"Current Attitude"
        else -> "現在姿勢"
    }

    XaxisText = when(LanguageOption){
        Chinese-> "X轴"
        English->"X-axis"
        else -> "Ｘ軸"
    }

    YaxisText = when(LanguageOption){
        Chinese-> "Y轴"
        English->"Y-axis"
        else -> "Ｙ軸"
    }

    ZaxisText = when(LanguageOption){
        Chinese-> "Z轴"
        English->"Z-axis"
        else -> "Ｚ軸"
    }

    yawText = when(LanguageOption){
        Chinese-> "航向角"
        English->"Yaw"
        else -> "針道角"
    }

    pitchText = when(LanguageOption){
        Chinese-> "俯仰角"
        English->"Pitch"
        else -> "ピッチ角"
    }

    rollText = when(LanguageOption){
        Chinese-> "横滚角"
        English->"Roll"
        else -> "ロール角"
    }

    LatText = when(LanguageOption){
        Chinese-> "纬度"
        English->"B"
        else -> "緯度"
    }

    LonText = when(LanguageOption){
        Chinese-> "经度"
        English->"L"
        else -> "経度"
    }

    HeightText = when(LanguageOption){
        Chinese-> "高"
        English->"H"
        else -> "高さ"
    }

    NavigationItemText1 = when(LanguageOption){
        Chinese-> "数据"
        English->"Data"
        else -> "データ"
    }

    NavigationItemText2 = when(LanguageOption){
        Chinese-> "轨迹"
        English->"Track"
        else -> "軌跡"
    }

    NavigationItemText3 = when(LanguageOption){
        Chinese-> "游戏"
        English->"Game"
        else -> "ゲーム"
    }

    NavigationItemText4 = when(LanguageOption){
        Chinese-> "设置"
        English->"Setting"
        else -> "設定"
    }

    ThemeOptionText = when(LanguageOption){
        Chinese-> "主题模式"
        English->"Theme"
        else -> "テーマ"
    }

    LanguageOptionText = when(LanguageOption){
        Chinese-> "语言"
        English->"Language"
        else -> "言語"
    }

    themeoptiontext1 = when(LanguageOption){
        Chinese-> "浅色主题"
        English->"LightTheme"
        else -> "明るい色テーマ"
    }

    themeoptiontext2 = when(LanguageOption){
        Chinese-> "深色主题"
        English->"DarkTheme"
        else -> "暗いテーマ"
    }

    themeoptiontext3 = when(LanguageOption){
        Chinese-> "KM主题"
        English->"KM Theme"
        else ->"かとうめぐみ"
    }

    settingOption1 = when(LanguageOption){
        Chinese-> "获取位置信息"
        English->"Get Position Information"
        else ->"位置情報を取得"
    }

    settingOption2 = when(LanguageOption){
        Chinese-> "获取传感器信息"
        English->"Get Sensor Information"
        else ->"センサ情報を取得"
    }

    settingOption3GNSS = when(LanguageOption){
        Chinese->"切换轨迹模式(当前为GNSS)"
        English->"Switch track mode(Now GNSS)"
        else->"軌跡モード切り替え(今はGNSS)"
    }

    settingOption3PDR = when(LanguageOption){
        Chinese->"切换轨迹模式(当前为PDR)"
        English->"Switch track mode(Now PDR)"
        else->"軌跡モード切り替え(今はPDR)"
    }

    abouttitle = when(LanguageOption){
        Chinese->"关于SOS"
        English->"About SOS"
        else->"SOS について"
    }

    aboutcontent = when(LanguageOption){
        Chinese->"版本信息:1.1.1\n软件名:Stroll Of Snake\n软件说明:此应用实现了将手机中自带运动传感器数据输出可视化显示，" +
                "使用卫星定位(GNSS)或仅靠手机自带传感器进行航迹推断(PDR)来在地图上完成行走轨迹的实时绘制的功能。" +
                "使用PDR模式时，首先应开启GNSS获得坐标初值，之后将其关闭，再将手机端平，" +
                "手机的长轴方向指向行走方向以便获得正确的航向角。之后应用将自动检测行走脚步为您进行行走轨迹绘制！"
        English->"Version information :1.1.1\nSoftware name:Stroll Of Snake\nSoftware description: This application realizes the function " +
                "of visualizing the data output of the motion sensor in the mobile phone," +
                " and completing the real-time drawing of the walking trajectory on the map by using the " +
                "Global Navigation Satellite System (GNSS) or only relying on the mobile phone's built-in sensor for " +
                "Pedestrian Dead Reckoning(PDR). When using PDR mode, GNSS should be turned on first to " +
                "obtain the initial value of coordinates, and then turned off. Then, the mobile phone should " +
                "be flat, and the long axis direction of the mobile phone should point to the walking dire" +
                "ction in order to obtain the correct heading angle. Then the application will automatically " +
                "detect the walking footsteps for you to draw the walking track!"
        else->"バージョン情報:1.1.1\nソフトウェア名前:Stroll Of Snake\nソフトウェア説明:このアプリケーションは、携帯電話に搭載された働作センサーのデータ出力を可視化し、" +
                "衛星測位(GNSS)や携帯電話に搭載されたセンサーだけで航跡推定(PDR)を行い、地図上で走行軌跡をリアルタイムで" +
                "描画する機能を実現します。PDRモードでは、まずGNSSをオンにして座標の初期値を取り、その後これをオフにして" +
                "携帯を平らにし、携帯の長軸方向を走行方向に向けて正しい針路角を取ります。" +
                "その後、アプリが自働的に歩行を検知して軌跡を描いてくれます!"
    }

    contactustitle = when(LanguageOption){
        Chinese->"联系我们"
        English->"Contact us"
        else->"私たちを連絡する"
    }

    contactuscontent = when(LanguageOption){
        Chinese->"作者:武汉大学测绘学院导航工程位置服务与实践课程小组第十一组\n联系我们:2020302142065@whu.edu.cn"
        English->"Author: Group 11 of Navigation Engineering Location Service and Practice Course Group, School of Geodesy and Geomatics, Wuhan University\n" +
                "Contact us:2020302142065@whu.edu.cn"
        else->"著者:武漢大学測量製図学院ナビゲーションの工程位置のサービスと実践の課程グループ第十一グループ\n" +
                "私たちを連絡する:2020302142065@whu.edu.cn"
    }
//    when(LanguageOption){
//        Chinese-> aMap?.setMapLanguage(AMap.CHINESE)
//        else-> aMap?.setMapLanguage(AMap.ENGLISH)
//    }
}

