package ru.liricsandsongs.cjnewplayer.interf
import android.media.MediaPlayer
import ru.liricsandsongs.cjnewplayer.setters.FileSongs
import ru.liricsandsongs.cjnewplayer.setters.Song

interface PlayerAdapter {

    fun isMediaPlayer(): Boolean
    fun isPlaying(): Boolean
    fun isReset(): Boolean
    fun getCurrentSong(): Song?
    fun getCurrentFileSong(): FileSongs?
    @PlaybackInfoListener.State
    fun getState(): Int
    fun getPlayerPosition(): Int
    fun getMediaPlayer(): MediaPlayer?
    fun initMediaPlayer()
    fun initMediaPlayerFave()
    fun release()
    fun resumeOrPause()
    fun resumeOrPauseFile()
    fun reset()
    fun instantReset()
    fun fileInstantReset()
    fun skipFile(isNext: Boolean)
    fun skip(isNext: Boolean)
    fun seekTo(position: Int)
    fun setPlaybackInfoListener(playbackInfoListener: PlaybackInfoListener)
    fun setPlaybackInfoListenerFave(playbackInfoListener: PlaybackInfoListener)
    fun registerNotificationActionsReceiver(isRegister: Boolean)

    fun setCurrentSong(song: Song, songs: List<Song>)
    fun setCurrentFileSong(song: FileSongs, songs: List<FileSongs>)
    fun onPauseActivity()
    fun onResumeActivity()
    fun onCompleted()
}
