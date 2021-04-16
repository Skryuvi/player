package ru.liricsandsongs.cjnewplayer.adaps

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ru.liricsandsongs.cjnewplayer.R
import ru.liricsandsongs.cjnewplayer.interf.AppsLinked
import ru.liricsandsongs.cjnewplayer.setters.AppSetter

import java.util.ArrayList

class OtherAdapter(var context: Context, myDi: ArrayList<AppSetter>, val listener: AppsLinked) :
    RecyclerView.Adapter<OtherAdapter.AchieveViewHolder>() {
    var apps: ArrayList<AppSetter> = myDi
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchieveViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_other_rec, parent, false)
        return AchieveViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onBindViewHolder(holder: AchieveViewHolder, position: Int) {
        val setter = apps[position]
        val link = setter.imglink

        holder.titles.text = setter.title
        Log.d("SOME", link.toString())
        val url = "http://37.143.14.240$link"
        Picasso.get().load(url).error(R.drawable.musicpicture)
            .placeholder(R.drawable.musicpicture).into(
                holder.img
            )
        holder.itemView.setOnClickListener {
                listener.onAppClicked(setter)
        }

    }
    override fun getItemCount(): Int {
        return apps.size
    }

    inner class AchieveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val titles  = itemView.findViewById<View>(R.id.app_title) as TextView
        val img = itemView.findViewById<View>(R.id.app_image) as ImageView
    }

}