package ru.liricsandsongs.cjnewplayer.interf

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import ru.liricsandsongs.cjnewplayer.MusicService
import ru.liricsandsongs.cjnewplayer.setters.FileSongs
import ru.liricsandsongs.cjnewplayer.setters.Song

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MediaPlayerHolder(private val mMusicService: MusicService?) :
        PlayerAdapter, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
    OnImageClickListener {
    private val mContext: Context

    private val mAudioManager: AudioManager
    var isPrepared = false
    private var mMediaPlayer: MediaPlayer? = null
    private var mPlaybackInfoListener: PlaybackInfoListener? = null
    private var mExecutor: ScheduledExecutorService? = null
    private var mSeekBarPositionUpdateTask: Runnable? = null
    lateinit var filepath:String
    private var mSelectedSong: Song? = null
    private var mSongs: List<Song>? = null

    private var mSelectedFileSong: FileSongs? = null
    private var mFileSongs: List<FileSongs>? = null

    private var sReplaySong = false
    @PlaybackInfoListener.State
    private var mState: Int = 0
    private var fNotificationActionsReceiver: FileNotificationReciever? = null
    private var mNotificationActionsReceiver: NotificationReceiver? = null
    private var mMusicNotificationManager: MusicNotificationManager? = null
    private var mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
    private var mPlayOnFocusGain: Boolean = false
    private val mOnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> mCurrentAudioFocusState = AUDIO_FOCUSED
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                mCurrentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
                mPlayOnFocusGain = isMediaPlayer() && mState == PlaybackInfoListener.State.PLAYING || mState == PlaybackInfoListener.State.RESUMED
            }
            AudioManager.AUDIOFOCUS_LOSS ->
                mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
        }
        if (mMediaPlayer != null) {
            configurePlayerState()
        }
    }
    init {
        mContext = mMusicService!!.applicationContext
        mAudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private fun registerActionsReceiver() {
        try{
            mNotificationActionsReceiver = NotificationReceiver()
            val intentFilter = IntentFilter()
            intentFilter.addAction(MusicNotificationManager.PREV_ACTION)
            intentFilter.addAction(MusicNotificationManager.PLAY_PAUSE_ACTION)
            intentFilter.addAction(MusicNotificationManager.NEXT_ACTION)
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
            intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            mMusicService!!.registerReceiver(mNotificationActionsReceiver, intentFilter)
        }catch (e:java.lang.Exception){
e.printStackTrace()
        }
    }
    private fun unregisterActionsReceiver() {
        if (mMusicService != null && mNotificationActionsReceiver != null) {
            try {
                mMusicService.unregisterReceiver(mNotificationActionsReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }
    private fun unregisterFActionsReceiver() {
        if (mMusicService != null && fNotificationActionsReceiver != null) {
            try {
                mMusicService.unregisterReceiver(fNotificationActionsReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }
    override fun registerNotificationActionsReceiver(isRegister: Boolean) {
        if (isRegister) {
            registerActionsReceiver()
        }else{
            unregisterFActionsReceiver()
        }
    }
    override fun getCurrentSong(): Song? {
        return mSelectedSong
    }
    override fun getCurrentFileSong(): FileSongs?{
        return mSelectedFileSong
    }
    override fun setCurrentSong(song: Song, songs: List<Song>) {
        mSelectedSong = song
        mSongs = songs
    }
    override fun setCurrentFileSong(song: FileSongs, songs: List<FileSongs>) {
        mSelectedFileSong = song
        mFileSongs = songs
    }
    override fun onCompleted(){
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener!!.onStateChanged(PlaybackInfoListener.State.COMPLETED)
            mPlaybackInfoListener!!.onPlaybackCompleted()
        }
        if (sReplaySong) {
            if (isMediaPlayer()) {
                resetSong()
            }
            sReplaySong = false
        }
    }
    override fun onCompletion(mediaPlayer: MediaPlayer) {
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener!!.onStateChanged(PlaybackInfoListener.State.COMPLETED)
            mPlaybackInfoListener!!.onPlaybackCompleted()
        }
        if (sReplaySong) {
            if (isMediaPlayer()) {
                resetSong()
            }
            sReplaySong = false
        }
    }
    override fun onResumeActivity() {
        startUpdatingCallbackWithPosition()
    }

    override fun onPauseActivity() {
        stopUpdatingCallbackWithPosition()
    }

    private fun tryToGetAudioFocus() {
try{
    val result = mAudioManager.requestAudioFocus(
        mOnAudioFocusChangeListener,
        AudioManager.STREAM_MUSIC,
        AudioManager.AUDIOFOCUS_GAIN)
    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
        mCurrentAudioFocusState = AUDIO_FOCUSED
    } else {
        mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
    }
}catch (e:java.lang.Exception){
    e.printStackTrace()
}
    }
    private fun giveUpAudioFocus() {
        if (mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
        }
    }
    override fun setPlaybackInfoListener(playbackInfoListener: PlaybackInfoListener) {
        mPlaybackInfoListener = playbackInfoListener
    }

    override fun setPlaybackInfoListenerFave(playbackInfoListener: PlaybackInfoListener) {
        TODO("Not yet implemented")
    }

    private fun setStatus(@PlaybackInfoListener.State state: Int) {

        mState = state
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener!!.onStateChanged(state)
        }
    }
    private fun resumeMediaPlayer() {
        if (!isPlaying()) {
            mMediaPlayer!!.start()
            setStatus(PlaybackInfoListener.State.RESUMED)
            mMusicService!!.startForeground(MusicNotificationManager.NOTIFICATION_ID, mMusicNotificationManager!!.createNotification())
        }
    }
    private fun resumeFileMediaPlayer() {
        if (!isPlaying()) {
            mMediaPlayer!!.start()
            setStatus(PlaybackInfoListener.State.RESUMED)
            mMusicService!!.startForeground(MusicNotificationManager.NOTIFICATION_ID, mMusicNotificationManager!!.createFileNotification())
        }
    }
    private fun pauseMediaPlayer() {
        setStatus(PlaybackInfoListener.State.PAUSED)
        mMediaPlayer!!.pause()
        mMusicService!!.stopForeground(false)
        mMusicNotificationManager!!.notificationManager.notify(MusicNotificationManager.NOTIFICATION_ID, mMusicNotificationManager!!.createNotification()
               )
    }
    private fun pauseFileMediaPlayer() {
        setStatus(PlaybackInfoListener.State.PAUSED)
        mMediaPlayer!!.pause()
        mMusicService!!.stopForeground(false)
        mMusicNotificationManager!!.notificationManager.notify(MusicNotificationManager.NOTIFICATION_ID, mMusicNotificationManager!!.createFileNotification()
        )
    }

    private fun resetSong() {
        mMediaPlayer!!.seekTo(0)
        mMediaPlayer!!.start()
        setStatus(PlaybackInfoListener.State.PLAYING)
    }

    private fun startUpdatingCallbackWithPosition() {
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadScheduledExecutor()
        }
        if (mSeekBarPositionUpdateTask == null) {
            mSeekBarPositionUpdateTask = Runnable { updateProgressCallbackTask() }
        }

        mExecutor!!.scheduleAtFixedRate(
                mSeekBarPositionUpdateTask,
                0,
                1000,
                TimeUnit.MILLISECONDS
        )
    }
    private fun stopUpdatingCallbackWithPosition() {
        if (mExecutor != null) {
            mExecutor!!.shutdownNow()
            mExecutor = null
            mSeekBarPositionUpdateTask = null
        }
    }

    private fun updateProgressCallbackTask() {
        if (isMediaPlayer() && mMediaPlayer!!.isPlaying) {
            val currentPosition = mMediaPlayer!!.currentPosition
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener!!.onPositionChanged(currentPosition)
            }
        }
    }

    override fun instantReset() {
        if (isMediaPlayer()) {
            if (mMediaPlayer!!.currentPosition < 5000) {
                skip(false)
            } else {
                resetSong()
            }
        }
    }
    override fun fileInstantReset() {
        if (isMediaPlayer()) {
            if (mMediaPlayer!!.currentPosition < 5000) {
                skipFile(false)
            } else {
                resetSong()
            }
        }
    }

    override fun initMediaPlayer() {
try{
    try {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.reset()
        } else {
            mMediaPlayer = MediaPlayer()
        }

        tryToGetAudioFocus()
        filepath = "http://37.143.14.240${mSelectedSong!!.mp3_url}"
        try{
                    mMediaPlayer!!.setDataSource(filepath)
            try{

                mMediaPlayer!!.prepareAsync()


                mMediaPlayer!!.setOnPreparedListener(this)
                mMediaPlayer!!.setOnCompletionListener(this)
                mMediaPlayer!!.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK)
                mMediaPlayer!!.setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                mMusicNotificationManager = mMusicService!!.musicNotificationManager
            }catch (e:java.lang.Exception){
                e.printStackTrace()
            }

        }catch (e:java.lang.Exception){
            e.printStackTrace()
            mMediaPlayer!!.setDataSource(filepath)
        }

    } catch (e: Exception) {
        e.printStackTrace()

    }
}catch (e:Exception){
    e.printStackTrace()
}
    }
    override fun initMediaPlayerFave() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer!!.reset()
            } else {
                mMediaPlayer = MediaPlayer()

                mMediaPlayer!!.setOnPreparedListener(this)
                mMediaPlayer!!.setOnCompletionListener(this)
                mMediaPlayer!!.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK)
                mMediaPlayer!!.setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                mMusicNotificationManager = mMusicService!!.musicNotificationManager
            }
            tryToGetAudioFocus()
            try{
                filepath = "${mSelectedFileSong!!.path}"
                mMediaPlayer!!.setDataSource(filepath)
            }catch (e:java.lang.Exception){
                e.printStackTrace()
                mMediaPlayer!!.setDataSource(filepath)
            }
            mMediaPlayer!!.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
            skipFile(true)
        }
    }


    override fun getMediaPlayer(): MediaPlayer? {
        return mMediaPlayer
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
try{
    startUpdatingCallbackWithPosition()
    setStatus(PlaybackInfoListener.State.PLAYING)
    isPrepared = true
    mediaPlayer.start()

}catch (e:Exception){
    e.printStackTrace()
}

    }
    override fun release() {
        if (isMediaPlayer()) {
            mMediaPlayer!!.release()
            mMediaPlayer = null

            giveUpAudioFocus()
            unregisterActionsReceiver()
        }
    }
    @SuppressLint("MissingPermission")
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }
    override fun isPlaying(): Boolean {
        return isMediaPlayer() && mMediaPlayer!!.isPlaying
    }

    override fun resumeOrPause() {

        if (isPlaying()) {
            pauseMediaPlayer()
        } else {
            resumeMediaPlayer()
        }
    }
    override fun resumeOrPauseFile() {

        if (isPlaying()) {
            pauseFileMediaPlayer()
        } else {
            resumeFileMediaPlayer()
        }
    }
    @PlaybackInfoListener.State
    override fun getState(): Int {
        return mState
    }

    override fun isMediaPlayer(): Boolean {
        return mMediaPlayer != null
    }

    override fun reset() {
        sReplaySong = !sReplaySong
    }

    override fun isReset(): Boolean {
        return sReplaySong
    }

    override fun skip(isNext: Boolean) {
        getSkipSong(isNext)
    }

    private fun getSkipSong(isNext: Boolean) {
        try{
            if(filepath.removeRange(5, filepath.length)=="/data"){

                val currentIndex = mFileSongs!!.indexOf(mSelectedFileSong)

                val index: Int

                try {
                    index = if (isNext) currentIndex + 1 else currentIndex - 1
                    mSelectedFileSong = mFileSongs!![index]
                    initMediaPlayerFave()
                } catch (e: IndexOutOfBoundsException) {
                    mSelectedFileSong = if (currentIndex != 0) mFileSongs!![0] else mFileSongs!![mFileSongs!!.size - 1]
                    initMediaPlayerFave()
                    e.printStackTrace()
                }
            }else{
                val currentIndex = mSongs!!.indexOf(mSelectedSong)
                val index: Int
                try {
                    index = if (isNext) currentIndex + 1 else currentIndex - 1
                    mSelectedSong = mSongs!![index]
                } catch (e: IndexOutOfBoundsException) {
                    mSelectedSong = if (currentIndex != 0) mSongs!![0] else mSongs!![mSongs!!.size - 1]
                    e.printStackTrace()
                }

                initMediaPlayer()
            }

        }catch (e:java.lang.Exception){
        }
    }
    private fun getSkipFileSong(isNext: Boolean){
        val currentIndex = mFileSongs!!.indexOf(mSelectedFileSong)
        val index: Int
        try{
            index = if(isNext) currentIndex + 1 else currentIndex - 1
            mSelectedFileSong = mFileSongs!![index]
        }catch (e: java.lang.IndexOutOfBoundsException){
            mSelectedFileSong = if (currentIndex!=0) mFileSongs!![0] else mFileSongs!![mFileSongs!!.size - 1]
            e.printStackTrace()
        }
        initMediaPlayerFave()
    }
    override fun skipFile(isNext: Boolean){
        getSkipFileSong(isNext)
    }

    override fun seekTo(position: Int) {
        if (isMediaPlayer()) {
            mMediaPlayer!!.seekTo(position)
        }
    }

    override fun getPlayerPosition(): Int {
        return mMediaPlayer!!.currentPosition
    }

    private fun configurePlayerState() {

        if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_NO_DUCK) {
            pauseMediaPlayer()
        } else {

            if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_CAN_DUCK) {
                mMediaPlayer!!.setVolume(VOLUME_DUCK, VOLUME_DUCK)
            } else {
                mMediaPlayer!!.setVolume(VOLUME_NORMAL, VOLUME_NORMAL)
            }
            if (mPlayOnFocusGain) {
                resumeMediaPlayer()
                mPlayOnFocusGain = false
            }
        }
    }
