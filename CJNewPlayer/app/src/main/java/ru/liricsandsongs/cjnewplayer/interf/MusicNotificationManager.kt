package ru.liricsandsongs.cjnewplayer.interf

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.session.MediaSessionManager
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import ru.liricsandsongs.cjnewplayer.HomeFragment
import ru.liricsandsongs.cjnewplayer.MainActivity
import ru.liricsandsongs.cjnewplayer.MusicService
import ru.liricsandsongs.cjnewplayer.R
import ru.liricsandsongs.cjnewplayer.setters.FileSongs
import ru.liricsandsongs.cjnewplayer.setters.Song


import java.lang.Exception


class MusicNotificationManager internal constructor(private val mMusicService: MusicService) {
    private val CHANNEL_ID = "action.CHANNEL_ID"
    private val REQUEST_CODE = 100
    val notificationManager: NotificationManager
    var notificationBuilder: NotificationCompat.Builder? = null

        private set
    var fNotificationBuilder: NotificationCompat.Builder? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionManager: MediaSessionManager? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null
    private val context: Context

    init {
        notificationManager = mMusicService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        context = mMusicService.baseContext
    }
    private fun backToAct(){
        notificationManager.notify(0, createNotification())
    }
    private fun playerAction(action: String): PendingIntent {

        val pauseIntent = Intent()
        pauseIntent.action = action

        return PendingIntent.getBroadcast(
            mMusicService,
            REQUEST_CODE,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    fun createFileNotification():Notification{

        val song = mMusicService.mediaPlayerHolder?.getCurrentFileSong()

        fNotificationBuilder = NotificationCompat.Builder(mMusicService, CHANNEL_ID)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val openPlayerIntent = Intent(mMusicService, HomeFragment::class.java)
        openPlayerIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val contentIntent = PendingIntent.getActivity(
            mMusicService, REQUEST_CODE,
            openPlayerIntent, 0
        )
try{
    val artist = song!!.artistName
    val songTitle = song.title
    val intent = Intent(context, MainActivity::class.java)
    val actionPendingIntent = PendingIntent
        .getActivity(
            context,
            0,
            intent,
            0
        )

    initFileMediaSession(song)
    val icon = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.icon
    )
    fNotificationBuilder!!
        .setShowWhen(false)
        .setSmallIcon(R.drawable.ic_music_player)
        .setColor(ContextCompat.getColor(context, R.color.colorAccent))
        .setContentTitle(songTitle)
        .setContentText(artist)
        .setContentIntent(contentIntent)
        .setLargeIcon(icon)
        .addAction(notificationAction(PREV_ACTION))
        .addAction(notificationAction(PLAY_PAUSE_ACTION))
        .addAction(notificationAction(NEXT_ACTION))
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)


    fNotificationBuilder!!.setStyle(
        androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession!!.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)
    )
        .setContentIntent(actionPendingIntent)
}catch (e:Exception){
    e.printStackTrace()
}

        return fNotificationBuilder!!.build()
    }
    fun createNotification(): Notification {
        val fileSongs = mMusicService.mediaPlayerHolder?.getCurrentFileSong()
        val song = mMusicService.mediaPlayerHolder?.getCurrentSong()

        notificationBuilder = NotificationCompat.Builder(mMusicService, CHANNEL_ID)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val openPlayerIntent = Intent(mMusicService, HomeFragment::class.java)
        openPlayerIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val contentIntent = PendingIntent.getActivity(
            mMusicService, REQUEST_CODE,
            openPlayerIntent, 0
        )
    try{
        var artist = song!!.artist_name
        val songTitle = song.title
        val intent = Intent(context, MainActivity::class.java)
        val actionPendingIntent = PendingIntent
            .getActivity(
                context,
                0,
                intent,
                0
            )

        initMediaSession(song)

        val icon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.icon
        )
       notificationBuilder!!
            .setShowWhen(false)
            .setSmallIcon(R.drawable.ic_music_player)
            .setColor(ContextCompat.getColor(context, R.color.colorAccent))
            .setContentTitle(songTitle)
            .setContentText(artist)
            .setContentIntent(contentIntent)
            .setLargeIcon(icon)
            .addAction(notificationAction(PREV_ACTION))
            .addAction(notificationAction(PLAY_PAUSE_ACTION))
            .addAction(notificationAction(NEXT_ACTION))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)


        notificationBuilder!!.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession!!.sessionToken)
                .setShowActionsInCompactView(0, 1, 2)
        )
            .setContentIntent(actionPendingIntent)
        return notificationBuilder!!.build()
    }catch (e:Exception){
        var artist = fileSongs!!.artistName
        val songTitle = fileSongs.title
        val intent = Intent(context, MainActivity::class.java)
        val actionPendingIntent = PendingIntent
            .getActivity(
                context,
                0,
                intent,
                0
            )

        initFileMediaSession(fileSongs)

        val icon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.icon
        )
        notificationBuilder!!
            .setShowWhen(false)
            .setSmallIcon(R.drawable.ic_music_player)
            .setColor(ContextCompat.getColor(context, R.color.colorAccent))
            .setContentTitle(songTitle)
            .setContentText(artist)
            .setContentIntent(contentIntent)
            .setLargeIcon(icon)
            .addAction(notificationAction(PREV_ACTION))
            .addAction(notificationAction(PLAY_PAUSE_ACTION))
            .addAction(notificationAction(NEXT_ACTION))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)


        notificationBuilder!!.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession!!.sessionToken)
                .setShowActionsInCompactView(0, 1, 2)
        )
            .setContentIntent(actionPendingIntent)
        return notificationBuilder!!.build()
    }

    }

    private fun notificationAction(action: String): NotificationCompat.Action {

        val icon: Int

        when (action) {
            PREV_ACTION -> icon = R.drawable.ic_skip_previous
            PLAY_PAUSE_ACTION ->

                icon =
                    if (mMusicService.mediaPlayerHolder?.getState() != PlaybackInfoListener.State.PAUSED)
                        R.drawable.ic_pause
                    else
                        R.drawable.ic_play
            NEXT_ACTION -> icon = R.drawable.ic_skip_next
            else -> icon = R.drawable.ic_skip_previous
        }
        return NotificationCompat.Action.Builder(icon, action, playerAction(action)).build()
    }

    @RequiresApi(26)
    private fun createNotificationChannel() {

        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                mMusicService.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            )

            notificationChannel.description = mMusicService.getString(R.string.app_name)

            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.setShowBadge(false)

            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

    private fun initMediaSession(song: Song) {
        mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        mediaSession = MediaSessionCompat(context, "AudioPlayer")
        transportControls = mediaSession!!.controller.transportControls
        mediaSession!!.isActive = true
        mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        updateMetaData(song)
    }
private fun initFileMediaSession(fileSongs: FileSongs){
    mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    mediaSession = MediaSessionCompat(context, "AudioPlayer")
    transportControls = mediaSession!!.controller.transportControls
    mediaSession!!.isActive = true
    mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
    updateFileMetaData(fileSongs)
}
    private fun updateMetaData(song: Song) {
        mediaSession!!.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist_name)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .build()
        )
    }
    private fun updateFileMetaData(fileSongs: FileSongs){
        mediaSession!!.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, fileSongs.artistName)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, fileSongs.title)
                .build()
        )
    }

    companion object {
        val NOTIFICATION_ID = 101
        internal val PLAY_PAUSE_ACTION = "action.PLAYPAUSE"
        internal val NEXT_ACTION = "action.NEXT"
        internal val PREV_ACTION = "action.PREV"
    }

}
