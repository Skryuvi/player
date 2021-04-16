package ru.liricsandsongs.cjnewplayer.interf

import android.graphics.Canvas

interface SwipeControllerActions {
    fun onLeftClicked(position: Int) {}
    fun onRightClicked(position: Int) {}

}