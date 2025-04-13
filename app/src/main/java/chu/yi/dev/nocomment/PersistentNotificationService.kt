package chu.yi.dev.nocomment

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.IBinder
import androidx.core.app.NotificationCompat

class PersistentNotificationService : Service() {

    private val CHANNEL_ID = "PersistentNotificationChannel"
    private val NOTIFICATION_ID = 1
    private val UPDATE_INTERVAL = 500L // 每5秒检测一次
    private val handler = Handler(Looper.getMainLooper())

    private var contentText = "无障碍运行检测中..."

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        startPeriodicCheck()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Persistent Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(contentText)
            .setSmallIcon(R.mipmap.logo)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun startPeriodicCheck() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateContentText()
                updateNotification()
                handler.postDelayed(this, UPDATE_INTERVAL)
            }
        }, UPDATE_INTERVAL)
    }

    private fun updateContentText() {
        val isRun = ServiceUtils.isServiceRunning(this.applicationContext, HideCommentService::class.java)
        contentText = if (isRun) "无障碍运行正常" else "无障碍权限失效，请点击返回App重新设置"
    }

    private fun updateNotification() {
        val notification = createNotification()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}    