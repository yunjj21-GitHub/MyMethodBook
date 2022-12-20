package com.example.mymethodbook.service

import android.app.*
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.mymethodbook.R
import com.example.mymethodbook.activity.MainActivity
import com.example.mymethodbook.model.TestResponse
import com.example.mymethodbook.network.APIClient
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import java.lang.Runnable

class ExampleService : Service() {
    lateinit var repeatWorkJob : Job
    val NOTIFICATION_CHANNEL_ID = "1004"
    val NOTIFICATION_ID = 1000

    // 서비스가 처음 생성되었을 때 실행된다.
    override fun onCreate() {

    }

    // 다른 구성 요소가 해당 서비스를 시작하도록 요청하는 경우 실행된다.
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Toast.makeText(applicationContext, "ExampleService 를 시작합니다.", Toast.LENGTH_LONG).show()

        // 해당 서비스를 포그라운드 서비스로 실행한다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)
        }

        // 백그라운드 작업
        repeatWork()

        return START_STICKY
    }

    // [필수 구현 요소] 다른 구성 요소가 해당 서비스에 바인딩되고자 하는 경우 실행된다.
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // 서비스를 더 이상 사용하지 않고 소멸시킬 때 실행된다.
    override fun onDestroy() {
        repeatWorkJob.cancel()
        Toast.makeText(applicationContext, "Example Service 를 종료합니다.", Toast.LENGTH_LONG).show()
    }

    // Notification Channel 을 생성한다.
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val name = "example notification channel"
        val descriptionText = "This is just example notification channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotification() : Notification {
        val pendingIntent: PendingIntent =
            Intent(applicationContext, MainActivity::class.java).let { notificationIntent ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getActivity(this, 0, notificationIntent, FLAG_MUTABLE)
                } else {
                    PendingIntent.getActivity(this, 0, notificationIntent, 0)
                }
            }

        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("example notification")
            .setContentText("This is just example notification.")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setTicker("Ticker")
            .build()
    }

    /* 서버 통신 관련 */
    fun test(){
        GlobalScope.launch(Dispatchers.IO) {
            val result = try{
                APIClient.apiInterface.test()
            }catch(_:Exception){
                Log.e(TAG, "Network request failed")
            }
            Log.e(TAG, "result in ExampleService : $result")
        }
    }

    /* 백그라운드 작업 관련 */
    fun repeatWork(){
       repeatWorkJob = GlobalScope.launch(Dispatchers.IO){
           while(true){
               test()
               delay(10000)
           }
       }
    }
}