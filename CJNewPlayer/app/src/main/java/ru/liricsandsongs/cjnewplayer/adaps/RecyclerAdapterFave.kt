package ru.liricsandsongs.cjnewplayer.adaps

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
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
import ru.liricsandsongs.cjnewplayer.R
import ru.liricsandsongs.cjnewplayer.interf.OnImageClickListener
import ru.liricsandsongs.cjnewplayer.setters.FileSongs
import ru.liricsandsongs.cjnewplayer.setters.Song

import java.io.File


class RecyclerAdapterFave(

    var context: Context,
    listener: OnImageClickListener,
    mad: ArrayList<FileSongs>
): RecyclerView.Adapter<RecyclerAdapterFave.AbonemRecViewHolder>() {
    private var selectionModeActive = false
    var myAbonements = mad

    private var onLongClick: OnLongClick? = null
    private var onSongClicked: SongClicked? = null
    private var songsSelected: SongsSelected? = null
    private var myAbonementosa = mutableListOf<Song>()
    private var myAbonementos = mutableListOf<FileSongs>()
    private var selectedSongs = mutableListOf<FileSongs>()
    private var cardList:ArrayList<ConstraintLayout>? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerAdapterFave.AbonemRecViewHolder {
        val v: View =
            LayoutInflater.from(p0.context).inflate(R.layout.track_item, p0, false)

        return AbonemRecViewHolder(v)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerAdapterFave.AbonemRecViewHolder, position: Int) {
        val cardList:ArrayList<ConstraintLayout>? = ArrayList<ConstraintLayout>()
        try {
            val list = context.filesDir?.let { getListFiles(it) }
            val song = myAbonements[position]
            holder.artist.setText(myAbonements[position].artistName)
            holder.title.setText(myAbonements[position].title)
            holder.pro.isVisible=false
            holder.fave.setImageResource(android.R.drawable.btn_star_big_on)
            val pre = context.getSharedPreferences("songs", Context.MODE_PRIVATE)
            val prefffff = context.getSharedPreferences("idsss", Context.MODE_PRIVATE)
            holder.constLiric.isVisible = false
            if (cardList?.contains(holder.constr)!!) {
                cardList.add(holder.constr);
            }

            holder.fave.setOnClickListener {
                holder.fave.setImageResource(android.R.drawable.btn_star_big_off)
                pre.edit().remove(song.title).apply()
                prefffff.edit().remove(song.title).apply()
                val filepath = list?.get(position)
                filepath?.delete()
                myAbonements.remove(song)
                notifyDataSetChanged()
            }
            holder.mainItem.isSelected = selectedSongs.contains(song)
            holder.mainItem.setOnLongClickListener {
                onLongClick?.onSongLongClicked(position)

                holder.mainItem.resources.getColor(R.color.browser_actions_bg_grey)

                if (!selectionModeActive) {
                    selectionModeActive = true
                }
                false
            }
            holder.img.setImageResource(R.drawable.icon)
            holder.mainItem.setOnClickListener {


                if (!selectionModeActive) {

                    onSongClicked?.onSongClicked(song)

                    holder.title.isSelected = true

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

        } catch (e: Exception) {

            e.printStackTrace()
        }

    }

    override fun getItemCount(): Int {

        return myAbonements.size
    }

    inner class AbonemRecViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var title: TextView = itemView.findViewById<View>(R.id.textViewSongTitless) as TextView
        var artist: TextView = itemView.findViewById<View>(R.id.textViewArtistNamess) as TextView
        var img: ImageView = itemView.findViewById<View>(R.id.imageView) as ImageView
        var fave: ImageView = itemView.findViewById<View>(R.id.img) as ImageView
        var mainItem: CardView =
            itemView.findViewById<View>(R.id.mainConstraint) as CardView
         var constr: ConstraintLayout = itemView.findViewById<View>(R.id.cons) as ConstraintLayout
        var pro = itemView.findViewById<View>(R.id.progressBar2) as ProgressBar
        var constLiric : ConstraintLayout = itemView.findViewById<View>(R.id.constText) as ConstraintLayout

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
    fun removeSelection() {
        selectionModeActive = false
        selectedSongs.clear()
        notifyDataSetChanged()
    }
    fun getSelectedSongs(): MutableList<FileSongs> {
        return myAbonementos
    }
    protected var onItemClickListener: ((FileSongs) -> Unit)? = null

    fun setItemClickListener(listener: (FileSongs) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnSongClicked(songClick: SongClicked) {
        this.onSongClicked = songClick
    }


    fun setSongsSelected(selection: SongsSelected) {
        this.songsSelected = selection
    }

    interface SongsSelected {
        fun onSelectSongs(selectedSongs: MutableList<FileSongs>)
    }

    interface SongClicked {
        fun onSongClicked(song: FileSongs)
    }

    interface OnLongClick {
        fun onSongLongClicked(position: Int)
    }


}