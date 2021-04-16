package ru.liricsandsongs.cjnewplayer
import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_fave.view.*
import kotlinx.android.synthetic.main.fave_control.*
import kotlinx.android.synthetic.main.fave_control.view.*
import org.json.JSONArray
import org.json.JSONObject
import ru.liricsandsongs.cjnewplayer.adaps.RecyclerAdapterFave
import ru.liricsandsongs.cjnewplayer.interf.*
import ru.liricsandsongs.cjnewplayer.setters.AdsSett
import ru.liricsandsongs.cjnewplayer.setters.FileSongs
import ru.liricsandsongs.cjnewplayer.setters.Song

import java.io.File
import kotlin.collections.ArrayList


class FaveFragment : Fragment(), OnImageClickListener, RecyclerAdapterFave.SongClicked,
    RecyclerAdapterFave.SongsSelected,ActionMode.Callback,View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var viewModel: FavoritesViewModel
    lateinit var recyclerView: RecyclerView
    lateinit var tracks:String
    var seekBar: SeekBar? = null
    lateinit var filesSongs: List<FileSongs>
    private lateinit var imageViewvv: ImageView
    private var mIsBound: Boolean? = null
    lateinit var adapter: RecyclerAdapterFave
    private var mPlayerAdapter: PlayerAdapter? = null
    private var playPause: ImageButton? = null
    private var next: ImageButton? = null
    lateinit var cs:ConstraintLayout
    private var swipeRefreshLayout:SwipeRefreshLayout? = null
    lateinit var Begin: AdView
    private var previous: ImageButton? = null
    private var mMusicService: MusicService? = null
    lateinit var missss:TextView
         private lateinit var swiper:SwipeRefreshLayout
    private var mUserIsSeeking = false
    private var actionMode: ActionMode? = null
    private var mPlaybackListener: PlaybackListener? = null
    private var mMusicNotificationManager:
            MusicNotificationManager? = null
    private var songs = ArrayList<Song>()
    private var fileSongss = ArrayList<FileSongs>()

    lateinit var ads:ImageView
    lateinit var adStr:String

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            mMusicService = (iBinder as MusicService.LocalBinder).instance
            mPlayerAdapter = mMusicService!!.mediaPlayerHolder
            mMusicNotificationManager = mMusicService!!.musicNotificationManager
            if (mPlaybackListener == null) {
                mPlaybackListener = PlaybackListener()
                mPlayerAdapter!!.setPlaybackInfoListener(mPlaybackListener!!)
            }
            if (mPlayerAdapter != null && mPlayerAdapter!!.isPlaying()) {
                restorePlayerStatus()
            }
            checkReadStoragePermissions()
        }
        override fun onServiceDisconnected(componentName: ComponentName) {
            mMusicService = null
        }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProviders.of(this).get(FavoritesViewModel::class.java)
        val root = inflater.inflate(R.layout.activity_fave, container, false)

        recyclerView = root.recyclerVieww
        seekBar = root.seekBarr

        initializeSeekBar()
        cs = root.findViewById(R.id.faveconst)
        imageViewvv = root.findViewById(R.id.imageViewControls)
        imageViewvv.setImageResource(R.drawable.icon)
        playPause = root.findViewById(R.id.buttonPlayPauses)
        next = root.findViewById(R.id.buttonNexts)
        previous = root.findViewById(R.id.buttonPreviouss)
        playPause!!.setOnClickListener(this)
        next!!.setOnClickListener(this)
        previous!!.setOnClickListener(this)
        Begin = root.findViewById(R.id.adView) as AdView
        tracks = activity?.intent?.getStringExtra("tracks").toString()
        ads = root.image_banner2
        swiper = root.swipeLay

        try{
            getMusic()
//            recyclerView.resetPivot()
            adapter = RecyclerAdapterFave(
                mad = fileSongss,
                listener = this,
                context = requireContext()
            )
            adapter.notifyDataSetChanged()
            adapter.setSongsSelected(this)
            adapter.setOnSongClicked(this)
            recyclerView.adapter = adapter

            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.setHasFixedSize(true)
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }

        if(fileSongss.isEmpty()){
            cs.visibility = View.GONE
        }else{
            cs.visibility = View.VISIBLE
        }
        addAds()
