package ru.liricsandsongs.cjnewplayer

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_loading.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

import org.json.JSONArray
import org.json.JSONObject
import ru.liricsandsongs.cjnewplayer.setters.AdsSett

import java.io.File
import java.io.IOException

class LoadingActivity : AppCompatActivity() {
        private lateinit var errDialog: Dialog
    private lateinit var di:Dialog

    lateinit var tracks:JSONArray
    private var filePath:String? = null
    lateinit var banTwo:JSONObject
    lateinit var Begin: AdView
    lateinit var adsar:JSONArray
    lateinit var adView:ImageView

    lateinit var banOne:JSONObject
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

try{
    adView = imageView2
    Begin = findViewById<AdView>(R.id.adst)

    di = Dialog(this)
    di.window?.setBackgroundDrawableResource(android.R.color.transparent)
    di.setContentView(R.layout.loading_dialog)
    try{
            imageView2.visibility = View.VISIBLE

    }catch (e:Exception){

        e.printStackTrace()
    }


    CoroutineScope(Dispatchers.IO).launch {
        try{
            getAds()
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }


        getMusicList()
    }.start()
}catch (e:Exception){
    e.printStackTrace()

}

    }
    private fun addAds(){

        val adReq = AdRequest.Builder()
            .addTestDevice("BAE637B7C7EA01DD1BEEC10D3BD54439")
            .build()
        Begin.loadAd(adReq)
    }
    fun getAds(){

                val request = Request.Builder()
                    .url("http://37.143.14.240/api/v1/app-ads")
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .method("GET", null)
                    .build()
                val client = OkHttpClient()
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        e.printStackTrace()
                        di.dismiss()
                    }

                    override fun onResponse(call: okhttp3.Call?, response: okhttp3.Response?) {

                            val body = response?.body()?.string()
                            val obd = JSONObject(body!!)
                            val fulscr = obd.getJSONObject("fullscreen")

                        banOne = obd.getJSONObject("banner1")
                        banTwo = obd.getJSONObject("banner2")
                            val adset = AdsSett(fulscr)
                            val urlim = adset.img
                            Log.d("UI", urlim)
                        runOnUiThread {
                            if(urlim!=""){
                                Picasso.get().load("$url$urlim").error(R.drawable.backgr)
                                    .placeholder(R.drawable.backgr).into(
                                        adView
                                    )
                                adView.setOnClickListener {
                                    val link = adset.urlad
                                    val uri = Uri.parse(link)
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    startActivity(intent)
                                }
                            }else{
                                adView.setImageResource(R.drawable.backgr)
                            }
                        }
                    }
                })
    }
   suspend fun getMusicList(){
       delay(1000L)
       val intent = Intent(this@LoadingActivity, MainActivity::class.java)
       try{
           if(isOnline(this)){
               val preff=getSharedPreferences("ALLPREFF", MODE_PRIVATE)
               if(preff.all.count()<=0){
                   val request = Request.Builder()
                       .url("http://37.143.14.240/api/v1/tracks?project=cj")
                       .addHeader("Accept", "application/json")
                       .addHeader("Content-Type", "application/json")
                       .method("GET", null)
                       .build()
                   val client = OkHttpClient()
                   client.newCall(request).enqueue(object : okhttp3.Callback {
                       override fun onFailure(call: okhttp3.Call, e: IOException) {
                           e.printStackTrace()
                           di.dismiss()
                       }

                       override fun onResponse(call: okhttp3.Call?, response: okhttp3.Response?) {
                           try{
                               val body = response?.body()?.string()
                               Log.d("SKRA", body.toString())
                               val whatahell = JSONObject(body.toString())
                               try{
                                   val list = filesDir?.let { getListFiles(it) }
                                   for(i in 0 until list?.size!!){
                                       filePath = list.get(i).toString()
                                   }
                               }catch (e:Exception){
                                   e.printStackTrace()
                               }

                               tracks = whatahell.getJSONArray("tracks")
                               val traces : JSONArray? = JSONArray()
                               for (i in 0 until tracks.length()){
                                   var obg = tracks.getJSONObject(i)
                                   obg.put("favorit", "")
                                       .put("internalPath", ""+filePath)
                                   traces?.put(obg)
                               }
                               runOnUiThread {
                                   di.dismiss()
                               }

                               try{
                                   intent.putExtra("one", banOne.toString())
                                   intent.putExtra("two", banTwo.toString())
                               }catch (e:java.lang.Exception){
                                   e.printStackTrace()
                               }

                               intent.putExtra("tracks", tracks.toString())
                               startActivity(intent)

                           }catch (e:Exception){
                               e.printStackTrace()
                               startActivity(intent)
                           }
                       }
                   })
               }
           }else{
               val list = filesDir?.let { getListFiles(it) }
               for(i in 0 until list?.size!!){
                   filePath = list.get(i).toString()
               }

               intent.putExtra("tracks", JSONArray().put(JSONObject().put("favorit", "")
                   .put("internalPath", ""+filePath)).toString())
               startActivity(intent)
           }

       }catch (e:java.lang.Exception){
           e.printStackTrace()
           errDialog =  Dialog(this)
           errDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
           errDialog.setContentView(R.layout.no_value_error_dialog)
           errDialog.show()
           errDialog.setOnCancelListener {
               val intento = Intent(this, MainActivity::class.java)
               intento.putExtra("tracks", JSONArray().toString())
               startActivity(intento)
           }
       }

    }
    private fun getListFiles(parentDir: File): List<File>? {
        val inFiles = ArrayList<File>()
        val files = parentDir.listFiles()
        for (file in files!!) {

            if (file.isDirectory) {
                inFiles.addAll(getListFiles(file)!!)
            } else {
                if (file.name.endsWith(".mp3")) {
                    inFiles.add(file)
                }
            }
        }
        return inFiles
    }
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }
}