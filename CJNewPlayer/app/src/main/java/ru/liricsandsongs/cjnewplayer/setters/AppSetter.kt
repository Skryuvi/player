package ru.liricsandsongs.cjnewplayer.setters

import org.json.JSONObject

class AppSetter(jsonObject: JSONObject) {
    val title = jsonObject.getString("name")
    val imglink = jsonObject.getString("cover")
    val link = jsonObject.getString("url")
}