try{
    adStr = activity?.intent?.getStringExtra("one").toString()
    val adOb = JSONObject(adStr)
    val adsSett = AdsSett(adOb)
    if(adsSett.img!=""){
        Picasso.get().load("$url${adsSett.img}").error(R.drawable.musicpicture)
            .placeholder(R.drawable.musicpicture).into(
                ads
            )
        ads.setOnClickListener {
            val link = adsSett.urlad
            val uri = Uri.parse(link)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }else{
        ads.visibility = View.GONE
    }
}catch (e:Exception){
    e.printStackTrace()
}
        swiper.setOnRefreshListener(this)
        return root
    }
    internal inner class PlaybackListener : PlaybackInfoListener() {
        override fun onPositionChanged(position: Int) {
            if (!mUserIsSeeking) {
                this@FaveFragment.seekBar?.progress = position
            }
        }
        override fun onStateChanged(@State state: Int) {
            updatePlayingStatus()
            if (mPlayerAdapter!!.getState() != State.PAUSED
                && mPlayerAdapter!!.getState() != State.PAUSED) {
                updatePlayingInfo(false, true)
            }
        }

        override fun onPlaybackCompleted() {
            skipNext()
        }
    }
    private fun restorePlayerStatus() {
        seekBar!!.isEnabled = mPlayerAdapter!!.isMediaPlayer()

        if (mPlayerAdapter != null && mPlayerAdapter!!.isMediaPlayer()) {

            mPlayerAdapter!!.onResumeActivity()
            updatePlayingInfo(true, false)
        }
    }
