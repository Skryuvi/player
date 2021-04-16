package ru.liricsandsongs.cjnewplayer.setters
data class Song (
    val id: String,
    var title: String,
    var artist_name: String,
    var cover: String,
    var mp3_url: String?,
    var internalPath: String?,
    var favorit: String?,
    var liric:String?

)