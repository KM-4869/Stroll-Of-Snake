# Stroll-Of-Snake
A Android app which can visualizing the data output of the motion sensor in the mobile phone and real-time drawing of walking trajectory on the map by using GNSS or PDR

此应用实现了将手机中自带运动传感器数据输出可视化显示,使用卫星定位(GNSS)或仅靠手机自带传感器进行航迹推断(PDR)来在地图上完成行走轨迹的实时绘制的功能.使用PDR模式时，首先应开启GNSS获得坐标初值，之后将其关闭，再将手机端平，手机的长轴方向指向行走方向以便获得正确的航向角。之后应用将自动检测行走脚步为您进行行走轨迹绘制！


This application realizes the function of visualizing the data output of the motion sensor in the mobile phone,and completing the real-time drawing of the walking trajectory on the map by using the Global Navigation Satellite System (GNSS) or only relying on the mobile phone's built-in sensor for Pedestrian Dead Reckoning(PDR). When using PDR mode, GNSS should be turned on first to obtain the initial value of coordinates, and then turned off. Then, the mobile phone should be flat, and the long axis direction of the mobile phone should point to the walking direction in order to obtain the correct heading angle. Then the application will automatically detect the walking footsteps for you to draw the walking track!


このアプリケーションは、携帯電話に搭載された働作センサーのデータ出力を可視化し、衛星測位(GNSS)や携帯電話に搭載されたセンサーだけで航跡推定(PDR)を行い、地図上で走行軌跡をリアルタイムで描画する機能を実現します。PDRモードでは、まずGNSSをオンにして座標の初期値を取り、その後これをオフにして携帯を平らにし、携帯の長軸方向を走行方向に向けて正しい針路角を取ります。その後、アプリが自働的に歩行を検知して軌跡を描いてくれます!
![Screenrecorder-2023-04-19-13-04-20-638](https://user-images.githubusercontent.com/124596304/233115547-c1f95e7d-d0bc-40fb-ba62-b24db750fdd2.gif)
