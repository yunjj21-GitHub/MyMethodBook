package com.example.mymethodbook.service

import android.Manifest
import android.app.*
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.mymethodbook.R
import com.example.mymethodbook.activity.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.*

class UserLocationTrackingAndShareService : Service() {
    // 비동기적 작업 관련 변수
    lateinit var userLocationTrackingJob : Job

    // Notification Channel 및 Notification 관련 변수
    private val N_C_I = "2000"
    private val N_I = 2222

    // 사용자 위치 정보 사용 관련 변수
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    /* Service 관련 메소드 */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Toast.makeText(applicationContext, "사용자 위치 트래킹 및 쉐어 기능을 활성화합니다.", Toast.LENGTH_LONG).show()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel()
            startForeground(N_I, createNotification())
        }

        // trackAndShareUserLocation()

        return START_STICKY
    }

    override fun onDestroy() {
        userLocationTrackingJob.cancel()
        Toast.makeText(applicationContext, "사용자 위치 트래킹 및 쉐어 기능을 중단합니다.", Toast.LENGTH_LONG).show()
        super.onDestroy()
    }

    /* Notification Channel 관련 메소드 */
    // Notification Channel 을 생성한다.
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        Log.e(TAG, "createNotificationChannel() 실행")
        val name = "User Location Information Usage Notification"
        val descriptionText = "User location information is used for location tracking and sharing functions."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(N_C_I, name, importance).apply {
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

        return Notification.Builder(this, N_C_I)
            .setContentTitle("Using your location information")
            .setContentText("Using your location information to use location tracking and sharing functions.")
            .setSmallIcon(R.drawable.ic_example_logo)
            .setContentIntent(pendingIntent)
            .build()
    }

    /* 사용자 위치 정보 사용 관련 메소드 */
    // 사용자 위치 정보를 가져온다. (위도와 경도)
    fun getUserLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // 대략적인 위치과 정확한 위치 정보 접근 모두가 거절되었다면
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, object: CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false
        }).addOnSuccessListener { location: Location? ->
            if (location != null) {
                Log.e(TAG, location.longitude.toString() + " " + location.latitude.toString())
            } else {
                Log.e(TAG, "사용자 위치 정보 없음")
            }
        }
    }

    /* 비동기적 작업 관련 메소드 */
    // 사용자 위치 정보를 일정 시간 간격으로 추적한다.
    fun trackAndShareUserLocation(){
        userLocationTrackingJob = GlobalScope.launch(Dispatchers.IO) {
            while(true){
                // 사용자 위치 정보 트래킹
                getUserLocation()

                // 사용자 위치 정보 쉐어
                shareUserLocation()

                // 가족들의 위치 정보를 가져옴
                getLocationOfFamily()

                // 일정 시간 간격
                delay(5000)
            }
        }
    }

    private fun shareUserLocation() {

    }

    private fun getLocationOfFamily() {

    }
}