package com.example.mymethodbook.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.example.mymethodbook.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@GlideModule
class MyFirebaseMessagingService : FirebaseMessagingService() {
    /* FCM 관련 메소드*/
    // 메세지를 수신했을 때 실행된다.
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        createNotificationChannel()

        val title = message.notification?.title ?: ""
        val content = message.notification?.body ?: ""
        val imageUrl = message.notification?.imageUrl

        if(imageUrl != null) postNotification(title, content, UrlImageToBitmapImage(imageUrl))
        else postNotification(title, content, null)
    }

    /* Url 이미지 처리 관련 */
    fun UrlImageToBitmapImage(imageUrl: Uri): Bitmap {
        return Glide.with(applicationContext).asBitmap().load(imageUrl).submit().get()
    }

    /* Notification 관련 메소드 */
    val NOTIFICATION_CHANNEL_ID = "5000"
    val NOTIFICATION_ID = 5555

    // notification channel 을 생성한다.
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "content"
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
    fun postNotification(title : String, content : String, image:Bitmap?){
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setLargeIcon(image)
            .setStyle(NotificationCompat.BigPictureStyle()
                .bigPicture(image))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)){
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}