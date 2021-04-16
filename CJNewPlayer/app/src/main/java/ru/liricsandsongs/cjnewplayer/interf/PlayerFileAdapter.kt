package ru.liricsandsongs.cjnewplayer.interf
import android.media.MediaPlayer
import ru.liricsandsongs.cjnewplayer.setters.FileSongs

interface PlayerFileAdapter {
    fun resumeOrPauseFile()
    fun isMediaPlayer(): Boolean
    fun isPlaying(): Boolean
    fun isReset(): Boolean
    fun fileInstantReset()
    fun getMediaPlayer(): MediaPlayer?
    fun skipFile(isNext: Boolean)
    fun seekTo(position: Int)
    fun setPlaybackInfoListener(playbackInfoListener: PlaybackInfoListener)
    fun registerNotificationActionsReceiver(isRegister: Boolean)
    fun setCurrentFileSong(song: FileSongs, songs: List<FileSongs>)
    fun onPauseActivity()
    fun onResumeActivity()
    fun initMediaPlayerFave()
    fun release()
    @PlaybackInfoListener.State
    fun getState(): Int
    fun getPlayerPosition(): Int
    fun getCurrentFileSong():FileSongs?
}