package com.example.mymethodbook.service

import android.app.*
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mymethodbook.R
import com.example.mymethodbook.activity.MainActivity
import com.example.mymethodbook.model.Message
import com.example.mymethodbook.network.APIClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Objects
import kotlin.math.sqrt

class ShackDetectionService : Service() {
    // Service 관련 변수
    private val NOTIFICATION_CHANNEL_ID = "3000"
    private val NOTIFICATION_ID = 3333

    // 센서 사용 관련 변수
    private lateinit var sensorManager: SensorManager
    var acceleration = 0f
    var currentAcceleration = 0f
    var lastAcceleration = 0f

    /* Service 관련 메소드 */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification())
        }

        // 센서 관련 변수 초기화
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if(!isAccelerometerSensorPresent()) stopSelf()

        Objects.requireNonNull(sensorManager)
            .registerListener(
                sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
            )

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "충격 감지를 종료합니다.")
        sensorManager.unregisterListener(sensorListener)
    }

    /* Notification Channel & Notification 관련 메소드 */
    // Notification Channel 을 생성한다.
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val name = "백그라운드 충격 감지"
        val descriptionText = "앱이 사용자의 안전을 위해 백그라운드에서 충격 감지를 하는 것에 동의할 수 있습니다."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Notification 을 생성한다.
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotification() : Notification {
        val pendingIntent: PendingIntent =
            Intent(applicationContext, MainActivity::class.java).let { notificationIntent ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_MUTABLE
                    )
                } else {
                    PendingIntent.getActivity(this, 0, notificationIntent, 0)
                }
            }

        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("백그라운드 사용중")
            .setContentText("우리 앱은 사용자의 안전을 위해 백그라운드에서 충격 감지 작업을 하고 있습니다.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }

    /* 센서 관련 메소드 */
    private val sensorListener: SensorEventListener = object : SensorEventListener{
        // [필수 구현 요소] 센서의 새로운 값을 보고할 때 실행된다.
        override fun onSensorChanged(event: SensorEvent) {
            // 흔들림인지 아닌지 결정한다.
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // 현재의 가속도를 가져온다.
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta : Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if(acceleration > 45){
                Log.e(TAG, "Shaking detected.")
                /*GlobalScope.launch (Dispatchers.IO){
                    val token = "d_E2-QA9Qd--Pfje4YIYHQ:APA91bEFXlqbDcfqPbQvhzGIaNf9XZRfXiKg8xO-jUN5OxwvQXOW3-IbCCClRHlYFHV8dkccguAapT1UE91S6v7mtpCibNtvclWcKwrV4D081gGACrTBoVxgslS08aehfk-_I236pS62"
                    val result = try {
                        APIClient.apiInterface.notifyToParents(
                            message = Message(to = token, com.example.mymethodbook.model.Notification(
                                title = "FCM 테스트",
                                body = "FCM 테스트입니다.",
                                image = null
                            ))
                        )
                    }catch (errorCode: Exception){
                        Log.e(TAG, errorCode.toString())
                    }
                }*/
            }
        }

        // [필수 구현 요소] 센서의 정확도가 변경되었을 때 실행된다.
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
    }

    fun isAccelerometerSensorPresent() : Boolean{
        return if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            true
        } else {
            Log.e(TAG, "[Hardware Error] 기기에 가속도계 센서가 존재하지 않습니다.")
            false
        }
    }
}