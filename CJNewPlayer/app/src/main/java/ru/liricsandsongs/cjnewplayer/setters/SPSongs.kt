package ru.liricsandsongs.cjnewplayer.setters

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class SPSongs {
    var songs = ArrayList<Song>()

    fun init(preff:String, name:String):ArrayList<Song>{
        val songsStr = preff
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
                val song = Song(
                    id = id,
                    title = title,
                    artist_name = artist,
                    cover = cover,
                    mp3_url = url,
                    internalPath = null,
                    favorit = null,
                    liric = ""
                )
                songs.add(song)
            }catch (e:Exception){
                e.printStackTrace()
            }

        }
        return songs
    }
    fun getById(id:String):Song{
        for (i in 0 until songs.size){
            if(id == songs.get(i).id){

                return songs.get(i)
            }
        }
        throw Throwable("No song in this list of songs")
    }
    fun getPosition(id: String):Int{
        for (i in 0 until songs.size){
            if(id == songs.get(i).id){
                return i
            }
        }
        throw Throwable("No song in this list of songs")
    }

    fun setTitle(id: String, value: String){
        var song = songs.get(getPosition(id))
        song.title = value
    }
    fun setArtist(id: String, value: String){
        var song = songs.get(getPosition(id))
        song.artist_name = value
    }
    fun setCover(id: String, value: String){
        var song = songs.get(getPosition(id))
        song.cover = value
    }
    fun setInternal(id: String, value: String){
        var song = songs.get(getPosition(id))
        song.internalPath = value
    }
    fun setFave(id: String, value: String){
        var song = songs.get(getPosition(id))
        song.favorit = value
    }
    fun setMp3url(id: String, value: String){
        var song = songs.get(getPosition(id))
        song.mp3_url = value
    }
    fun getProperty(id: String, property:String):String?{
        var song = songs.get(getPosition(id))
        if(property == "title"){
            return song.title
        }
        if(property == "artist_name"){
            return song.artist_name
        }
        if(property == "cover"){
            return song.cover
        }
        if(property == "mp3_url"){
            return song.mp3_url
        }
        if(property == "internalPath"){
            return song.internalPath
        }
        if(property == "favorit"){
            return song.favorit
        }
        throw Throwable("Missmatch property")
    }

    fun songToJSON(id: String):JSONObject{
        var song = songs.get(getPosition(id))
        var songObj:JSONObject = JSONObject()
        songObj.put("id", song.id)
        songObj.put("artist_name", song.artist_name)
        songObj.put("cover", song.cover)
        songObj.put("favorit", song.favorit)
        songObj.put("internalPath", song.internalPath)
        songObj.put("mp3_url", song.mp3_url)
        songObj.put("title", song.title)
        return songObj
    }
    fun update(preff:SharedPreferences, name:String, songs:ArrayList<Song>){
        preff.edit().putString(name, songs.toString()).apply()
    }
}