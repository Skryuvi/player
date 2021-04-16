package ru.liricsandsongs.cjnewplayer.interf

import ru.liricsandsongs.cjnewplayer.setters.Song

interface Kafka {
    fun linda(pos:Int, song: Song)
}