private inner class FileNotificationReciever : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {

        val action = intent.action

        if (action != null) {

            when (action) {
                MusicNotificationManager.PREV_ACTION -> fileInstantReset()
                MusicNotificationManager.PLAY_PAUSE_ACTION -> resumeOrPauseFile()
                MusicNotificationManager.NEXT_ACTION -> skip(true)

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> if (mSelectedSong != null) {
                    pauseFileMediaPlayer()
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> if (mSelectedSong != null && !isPlaying()) {
                    resumeFileMediaPlayer()
                }
                Intent.ACTION_HEADSET_PLUG -> if (mSelectedSong != null) {
                    when (intent.getIntExtra("state", -1)) {
                        0 -> pauseFileMediaPlayer()
                        1 -> if (!isPlaying()) {
                            resumeFileMediaPlayer()
                        }
                    }
                }
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> if (isPlaying()) {
                    pauseFileMediaPlayer()
                }
            }
        }
    }
}
    private inner class NotificationReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action != null) {

                when (action) {
                    MusicNotificationManager.PREV_ACTION -> instantReset()
                    MusicNotificationManager.PLAY_PAUSE_ACTION -> resumeOrPause()
                    MusicNotificationManager.NEXT_ACTION -> skip(true)


                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> if (mSelectedSong != null) {
                        pauseMediaPlayer()
                    }
                    BluetoothDevice.ACTION_ACL_CONNECTED -> if (mSelectedSong != null && !isPlaying()) {
                        resumeMediaPlayer()
                    }
                    Intent.ACTION_HEADSET_PLUG -> if (mSelectedSong != null) {
                        when (intent.getIntExtra("state", -1)) {
                            0 -> pauseMediaPlayer()
                            1 -> if (!isPlaying()) {
                                resumeMediaPlayer()
                            }
                        }
                    }
                    AudioManager.ACTION_AUDIO_BECOMING_NOISY -> if (isPlaying()) {
                        pauseMediaPlayer()
                    }
                }
            }
        }
    }

    companion object {
        private val VOLUME_DUCK = 0.2f
        private val VOLUME_NORMAL = 1.0f
        private val AUDIO_NO_FOCUS_NO_DUCK = 0
        private val AUDIO_NO_FOCUS_CAN_DUCK = 1
        private val AUDIO_FOCUSED = 2
    }
    override fun onImageClick(song: String?) {

    }
}
