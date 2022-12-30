package com.example.mymethodbook.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.mymethodbook.R
import com.example.mymethodbook.network.APIClient
import com.example.mymethodbook.service.ShackDetectionService
import com.example.mymethodbook.service.UserLocationTrackingAndShareService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.Executor

// Bottom Navigation Item 클릭시 업로드하는 웹페이지 목록
const val webpage1 = "https://www.webpage1"
const val webpage2 = "https//www.webpage2"
const val webpage3 = "https://www.webpage3"
const val webpage4 = "https://www.webpage4"

// Firebase Dynamic Link 관련 변수
const val domainPrefix = "https://mymethodbook.page.link"
const val deeplinkForOpeningTheWebpage2 = "https://mymethodbook.page.link/webpage2"

// 생체 인식 인증 관련 변수
private lateinit var executor: Executor
private lateinit var biometricPrompt: BiometricPrompt
private lateinit var promptInfo : BiometricPrompt.PromptInfo

// 사용자 위치 정보 사용 관련 변수
private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
private lateinit var sensorManager: SensorManager

class MainActivity : AppCompatActivity() {
    lateinit var btmNav : BottomNavigationView
    lateinit var resultText : TextView
    lateinit var testBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btmNav = findViewById<BottomNavigationView>(R.id.btmNav)
        resultText = findViewById<TextView>(R.id.resultText)
        testBtn = findViewById<Button>(R.id.testButton)

        initBottomNav()
        setWebPage(webpage1)

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
        // startUserLocationTrackingAndShareService()

        /* 흔들림 감지 기능 */
        // startShackDetectionService()

        val logoImage = findViewById<ImageView>(R.id.logoImage)
        logoImage.setOnClickListener {
            // stopUserLocationTrackingAndShareService()
            // stopShackDetectionService()
        }

        /* notification 관련 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkAndGetNotificationPermission()
        }
        // createNotificationChannel()
        // postNotification()

        /* FCM 관련 */
        getUsersFCMToken()
        subscribeAdvertisement()
        subscribeNotice()

        /* Firebase Dynamic Link 관련 */
        val postId = "1234"
        listenDynamicLink()
        testBtn.setOnClickListener {
            generateSharingLink(
                "${domainPrefix}/post/${postId}".toUri(),
                previewImageLink = "https://blog.branch.io/ko/wp-content/uploads/2017/01/android-image.png".toUri()
            ) { generatedLink ->
               Log.e(TAG, generatedLink.toString())
            }
        }
    }

    /* Bottom Navigation 관련 메소드 */
    // Bottom Navigation 의 초기설정을 한다.
    fun initBottomNav() {
        btmNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    setWebPage(webpage1)
                    true
                }
                R.id.money -> {
                    setWebPage(webpage2)
                    true
                }
                R.id.person -> {
                    setWebPage(webpage3)
                    true
                }
                R.id.chat -> {
                    setWebPage(webpage4)
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
    fun setWebPage(URL: String) {
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

    /* Notification 관련 메소드 */
    val NOTIFICATION_CHANNEL_ID = "5000"
    val NOTIFICATION_ID = 5555
    // notification 권한이 있는지 확인한다.
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkAndGetNotificationPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            return
        } else {
            getNotificationPermission()
        }
    }

    // 사용자에게 notification 권한을 요청한다.
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getNotificationPermission(){
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) { // notification 공지 허락
                Log.e(TAG, "notification 권한 허락")
            } else { // notification 공지 거절
                Log.e(TAG, "notification 권한 거절")
            }
        }

        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    // notification channel 을 생성한다.
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "contnet"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // notification 을 띄운다.
    fun postNotification(){
        val button = findViewById<Button>(R.id.testButton)
        button.setOnClickListener {
            val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_money)
                .setContentTitle("테스트 공지")
                .setContentText("반갑습니다. 테스트 공지입니다.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)){
                notify(NOTIFICATION_ID, builder.build())
            }
        }
    }

    /* FCM 관련 메소드 */
    // token 을 가져온다.
    fun getUsersFCMToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            Log.e(TAG, "FCM token : $token")
        })
    }

    // 광고를 구독한다.
    fun subscribeAdvertisement(){
        FirebaseMessaging.getInstance().subscribeToTopic("Advertisement")
            .addOnCompleteListener { task ->
                var result = "Subscribed to the advertisement"
                if(!task.isSuccessful){
                    result = "Advertisement subscribe failed"
                }
                Log.e(TAG, result)
            }
    }

    // 공지를 구독한다.
    fun subscribeNotice(){
        FirebaseMessaging.getInstance().subscribeToTopic("Notice")
            .addOnCompleteListener { task ->
                var result = "Subscribed to the notice"
                if(!task.isSuccessful){
                    result = "Notice subscribe failed"
                }
                Log.e(TAG, result)
            }
    }

    /* Firebase Dynamic Link 관련 메소드 */
    // Deeplink 수신한다.
    fun listenDynamicLink() {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                var deepLink: Uri? = null

                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }

                deepLink?.let { uri ->
                    val path = uri.toString().substring(deepLink.toString().lastIndexOf("/") + 1)

                    when {
                        uri.toString().contains("post") -> {
                            val postId = path
                            Log.e(TAG, "Post id : ${postId} 포스트로 이동합니다.")
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "handleIncomingDeepLinks: ${it.message}")
            }
    }

    // Deeplink 생성한다.
    fun generateSharingLink(
        deepLink: Uri,
        previewImageLink: Uri,
        getShareableLink: (String) -> Unit = {}
    ){
        FirebaseDynamicLinks.getInstance().createDynamicLink().run{
            link = deepLink
            domainUriPrefix = domainPrefix
            androidParameters {
                // minimumVersion = Build.VERSION_CODES.N
                build()
            }

            socialMetaTagParameters {
                title = "MyMethodBook을 사용하고 놀라운 경험을!"
                description = "MyMethodBook은 매일 당신에게 새로운 경험을 선물합니다."
                imageUrl = previewImageLink
                build()
            }

            buildShortDynamicLink()
        }.also {
            it.addOnSuccessListener { dynamicLink ->
                Log.e(TAG, "성공")

                getShareableLink.invoke(dynamicLink.shortLink.toString())
            }

            it.addOnFailureListener {
                Log.e(TAG, "실패")
            }
        }
    }
}

