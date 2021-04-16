package ru.liricsandsongs.cjnewplayer.interf

import ru.liricsandsongs.cjnewplayer.setters.AppSetter

interface AppsLinked {
    fun onAppClicked(appSetter: AppSetter)
}