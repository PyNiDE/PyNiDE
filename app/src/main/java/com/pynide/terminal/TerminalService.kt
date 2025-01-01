package com.pynide.terminal

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder

import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat

import com.blankj.utilcode.util.ResourceUtils

import com.pynide.BuildVars
import com.pynide.R
import com.pynide.ui.terminal.TerminalActivity
import com.pynide.utils.AndroidUtilities
import com.pynide.utils.EmptyTerminalSessionClientImpl
import com.pynide.utils.FileLog

import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient

import java.io.File

class TerminalService : Service() {
    inner class ServiceBinder : Binder() {
        val service get() = this@TerminalService
    }

    private val serviceBinder = ServiceBinder()
    private val terminalSessions = mutableListOf<TerminalSession>()
    private var terminalSessionClient: TerminalSessionClientImpl? = null
    private var emptyTerminalSessionClient = EmptyTerminalSessionClientImpl()
    private var wantsToStop: Boolean = false

    private val notificationManager by lazy { getSystemService(NotificationManager::class.java) }

    @get:Synchronized
    val sessions: List<TerminalSession> get() = terminalSessions

    @get:Synchronized
    val isWantsToStop: Boolean get() = wantsToStop

    @get:Synchronized
    val sessionClient: TerminalSessionClient
        get() = terminalSessionClient ?: emptyTerminalSessionClient

    override fun onBind(intent: Intent?): IBinder {
        return serviceBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (terminalSessionClient != null) {
            unsetSessionClient()
        }
        return false
    }

    override fun onCreate() {
        runStartForeground()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runStartForeground()
        when (intent?.action) {
            ACTION_SERVICE_STOP -> actionStopService()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        if (!wantsToStop) {
            closeAllSessions()
        }
        AndroidUtilities.clearTMPDIR()
        runStopForeground()
        super.onDestroy()
    }

    private fun runStartForeground() {
        try {
            createNotificationChannel()
            ServiceCompat.startForeground(
                this, NOTIFICATION_ID, createNotification(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
                } else {
                    0
                },
            )
        } catch (e: Throwable) {
            FileLog.e(e)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException) {
                notificationManager.notify(NOTIFICATION_ID, createNotification())
            }
        }
    }

    private fun runStopForeground() {
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Throwable) {

        }
    }

    private fun requestStopService() {
        runStopForeground()
        stopSelf()
    }

    private fun actionStopService() {
        wantsToStop = true
        closeAllSessions()
        requestStopService()
    }

    @Synchronized
    private fun closeAllSessions() {
        terminalSessions.forEach { session ->
            session.finishIfRunning()
        }
        terminalSessions.clear()
    }

    @Synchronized
    fun setSessionClient(sessionClient: TerminalSessionClientImpl?) {
        this.terminalSessionClient = sessionClient
    }

    fun unsetSessionClient() {
        this.terminalSessionClient = null
    }

    @Synchronized
    fun createSession(): TerminalSession {
        val newSession =
            TerminalHelper.createSession(null, null, null, sessionClient)
        terminalSessions.add(newSession)
        // TODO Notify sessions updated
        return newSession
    }

    @Synchronized
    fun getIndexOfSession(terminalSession: TerminalSession?): Int {
        if (terminalSession == null) return -1
        terminalSessions.forEachIndexed { index, session ->
            if (terminalSession == session) {
                return index
            }
        }
        return -1
    }

    @Synchronized
    fun removeSession(sessionToRemove: TerminalSession?): Int {
        val index = getIndexOfSession(sessionToRemove)
        if (index >= 0) {
            val session = terminalSessions[index]
            if (!session.isRunning) {
                terminalSessions.remove(session)
                // TODO Notify sessions updated
            }
        }
        return index
    }

    @Synchronized
    fun getSession(index: Int): TerminalSession? {
        return if (index >= 0 && index < terminalSessions.size) {
            terminalSessions[index]
        } else {
            null
        }
    }

    @Synchronized
    fun getLastSession(): TerminalSession? {
        return if (terminalSessions.isEmpty()) {
            null
        } else {
            terminalSessions.last()
        }
    }

    @Synchronized
    fun getSessionForHandle(sessionHandle: String): TerminalSession? {
        terminalSessions.forEach { session ->
            if (sessionHandle == session.mHandle) {
                return session
            }
        }
        return null
    }

    private fun createNotificationChannel() {
        val builder = NotificationChannelCompat.Builder(
            NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW
        ).apply {
            setName(getString(R.string.terminal))
            setSound(null, null)
            setVibrationEnabled(false)
            setShowBadge(true)
        }
        NotificationManagerCompat.from(this).createNotificationChannel(builder.build())
    }

    private fun createNotification(): Notification {
        var smallIcon = ResourceUtils.getDrawableIdByName("ic_logo")
        if (smallIcon == 0) {
            smallIcon = android.R.drawable.sym_def_app_icon
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).apply {
            setSmallIcon(smallIcon)
            setContentIntent(createContentIntent())
            setContentTitle(getString(R.string.terminal_session_is_running))
            setContentText(getString(R.string.tap_close_to_stop))
            setPriority(NotificationCompat.PRIORITY_LOW)
            setShowWhen(false)
            setAutoCancel(false)
            setOngoing(true)
            setSilent(true)
            setWhen(System.currentTimeMillis())
        }

        builder.addAction(R.drawable.ic_close, getString(R.string.close), createActionStopIntent())
        return builder.build()
    }

    private fun createContentIntent(): PendingIntent {
        val context = this
        Intent(context, TerminalActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }.also { intent ->
            return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    private fun createActionStopIntent(): PendingIntent {
        val context = this
        Intent(context, TerminalService::class.java).apply {
            action = ACTION_SERVICE_STOP
        }.also { intent ->
            return PendingIntent.getService(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    @Synchronized
    fun updateNotification() {
        if (terminalSessions.isEmpty()) {
            requestStopService()
        } else {
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        }
    }

    companion object {
        @JvmStatic
        private val ACTION_SERVICE_STOP = "${BuildVars.PACKAGE_NAME}.action.SERVICE_STOP"

        @JvmStatic
        private val NOTIFICATION_CHANNEL_ID = "${BuildVars.PACKAGE_NAME}.notification_channel"
        private const val NOTIFICATION_ID = 1000
    }
}