package ru.liricsandsongs.cjnewplayer

import android.Manifest
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.liricsandsongs.cjnewplayer.interf.SwipeController


import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_main.view.*
import kotlinx.android.synthetic.main.controls.*
import kotlinx.android.synthetic.main.controls.view.*
import kotlinx.android.synthetic.main.fragment_control.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.json.JSONArray
import org.json.JSONObject
import ru.liricsandsongs.cjnewplayer.adaps.RecyclerAdapter
import ru.liricsandsongs.cjnewplayer.interf.*
import ru.liricsandsongs.cjnewplayer.setters.AdsSett
import ru.liricsandsongs.cjnewplayer.setters.Song


class HomeFragment : Fragment(), OnImageClickListener, RecyclerAdapter.SongClicked,RecyclerAdapter.SongsSelected,ActionMode.Callback,View.OnClickListener {
        lateinit var recyclerView: RecyclerView
    lateinit var cardview:CardView
    lateinit var cs:ConstraintLayout
    var mProgressDialog: ProgressDialog? = null
    lateinit var seekBar: SeekBar
    private lateinit var homeViewModel: HomeViewModel
    private var mIsBound: Boolean? = null
    lateinit var adapter:RecyclerAdapter
    lateinit var tracks:String
    private var mPlayerAdapter: PlayerAdapter? = null
    private var playPause: ImageButton? = null
    private var next: ImageButton? = null
    private lateinit var imageViewvv: ImageView
    private var previous: ImageButton? = null
    private var mMusicService: MusicService? = null
    private var mUserIsSeeking = false
    private var actionMode: ActionMode? = null
    private var mPlaybackListener: PlaybackListener? = null
    private var mMusicNotificationManager: MusicNotificationManager? = null
    private var songs = ArrayList<Song>()
    private lateinit var errdialog: Dialog
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
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
    private fun doBindService() {

        context?.bindService(
            Intent(
                activity,
                MusicService::class.java
            ), mConnection, Context.BIND_AUTO_CREATE
        )
        mIsBound = true
        val startNotStickyIntent = Intent(activity, MusicService::class.java)
        context?.startService(startNotStickyIntent)
    }
    private fun restorePlayerStatus() {
        seekBar.isEnabled = mPlayerAdapter!!.isMediaPlayer()
        if (mPlayerAdapter != null && mPlayerAdapter!!.isMediaPlayer()) {

            mPlayerAdapter!!.onResumeActivity()
            updatePlayingInfo(true, false)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.activity_main, container, false)
        tracks = activity?.intent?.getStringExtra("tracks").toString()
        imageViewvv = root.findViewById(R.id.imageViewControl)
        recyclerView = root.recyclerVieww
        seekBar = root.seekBar
        ads = root.image_banner
        cs = root.cardsv

        playPause = root.findViewById(R.id.buttonPlayPause)
        next = root.findViewById(R.id.buttonNext)
        previous = root.findViewById(R.id.buttonPrevious)
        playPause!!.setOnClickListener(this)
        next!!.setOnClickListener(this)
        previous!!.setOnClickListener(this)
        doBindService()
        initializeSeekBar()
        mProgressDialog = ProgressDialog(requireContext())
        mProgressDialog?.setMessage("A message");
        mProgressDialog?.setIndeterminate(true);
        mProgressDialog?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog?.setCancelable(true);
        try{
            try{
                adStr = activity?.intent?.getStringExtra("two").toString()
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
            }catch (e: java.lang.Exception){
                e.printStackTrace()
            }
                try{
                    val songsStr = tracks
                    val songJArray = JSONArray(songsStr)
                    songs = ArrayList<Song>()
                    val i:Int = songJArray.length()
                    for(q in 0 until i){
                        val songObj = songJArray.getJSONObject(q)
                        val title = songObj.getString("title")
                        val id = songObj.getString("id")
                        val artist = songObj.getString("artist_name")
                        val cover = songObj.getString("cover")
                        try{
                            val url = songObj.getString("mp3_url")
                            cs.visibility = View.VISIBLE
                            val liric = songObj.getString("text")
                            val song = Song(
                                id = id,
                                title = title,
                                artist_name = artist,
                                cover = cover,
                                mp3_url = url,
                                internalPath = null,
                                favorit = null,
                                liric = liric
                            )
                            songs.add(song)
                        }catch (e: java.lang.Exception){
                            e.printStackTrace()

                            val liric = songObj.getString("text")
                            val song = Song(
                                id = id,
                                title = title,
                                artist_name = artist,
                                cover = cover,
                                mp3_url = "",
                                internalPath = null,
                                favorit = null,
                                liric = liric
                            )
                            songs.add(song)
                        }
                    }
                    adapter = RecyclerAdapter(
                        mad = songs,
                        context = requireContext(),
                        st = tracks, listener = this
                    )


                    adapter.setSongsSelected(this)
                    adapter.setOnSongClicked(this)
                    recyclerView.adapter = adapter
                    recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                    recyclerView.layoutManager =LinearLayoutManager(activity)

                    var swipeController =  SwipeController(object : SwipeControllerActions {
                        override fun onRightClicked(position: Int) {
                            super.onRightClicked(position)
                            val intent = Intent(requireContext(), LiricsActivity::class.java)
                            intent.putExtra("text", songs[position].liric)
                            activity?.startActivity(intent)
                        }

                    })
                    val itemTouchHelper = ItemTouchHelper(swipeController)
                    if(songs[0].mp3_url!=""){
                        itemTouchHelper.attachToRecyclerView(recyclerView)
                        recyclerView.addItemDecoration(object :RecyclerView.ItemDecoration(){
                            override fun onDraw(
                                    c: Canvas,
                                    parent: RecyclerView,
                                    state: RecyclerView.State
                            ) {
                                swipeController.onDrawNotCome(c)
                            }
                        })
                    }


                }catch (e: java.lang.Exception){
                    e.printStackTrace()
                    errdialog =  Dialog(requireContext())
                    errdialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    errdialog.setContentView(R.layout.no_value_error_dialog)
                    errdialog.show()
                }

        }catch (e: java.lang.Exception){
            e.printStackTrace()
            errdialog =  Dialog(requireContext())
            errdialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            errdialog.setContentView(R.layout.no_value_error_dialog)
            errdialog.show()
        }
        return root
    }

