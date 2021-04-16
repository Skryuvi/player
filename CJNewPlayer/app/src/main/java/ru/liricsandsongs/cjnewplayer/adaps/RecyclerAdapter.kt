package ru.liricsandsongs.cjnewplayer.adaps

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.json.JSONObject
import ru.liricsandsongs.cjnewplayer.MainActivity
import ru.liricsandsongs.cjnewplayer.R
import ru.liricsandsongs.cjnewplayer.interf.DownloadAudioFromUrl
import ru.liricsandsongs.cjnewplayer.interf.OnImageClickListener
import ru.liricsandsongs.cjnewplayer.setters.Song
import java.io.File
import java.util.*


class RecyclerAdapter(

    var context: Context, listener: OnImageClickListener, mad: ArrayList<Song>, st: String
): RecyclerView.Adapter<RecyclerAdapter.AbonemRecViewHolder>() {
    private var selectionModeActive = false
    var myAbonements = mad
    var n = st
    val listenerr = listener
    var musicPlayer = MediaPlayer()
    private var onLongClick: OnLongClick? = null
    private var errdialog: Dialog? = null
    private var onSongClicked: SongClicked? = null
    private var songsSelected: SongsSelected? = null
    private var myAbonementos = mutableListOf<Song>()
    private var selectedSongs = mutableListOf<Song>()
    private var downloadManager:DownloadManager?=null
    var igil = -1
    var taped = false
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerAdapter.AbonemRecViewHolder {
        val v: View =
            LayoutInflater.from(p0.context).inflate(R.layout.track_item, p0, false)

        return AbonemRecViewHolder(v)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: RecyclerAdapter.AbonemRecViewHolder, position: Int) {

        try {

            holder.fave.setImageResource(android.R.drawable.btn_star_big_off)
            val pre = context.getSharedPreferences("songs", Context.MODE_PRIVATE)
            val prefffff = context.getSharedPreferences("idsss", Context.MODE_PRIVATE)
            val edits = prefffff.edit()
            val allEntries: Map<String, *> = prefffff?.all!!

            for ((key, value) in allEntries){
                var songsStr = prefffff.getString(key, "")
            }
                    val song = myAbonements[position]
            if(song.mp3_url==""){
                holder.fave.visibility = View.GONE
                holder.pro.isVisible = false
                holder.constLiric.isVisible = false
            }else{
                holder.constLiric.isVisible = false
                holder.fave.visibility = View.VISIBLE
            }

            holder.artist.setText(myAbonements[position].artist_name)
            holder.title.setText(myAbonements[position].title)
            if(holder.pro.progress!=0){
                holder.pro.progress = DownloadAudioFromUrl(context, holder.pro, song.title, holder.downloading).progress
            }
                    val gers = myAbonements[position].cover
                    val url = ("http://37.143.14.240$gers")

                    Picasso.get().load(url).error(R.drawable.musicpicture)
                        .placeholder(R.drawable.musicpicture).into(
                            holder.img
                        )

            try{

                    holder.fave.setOnClickListener {
                        if(isOnline(context)){
                            if(song.mp3_url!=""){
                                holder.downloading.setImageResource(R.drawable.downloading)
                                holder.downloading.visibility = View.VISIBLE
                                holder.fave.setImageResource(android.R.drawable.btn_star_big_on)
                                val editor = pre.edit()
                                val obj = JSONObject()

                                obj .put("id", song.id)
                                    .put("artist_name", song.artist_name)
                                    .put("title", song.title)
                                    .put("cover", song.cover)
                                    .put("mp3_url", song.mp3_url)
                                    .put("favorit", song.id)
                                    .put("text", song.liric.toString())


                                val jobOne = CoroutineScope(Dispatchers.IO).async(
                                    start = CoroutineStart.LAZY
                                ) {
                                    val downloadAudioFromUrl = DownloadAudioFromUrl(context, holder.pro, song.title, holder.downloading)
                                    downloadAudioFromUrl.execute(obj.toString())


                                }
                                jobOne.start()
                                editor.putString("${song.title}.mp3", obj.toString()).apply()
                                edits.putString("${song.title}.mp3", "${song.title}.mp3").apply()
                            }

                        }else{

                            errdialog =  Dialog(context)
                            errdialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
                            errdialog?.setContentView(R.layout.no_value_error_dialog)
                            errdialog?.show()
                            errdialog?.setOnCancelListener{
                                val inetnt = Intent(context, MainActivity::class.java)
                                context.startActivity(inetnt)
                            }
                        }
                    }


            }catch (e: java.lang.Exception){
                e.printStackTrace()
            }

            holder.mainItem.isSelected = selectedSongs.contains(song)

            holder.mainItem.setOnLongClickListener {

                onLongClick?.onSongLongClicked(position)
                if (!selectionModeActive) {

                    selectionModeActive = true
                }
                false
            }
            try{

                holder.mainItem.setOnClickListener {

                    notifyDataSetChanged()

                    holder.title.isSelected = true
                    if (!selectionModeActive) {
                        if(!taped){
                            taped = true
                            try{
                            if(song.mp3_url==""){
                                    igil = position
                            }else{
                                onSongClicked?.onSongClicked(song)
                            }
                        }catch (e: java.lang.Exception){
                            e.printStackTrace()
                        }

                    }else{
                             igil= -1
                            taped=false
                        }


                    } else {

                        if (selectedSongs.contains(song)) {
                            selectedSongs.remove(song)
                            songsSelected?.onSelectSongs(getSelectedSongs())

                        } else {
                            selectedSongs.add(song)
                            songsSelected?.onSelectSongs(getSelectedSongs())
                        }
                        notifyItemChanged(position)
                    }
                }
            }catch (e: java.lang.Exception){
                e.printStackTrace()
            }

            if(song.mp3_url==""){
                if(igil == position){

                        holder.constLiric.isVisible = true
                        holder.textTv.text = song.liric


                }
            }

            val list = context.filesDir?.let { getListFiles(it) }
            for(k in list!!.indices){
                val names = list.get(k).name
                if((myAbonements[position].title+".mp3")==names){
                    holder.fave.setImageResource(android.R.drawable.btn_star_big_on)

                }
            }



        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun getItemCount(): Int {

        return myAbonements.size
    }

    inner class AbonemRecViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById<View>(R.id.textViewSongTitless) as TextView
        var textTv :TextView = itemView.findViewById<View>(R.id.tvText) as TextView
        var artist: TextView = itemView.findViewById<View>(R.id.textViewArtistNamess) as TextView
        var img: ImageView = itemView.findViewById<View>(R.id.imageView) as ImageView
        var fave: ImageView = itemView.findViewById<View>(R.id.img) as ImageView
        var downloading = itemView.findViewById<View>(R.id.imageView3) as ImageView
        var constLiric : ConstraintLayout = itemView.findViewById<View>(R.id.constText) as ConstraintLayout
        var mainItem: CardView =
            itemView.findViewById<View>(R.id.mainConstraint) as CardView
        var pro = itemView.findViewById<View>(R.id.progressBar2) as ProgressBar


    }
    fun removeSelection() {
        selectionModeActive = false
        selectedSongs.clear()
        notifyDataSetChanged()
    }
    fun getSelectedSongs(): MutableList<Song> {
        return myAbonementos
    }
    protected var onItemClickListener: ((Song) -> Unit)? = null

    fun setItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnSongClicked(songClick: SongClicked) {
        try{
            this.onSongClicked = songClick
        }catch (e: Exception){
            e.printStackTrace()
        }


    }
    fun setSongsSelected(selection: SongsSelected) {
        try{
            this.songsSelected = selection
        }catch (e: java.lang.Exception){
            e.printStackTrace()
        }

    }

    interface SongsSelected {
        fun onSelectSongs(selectedSongs: MutableList<Song>)
    }

    interface SongClicked {
        fun onSongClicked(song: Song)
    }

    interface OnLongClick {
        fun onSongLongClicked(position: Int)
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
}