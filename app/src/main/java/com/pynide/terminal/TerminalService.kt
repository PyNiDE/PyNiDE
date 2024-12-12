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
import android.util.SparseArray

import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.util.forEach
import androidx.core.util.isEmpty

import com.blankj.utilcode.util.ResourceUtils

import com.pynide.BuildVars
import com.pynide.R
import com.pynide.utils.FileLog

import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient

class TerminalService : Service() {
    inner class LocalBinder : Binder() {
        val terminalService get() = this@TerminalService
    }

    private val localBinder = LocalBinder()
    private val terminalSessions = SparseArray<TerminalSession>(3)
    private var terminalSessionClient: TerminalSessionClient? = null

    private val notificationService by lazy { getSystemService(NotificationManager::class.java) }

    var wantsToStop: Boolean = false

    override fun onBind(intent: Intent?): IBinder {
        return localBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        terminalSessionClient?.let { unsetTerminalSessionClient() }
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
            closeAllTerminalSessions()
        }
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
                notificationService.notify(NOTIFICATION_ID, createNotification())
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
        closeAllTerminalSessions()
        requestStopService()
    }

    @Synchronized
    fun setTerminalSessionClient(terminalSessionClient: TerminalSessionClient) {
        this.terminalSessionClient = terminalSessionClient
    }

    fun unsetTerminalSessionClient() {
        this.terminalSessionClient = null
    }

    @Synchronized
    fun getOrCreateTerminalSession(terminalType: Int): TerminalSession {
        val currentSession = terminalSessions.get(terminalType)
        if (currentSession != null && currentSession.isRunning) return currentSession

        val sessionClient = object : TerminalSessionClient {
            override fun onTextChanged(changedSession: TerminalSession) {
                terminalSessionClient?.onTextChanged(changedSession)
            }

            override fun onTitleChanged(changedSession: TerminalSession) {
                terminalSessionClient?.onTitleChanged(changedSession)
            }

            override fun onSessionFinished(finishedSession: TerminalSession) {
                terminalSessionClient?.onSessionFinished(finishedSession)
            }

            override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {
                terminalSessionClient?.onCopyTextToClipboard(session, text)
            }

            override fun onPasteTextFromClipboard(session: TerminalSession?) {
                terminalSessionClient?.onPasteTextFromClipboard(session)
            }

            override fun onBell(session: TerminalSession) {
                terminalSessionClient?.onBell(session)
            }

            override fun onColorsChanged(session: TerminalSession) {
                terminalSessionClient?.onColorsChanged(session)
            }

            override fun onTerminalCursorStateChange(state: Boolean) {
                terminalSessionClient?.onTerminalCursorStateChange(state)
            }
        }

        val newSession = when (terminalType) {
            TerminalVars.TERMINAL_TYPE_SHELL -> TerminalHelper.createSession(
                null,
                null,
                null,
                sessionClient
            )

            TerminalVars.TERMINAL_TYPE_PYTHON -> TerminalHelper.createSession(
                null,
                null,
                null,
                sessionClient
            )

            else -> TerminalHelper.createSession(null, null, null, sessionClient)
        }
        terminalSessions.put(terminalType, newSession)
        updateNotification()
        return newSession
    }

    @Synchronized
    fun resetTerminalSession(terminalType: Int): TerminalSession {
        val currentSession = terminalSessions.get(terminalType)
        currentSession?.finishIfRunning()
        return getOrCreateTerminalSession(terminalType)
    }

    @Synchronized
    fun closeTerminalSession(terminalType: Int) {
        val currentSession = terminalSessions.get(terminalType)
        currentSession?.let {
            if (!currentSession.isRunning) {
                terminalSessions.remove(terminalType)
                updateNotification()
            }
        }
    }

    @Synchronized
    private fun closeAllTerminalSessions() {
        terminalSessions.forEach { _, session ->
            session.finishIfRunning()
        }
        terminalSessions.clear()
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

    private fun createCloseActionIntent(): PendingIntent {
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

    private fun createNotification(): Notification {
        val sessionCount = terminalSessions.size()
        var notificationText = "$sessionCount session"
        notificationText += if (sessionCount == 1) "" else "s"
        notificationText += " is running"

        var notificationIcon = ResourceUtils.getDrawableIdByName("ic_logo")
        if (notificationIcon == 0) {
            notificationIcon = android.R.drawable.sym_def_app_icon
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).apply {
            setContentTitle(getString(R.string.terminal))
            setSmallIcon(notificationIcon)
            setContentText(notificationText)
            setPriority(NotificationCompat.PRIORITY_LOW)
            setShowWhen(false)
            setAutoCancel(false)
            setOngoing(true)
            setSilent(true)
            setWhen(System.currentTimeMillis())
        }

        builder.addAction(R.drawable.ic_close, getString(R.string.close), createCloseActionIntent())
        return builder.build()
    }

    @Synchronized
    fun updateNotification() {
        if (terminalSessions.isEmpty()) {
            requestStopService()
        } else {
            notificationService.notify(NOTIFICATION_ID, createNotification())
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