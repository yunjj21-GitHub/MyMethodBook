package com.example.mymethodbook.activity

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ALL
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.mymethodbook.service.ExampleService
import com.example.mymethodbook.R
import com.example.mymethodbook.model.Movie
import com.example.mymethodbook.model.TestResponse
import com.example.mymethodbook.network.APIClient
import com.example.mymethodbook.service.ShackDetectionService
import com.example.mymethodbook.service.UserLocationTrackingAndShareService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

// Bottom Navigation Item 클릭시 업로드하는 웹페이지 목록
const val webpage1 = "https://www.webpage1"
const val webpage2 = "https://www.webpage2"
const val webpage3 = "https://www.webpage3"
const val webpage4 = "https://www.webpage4"

// 생체 인식 인증 관련 변수
private lateinit var executor: Executor
private lateinit var biometricPrompt: BiometricPrompt
private lateinit var promptInfo : BiometricPrompt.PromptInfo

// 사용자 위치 정보 사용 관련 변수
private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
private lateinit var sensorManager: SensorManager

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initBottomNav()
        loadWebPage(webpage1)

        /* 생체 인식 인증 */
        // initBiometricPrompt()
        // initPromptInfo()
        // checkThatBiometricAuthenticationIsAvailable()

        /* 사용자 위치 정보 */
        // 사용자 위치 정보 액세스 권한 요청
        // getUsersLocationPermission()
        // upgradeUsersLocationPermission()

        // 사용자 위치 정보 사용
        // getUserLocation()

        /* 서버 통신 */
        // test()
        /* CoroutineScope(Dispatchers.IO).launch {
            while(true){
                test()
                delay(10000)
            }
        }*/

        /* 사용자 위치 트래킹 및 쉐어 기능 */
        startUserLocationTrackingAndShareService()

        /* 흔들림 감지 기능 */
        startShackDetectionService()

        val logoImage = findViewById<ImageView>(R.id.logoImage)
        logoImage.setOnClickListener {
            stopUserLocationTrackingAndShareService()
            stopShackDetectionService()
        }
    }

    /* Bottom Navigation 관련 메소드 */
    // Bottom Navigation 의 초기설정을 한다.
    fun initBottomNav() {
        val btmNav = findViewById<BottomNavigationView>(R.id.btmNav)
        btmNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    loadWebPage(webpage1)
                    true
                }
                R.id.money -> {
                    loadWebPage(webpage2)
                    true
                }
                R.id.person -> {
                    loadWebPage(webpage3)
                    true
                }
                R.id.chat -> {
                    loadWebPage(webpage4)
                    true
                }
                else -> false
            }
        }
    }

    /* Toolbar 관련 메소드 */
    // Toolbar 를 숨긴다.
    fun hideToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.visibility = View.INVISIBLE
    }

    // Toolbar 를 보이도록 한다.
    fun showToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.visibility = View.VISIBLE
    }

    // 로드된 웹페이지에 따라 Toolbar 의 Title 을 설정
    fun setTitleOfToolbar(URL: String) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val logoImage = findViewById<ImageView>(R.id.logoImage)
        when (URL) {
            webpage1 -> {
                toolbar.title = null
                logoImage.visibility = View.VISIBLE
            }
            webpage2 -> {
                toolbar.title = "Money"
                logoImage.visibility = View.INVISIBLE
            }
            webpage3 -> {
                toolbar.title = "Person"
                logoImage.visibility = View.INVISIBLE
            }
            webpage4 -> {
                toolbar.title = "Chat"
                logoImage.visibility = View.INVISIBLE
            }
        }
    }

    /* WebView 관련 메소드 */
    // WebView 에 URL 에 해당하는 웹페이지를 로드한다.
    fun loadWebPage(URL: String) {
        val webView = findViewById<WebView>(R.id.webView)
        webView.webViewClient = WebViewClient() // 웹페이지를 앱 외부에서 띄우지 않도록 설정
        webView.loadUrl(URL)

        // Toolbar 의 Title 변경
        setTitleOfToolbar(URL)
    }

    /* 생체 인식 인증 관련 메소드 */
    // biometricPrompt 초기화
    fun initBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    Log.e(TAG, "생체 인식 인증에 성공했습니다.")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    Log.e(TAG, "생체 인식 인증에 실패했습니다.")
                }
            })
    }

    // promptInfo 초기화
    fun initPromptInfo() {
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .setNegativeButtonText("cancel")
            .build()
    }

    // 생체 인식 인증을 사용할 수 있는지 확인
    @RequiresApi(Build.VERSION_CODES.R)
    fun checkThatBiometricAuthenticationIsAvailable() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.e(TAG, "App can authenticate using biometrics.")
                biometricAuth()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e(TAG, "No Biometric features available on this device.")

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e(TAG, "Biometric feature are currently unavailable.")

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                createCredentials()
        }
    }

    // 생체 정보 등록 화면으로 이동한다.
    @RequiresApi(Build.VERSION_CODES.R)
    fun createCredentials() {
        val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
            putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG)
        }
        startActivityForResult(enrollIntent, 123)
    }

    // 생체 인증 인식을 실행한다.
    fun biometricAuth() {
        biometricPrompt.authenticate(promptInfo)
    }

    /* 위치 정보 액세스 관련 메소드 */
    // 사용자에게 위치 정보 액세스 권한 요청을 한다.
    fun getUsersLocationPermission() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        Log.e(TAG, "Precise location access granted.")
                    }
                    permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        Log.e(TAG, "Only approximate location access granted.")
                    }
                    else -> {
                        Log.e(TAG, "No location access granted.")
                    }
                }
            }
        }

        locationPermissionRequest.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    // 사용자의 위치 정보 액세스 권한 업그레이드 요청을 한다.
    fun upgradeUsersLocationPermission(){
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted : Boolean ->
            if(isGranted) {
                Log.e(TAG, "Permission is granted")
            } else {
                Log.e(TAG, "Permission is denied")
            }
        }

        locationPermissionRequest.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    /* 사용자 위치 정보 사용 관련 메소드 */
    // 사용자의 위치 정보를 가져온다. (위도와 경도)
    @SuppressLint("MissingPermission")
    fun getUserLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                Log.e(TAG, "현재 사용자의 위치 : " + location.longitude.toString() + " " + location.latitude.toString())
            } else {
                Log.e(TAG, "사용자 위치 정보 없음")
            }
        }
    }

    /* 사용자 위치 트래킹 및 쉐어 기능 관련 메소드 */
    // UserLocationTrackingAndShareService 를 시작한다.
    fun startUserLocationTrackingAndShareService(){
        Intent(this, UserLocationTrackingAndShareService::class.java).also { intent ->
            startService(intent)
        }
    }

    // UserLocationTrackingAndShareService 를 종료한다.
    fun stopUserLocationTrackingAndShareService(){
        Intent(this, UserLocationTrackingAndShareService::class.java).also{ intent ->
            stopService(intent)
        }
    }

    /* 흔들림 감지 관련 메소드 */
    // UserLocationTrackingAndShareService 를 시작한다.
    fun startShackDetectionService(){
        Intent(this, ShackDetectionService::class.java).also { intent ->
            startService(intent)
        }
    }

    // UserLocationTrackingAndShareService 를 종료한다.
    fun stopShackDetectionService(){
        Intent(this, ShackDetectionService::class.java).also{ intent ->
            stopService(intent)
        }
    }

    /* 서버 통신 관련 메소드 */
    fun test(){
        CoroutineScope(Dispatchers.IO).launch {
            val result = try{
                APIClient.apiInterface.test()
            }catch (e: Exception){
                Log.e(TAG, "Network request failed")
            }
            Log.e(TAG, "result in MainActivity : $result")
        }
    }
}