private fun getListFiles(parentDir: File): List<File>? {
    val inFiles = ArrayList<File>()

    val files = parentDir.listFiles()
    for (file in files!!) {

        if (file.isDirectory) {
            inFiles.addAll(getListFiles(file)!!)
        } else {
            if (file.name.endsWith(".mp3")) {
                inFiles.add(file)
            }
        }
    }
    return inFiles
}
    private fun updatePlayingInfo(restore: Boolean, startPlay: Boolean) {
        if (startPlay) {
            try{
                mPlayerAdapter!!.getMediaPlayer()?.start()
                Handler().postDelayed({
                    mMusicService!!.startForeground(
                        MusicNotificationManager.NOTIFICATION_ID,
                        mMusicNotificationManager!!.createFileNotification()
                    )
                }, 200)
                val selectedSong = mPlayerAdapter!!.getCurrentFileSong()
                songTitles?.text = selectedSong?.title
                val duration = mPlayerAdapter!!.getMediaPlayer()?.duration
                seekBar?.max = duration!!
                if (restore) {
                    seekBar!!.progress = mPlayerAdapter!!.getPlayerPosition()
                    updatePlayingStatus()
                    Handler().postDelayed({
                        if (mMusicService!!.isRestoredFromPause) {
                            mMusicService!!.stopForeground(false)
                            mMusicService!!.musicNotificationManager!!.notificationManager
                                .notify(
                                    MusicNotificationManager.NOTIFICATION_ID,
                                    mMusicService!!.musicNotificationManager!!.fNotificationBuilder!!.build()
                                )
                            mMusicService!!.isRestoredFromPause = false
                        }
                    }, 200)
                }
            }catch (e:java.lang.Exception){
                e.printStackTrace()
            }
        }
    }
    private fun checkReadStoragePermissions() {
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.INTERNET) } != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.INTERNET,
                    Manifest.permission.INTERNET
                ), 1
            )
        }
    }
    private fun updatePlayingStatus() {
        val drawable = if (mPlayerAdapter!!.getState() != PlaybackInfoListener.State.PAUSED)
            R.drawable.ic_pause
        else
            R.drawable.ic_play
        restorePlayerStatus()
        playPause!!.post { playPause!!.setImageResource(drawable) }
    }
    override fun onImageClick(song: String?) {
    }

    override fun onSelectSongs(selectedSongs: MutableList<FileSongs>) {
        if (selectedSongs.isEmpty()) {
            actionMode?.finish()
            adapter.removeSelection()
        } else {
            val title = "Delete ${selectedSongs.size} Songs"
            actionMode?.title = title
        }
    }
    private fun doBindService() {

        requireContext().bindService(
            Intent(
                activity,
                MusicService::class.java
            ), mConnection, Context.BIND_AUTO_CREATE
        )
        mIsBound = true

        val startNotStickyIntent = Intent(activity, MusicService::class.java)
        requireContext().startService(startNotStickyIntent)
    }

    override fun onResume() {
        super.onResume()
        doBindService()
        if (mPlayerAdapter != null && mPlayerAdapter!!.isPlaying()) {
            mPlaybackListener = this.PlaybackListener()

            mPlayerAdapter!!.setPlaybackInfoListener(mPlaybackListener!!)
            restorePlayerStatus()
        }
    }

    override fun onSongClicked(song: FileSongs) {
        onSongSelected(song, fileSongss)
        mPlayerAdapter!!.getMediaPlayer()
        mPlayerAdapter!!.setPlaybackInfoListener(this.PlaybackListener())
        val duration = mPlayerAdapter!!.getMediaPlayer()?.duration

        seekBarr?.max = duration!!
        seekBarr?.progress = mPlayerAdapter!!.getPlayerPosition()
        songTitles.text = song.title
        restorePlayerStatus()
        updatePlayingStatus()
    }
    private fun getMusic() {
        fileSongss =  ArrayList<FileSongs>()

        val list = context?.filesDir?.let { getListFiles(it) }

        for(i in 0 until list?.size!!){
            val filePath = list.get(i).toString()
            val file = File(filePath)
            val filename = file.name
            val fileSongs = FileSongs(title = filename, path = filePath, artistName = "CJ", liric = "")
            fileSongss.add(fileSongs)
        }
    }
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onDestroyActionMode(mode: ActionMode?) {

    }

    private fun skipPrev() {
        try{
            if (checkIsPlayer()) {
                mPlayerAdapter!!.fileInstantReset()

                songTitles.text =  mPlayerAdapter!!.getCurrentFileSong()?.title
            }
        }catch (e:java.lang.Exception){
            if (checkIsPlayer()) {
                mPlayerAdapter!!.instantReset()
            }
        }

    }
    private fun skipNext() {

            if(checkIsPlayer()){
                mPlayerAdapter!!.skipFile(true)
                val filesonga = mPlayerAdapter?.getCurrentFileSong()
                songTitles.text =  filesonga?.title
        }
    }

    private fun addAds(){

        val adReq = AdRequest.Builder()
            .addTestDevice("BAE637B7C7EA01DD1BEEC10D3BD54439")
            .build()

        Begin.loadAd(adReq)
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonPlayPauses -> {
                try{

                    resumeOrPauseFile()
                    updatePlayingStatus()
                }catch (e:java.lang.Exception){
                    e.printStackTrace()

                }
            }
            R.id.buttonNexts -> {
                skipNext()
            }
            R.id.buttonPreviouss -> {
                skipPrev()
            }
        }
    }

    private fun checkIsPlayer(): Boolean {
        return mPlayerAdapter!!.isMediaPlayer()
    }
    private fun onSongSelected(song: FileSongs, songs: List<FileSongs>) {
        if (!seekBarr!!.isEnabled) {
            seekBarr!!.isEnabled = true
        }
        try {
            mPlayerAdapter!!.setCurrentFileSong(song, songs)
            mPlayerAdapter!!.initMediaPlayerFave()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun resumeOrPauseFile() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.resumeOrPauseFile()
        } else {

            fileSongss = ArrayList<FileSongs>()
                try{
                    fileSongss =  ArrayList<FileSongs>()
                    val list = context?.filesDir?.let { getListFiles(it) }
                    for(i in 0 until list?.size!!) {
                        val filePath = list.get(i).toString()
                        val file = File(filePath)
                        val filename = file.name
                        val fileSongs =
                            FileSongs(title = filename, path = filePath, artistName = "CJ", liric = "")
                        fileSongss.add(fileSongs)
                        if (fileSongss.isNotEmpty()) {
                            onSongSelected(fileSongss[0], fileSongss)
                        }
                    }
                }catch (e: java.lang.Exception){
                    e.printStackTrace()
                }

        }
    }
    private fun initializeSeekBar() {
        seekBar!!.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                var userSelectedPosition = 0

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    mUserIsSeeking = true
                }

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                    if (fromUser) {
                        userSelectedPosition = progress

                    }

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                    mUserIsSeeking = false
                    mPlayerAdapter!!.seekTo(userSelectedPosition)
                }
            })
    }

    override fun onRefresh() {
        try{
            fileSongss.clear()
            getMusic()
            adapter = context?.let {
                RecyclerAdapterFave(
                    mad = fileSongss,
                    listener = this,
                    context = it
                )
            }!!
            adapter.notifyDataSetChanged()
            adapter.setSongsSelected(this)
            adapter.setOnSongClicked(this)
            recyclerView.adapter = this.adapter

            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.setHasFixedSize(true)
            swiper.isRefreshing = false
        }catch (e:java.lang.Exception){
            e.printStackTrace()
            swiper.isRefreshing = false
        }
        swiper.isRefreshing = false
    }



}