    override fun onPause() {
        try{
            mPlaybackListener = null
            if(songs.size!=0){
                adapter.notifyDataSetChanged()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        super.onPause()
    }
    internal inner class PlaybackListener : PlaybackInfoListener() {
        override fun onPositionChanged(position: Int) {
            if (!mUserIsSeeking) {
                seekBar.progress = position

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
    override fun onSongClicked(song: Song) {
        try{

                CoroutineScope(Dispatchers.Main).async {
                    if(isOnline(requireContext())){

                        onSongSelected(song, songs)

                    }else{

                        errdialog =  Dialog(requireContext())
                        errdialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                        errdialog.setContentView(R.layout.no_value_error_dialog)
                        errdialog.show()
                    }
                }.start()


        }catch (e: Exception){
            errdialog
        }


    }
    private fun updatePlayingStatus() {
        try{
            val drawable = if (mPlayerAdapter!!.getState() != PlaybackInfoListener.State.PAUSED)
                R.drawable.ic_pause
            else
                R.drawable.ic_play
            playPause!!.post { playPause!!.setImageResource(drawable) }
        }catch (e: java.lang.Exception){
            e.printStackTrace()
        }

    }

    override fun onSelectSongs(selectedSongs: MutableList<Song>) {
        try{
            if(isOnline(requireContext())){
                if (selectedSongs.isEmpty()) {
                    actionMode?.finish()
                    adapter.removeSelection()
                } else {
                    val title = "Delete ${selectedSongs.size} Songs"
                    actionMode?.title = title
                }
            }
        }catch (e: java.lang.Exception){
            e.printStackTrace()
            errdialog =  Dialog(requireContext())
            errdialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            errdialog.setContentView(R.layout.no_value_error_dialog)
            errdialog.show()
        }
    }
    override fun onResume() {
        super.onResume()
        doBindService()
        mPlaybackListener = this.PlaybackListener()
        if (mPlayerAdapter != null && mPlayerAdapter!!.isPlaying()) {
            mPlaybackListener = this.PlaybackListener()
            mPlayerAdapter!!.setPlaybackInfoListener(mPlaybackListener!!)
            restorePlayerStatus()
        }
    }
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        val inflater = mode?.menuInflater
        inflater?.inflate(R.menu.action_mode_menu, menu!!)
        toolbar.visibility= View.VISIBLE

        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        adapter.removeSelection()
        toolbar.visibility= View.VISIBLE
        actionMode = null
    }


    private fun checkReadStoragePermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.INTERNET,
                    Manifest.permission.INTERNET
                ), 1
            )
        }
    }

