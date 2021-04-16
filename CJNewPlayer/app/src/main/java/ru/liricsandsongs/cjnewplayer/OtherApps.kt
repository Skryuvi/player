package ru.liricsandsongs.cjnewplayer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_other_apps.*
import okhttp3.*
import org.json.JSONArray
import ru.liricsandsongs.cjnewplayer.adaps.OtherAdapter
import ru.liricsandsongs.cjnewplayer.interf.AppsLinked
import ru.liricsandsongs.cjnewplayer.setters.AppSetter

import java.io.IOException
import java.lang.Exception
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class OtherApps:AppCompatActivity(), AppsLinked {
    lateinit var recyclerView: RecyclerView
    lateinit var list: ArrayList<AppSetter>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_apps)
        recyclerView = recapps
        try{
            getApps()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    private fun getApps(){
        val client = getUnsafeOkHttpClient()
        val request = Request.Builder()
            .url("http://37.143.14.240/api/v1/app-links")
            .addHeader("Accept", "application/json")
            .build()
        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val resp = response.body()?.string()
                list = ArrayList<AppSetter>()
                val str = resp.toString()
                try{
                    val array = JSONArray(str)
                    for(q in 0 until array.length())
                    {
                        val obj = array.getJSONObject(q)
                        val appsetter = AppSetter(obj)
                        list.add(appsetter)
                    }
                    runOnUiThread{
                        recyclerView.layoutManager = LinearLayoutManager(this@OtherApps)
                        recyclerView.adapter = OtherAdapter(this@OtherApps, list, this@OtherApps)
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        })
    }
    override fun onAppClicked(appSetter: AppSetter) {
        val link = appSetter.link
        val uri = Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }
            override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }.build()
    }

}