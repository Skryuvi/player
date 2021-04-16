package ru.liricsandsongs.cjnewplayer.interf

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import ru.liricsandsongs.cjnewplayer.setters.FileSongs

import java.util.*

object FileSongProvider {
    private val TITLE = 0
    private val TRACK = 1
    private val YEAR = 2
    private val DURATION = 3
    private val PATH = 4
    private val ALBUM = 5
    private val ARTIST_ID = 6
    private val ARTIST = 7

    private val BASE_PROJECTION = arrayOf(
        MediaStore.Audio.AudioColumns.TITLE, // 0
        MediaStore.Audio.AudioColumns.TRACK, // 1
        MediaStore.Audio.AudioColumns.YEAR, // 2
        MediaStore.Audio.AudioColumns.DURATION, // 3
        MediaStore.Audio.AudioColumns.DATA, // 4
        MediaStore.Audio.AudioColumns.ALBUM, // 5
        MediaStore.Audio.AudioColumns.ARTIST_ID, // 6
        MediaStore.Audio.AudioColumns.ARTIST
    )



    fun getAllDeviceSongs(context: Context): MutableList<FileSongs> {
        val cursor = makeSongCursor(context)
        return getSongs(cursor)
    }


    private fun getSongs(cursor: Cursor?): MutableList<FileSongs> {
        val songs = ArrayList<FileSongs>()
        if (cursor != null && cursor.moveToFirst()) {
            do {

            } while (cursor.moveToNext())
        }

        cursor?.close()

        return songs
    }

    internal fun makeSongCursor(context: Context): Cursor? {
        try {
            val filedir ="${context?.filesDir}"
            return context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                BASE_PROJECTION, null, null, null
            )
        } catch (e: SecurityException) {
            return null
        }

    }
}