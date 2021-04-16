package ru.liricsandsongs.cjnewplayer.interf

import ru.liricsandsongs.cjnewplayer.setters.Song

interface Clickers {
    fun onCLickItem(file_path: String?)
    fun onShareClick(downloadModel: Song?)
}