    private fun updatePlayingInfo(restore: Boolean, startPlay: Boolean) {
        if (startPlay) {
            mPlayerAdapter!!.getMediaPlayer()?.start()
            Handler().postDelayed({
                mMusicService!!.startForeground(
                    MusicNotificationManager.NOTIFICATION_ID,
                    mMusicNotificationManager!!.createNotification()
                )
            }, 200)
        }

        val selectedSong = mPlayerAdapter!!.getCurrentSong()
        songTitle?.text = selectedSong?.title
        val duration = mPlayerAdapter!!.getMediaPlayer()?.duration
        seekBar.max = duration!!
        if (restore) {
            seekBar.progress = mPlayerAdapter!!.getPlayerPosition()

            updatePlayingStatus()
            Handler().postDelayed({
                if (mMusicService!!.isRestoredFromPause) {
                    mMusicService!!.stopForeground(false)
                    mMusicService!!.musicNotificationManager!!.notificationManager
                        .notify(
                            MusicNotificationManager.NOTIFICATION_ID,
                            mMusicService!!.musicNotificationManager!!.notificationBuilder!!.build()
                        )
                    mMusicService!!.isRestoredFromPause = false
                }

            }, 200)
        }
    }
    override fun onClick(v: View?) {
        try{
            when (v?.id) {
                R.id.buttonPlayPause -> {
                    resumeOrPause()
                }
                R.id.buttonNext -> {

                    skipNext()
                }
                R.id.buttonPrevious -> {
                    skipPrev()
                }
            }
        }catch (e: java.lang.Exception){
            e.printStackTrace()
        }
    }
    private fun skipPrev() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.instantReset()
        }
    }
    private fun skipNext() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.skip(true)
        }
    }
    private fun resumeOrPause() {
        if (checkIsPlayer()) {
            mPlayerAdapter!!.resumeOrPause()
        } else {
            try{
                val songsStr = tracks
                val songJArray = JSONArray(songsStr)
                songs = ArrayList<Song>()
                val i:Int = songJArray.length()
                for(q in 0 until i){
                    val songObj = songJArray.getJSONObject(q)
                    val title = songObj.getString("title")
                    val id = songObj.getString("id")
                    val artist = songObj.getString("artist_name")
                    val cover = songObj.getString("cover")
                    try{
                        val url = songObj.getString("mp3_url")
                        val liric = songObj.getString("text")
                        val song = Song(
                            id = id,
                            title = title,
                            artist_name = artist,
                            cover = cover,
                            mp3_url = url,
                            internalPath = null,
                            favorit = null,
                            liric = liric
                        )
                        songs.add(song)

                    }catch (e: java.lang.Exception){
                        e.printStackTrace()
                        val liric = songObj.getString("text")
                        val song = Song(
                            id = id,
                            title = title,
                            artist_name = artist,
                            cover = cover,
                            mp3_url = "",
                            internalPath = null,
                            favorit = null,
                            liric = liric
                        )
                        songs.add(song)

                    }

                }
                val songs = songs
            try{
                if (songs.isNotEmpty()&&songs[0].mp3_url!="") {
                    onSongSelected(songs[0], songs)
                    Log.d("SONGG", songs[0].artist_name)
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
            }catch (e: java.lang.Exception){
                e.printStackTrace()
            }
        }
    }
    private fun checkIsPlayer(): Boolean {
        return mPlayerAdapter!!.isMediaPlayer()
    }
    private fun onSongSelected(song: Song, songs: List<Song>) {
        try{
            if (!seekBar.isEnabled) {
                seekBar.isEnabled = true
            }
            try {
                mPlayerAdapter!!.setCurrentSong(song, songs)
                mPlayerAdapter!!.initMediaPlayer()
                songTitle.text = song.title
                mPlaybackListener = PlaybackListener()
                mPlayerAdapter?.setPlaybackInfoListener(mPlaybackListener!!)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }catch (e: java.lang.Exception){
            e.printStackTrace()
            errdialog =  Dialog(requireContext())
            errdialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            errdialog.setContentView(R.layout.no_value_error_dialog)
            errdialog.show()
        }
    }
    private fun initializeSeekBar() {
        seekBar.setOnSeekBarChangeListener(
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


    override fun onImageClick(song: String?) {
        val n = song
    }

}