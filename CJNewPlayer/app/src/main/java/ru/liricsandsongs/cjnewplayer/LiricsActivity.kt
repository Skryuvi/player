package ru.liricsandsongs.cjnewplayer

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.activity_lirics.*
import java.lang.Exception
import java.nio.charset.Charset

class LiricsActivity : AppCompatActivity() {
    lateinit var lirics: TextView
    lateinit var Begin: AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lirics)
        lirics = liricsTV
        Begin = findViewById<AdView>(R.id.adView2)
        val errordi = Dialog(this)
        try{
            val text = intent?.getStringExtra("text")
            lirics.text = text

        }catch (e:Exception){
            e.printStackTrace()
            errordi.window?.setBackgroundDrawableResource(android.R.color.transparent)
            errordi.setContentView(R.layout.noval)
            errordi.show()
        }
        try{
            addAds()
        }catch (e:Exception){

        }
    }
    private fun addAds(){

        val adReq = AdRequest.Builder()
            .addTestDevice("BAE637B7C7EA01DD1BEEC10D3BD54439")
            .build()

        Begin.loadAd(adReq)
    }
}