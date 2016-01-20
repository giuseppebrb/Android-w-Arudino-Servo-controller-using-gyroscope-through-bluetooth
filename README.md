# Android (w/ Arudino) - Servo controller using gyroscope through bluetooth

With this app, which connects itself to Arduino by bluetooth, you can control a servo motor using gyroscope.
While user rotate phone on y axis* servo changes its position.
App sends a new value to arduino when its current value It is a divisor of five (so you could adjust calibration changing this value).

*Note that app runs in landscape mode, so in this case y axis is reversed with x axis 

## The circuit

### Components
* 1 Arduino (UNO rev 3 in pic)
* 1 Bluetooth module* (HC-06)
* 1 Capacitor
* 1 Servo motor
* Wires


*Note that you have to know the MAC address and insert it into MainActivity.java code. If you don't know how to find it, after created the circuit and turned it on you can find it using an app such as https://play.google.com/store/apps/details?id=com.ccpcreations.android.bluetoothmacfinder&hl=it
![The circuit](http://s9.postimg.org/e9tkh3y27/Circuit.jpg)


## The Application

The application is a very light and has a simple interface. Source is in the "Android Project" folder.

![The Application](http://s12.postimg.org/tmmefa3yl/Screenshot_2016_01_20_20_21_41.png)
