package ru.liricsandsongs.cjnewplayer.setters

import org.json.JSONObject

class AdsSett(jsonObject: JSONObject) {
    val img = jsonObject.getString("img")
    val urlad = jsonObject.getString("url")
}