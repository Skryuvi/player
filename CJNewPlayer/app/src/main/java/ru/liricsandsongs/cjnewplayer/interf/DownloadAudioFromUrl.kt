package ru.liricsandsongs.cjnewplayer.interf


import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.os.PowerManager.WakeLock
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible

import org.json.JSONObject
import ru.liricsandsongs.cjnewplayer.R
import ru.liricsandsongs.cjnewplayer.url

import java.io.BufferedInputStream
import java.net.URL


open class DownloadAudioFromUrl(val context: Context, pro: ProgressBar, nam:String, ima: ImageView): AsyncTask<String, String, String>() {
    var progress = 0
    var p = pro
    var o = nam
    var im = ima
    var di:ProgressDialog?=null


    @SuppressLint("ShowToast")
    override fun doInBackground(vararg p0: String?): String {
        try{
            if(isOnline(context)==true){
                val go = p0[0]
                val og = JSONObject(go)
                val u = og.getString("mp3_url")
                val f = og.getString("title")
                val url  = URL(url + u)

                val connection = url.openConnection()
                connection.connect()
                connection.connect()
                val inputStream = BufferedInputStream(url.openStream())
                val filess = url.toString().length.let{url.toString().removeRange(0, 51)}
                val lengthOfFile = connection.contentLength
                val filename = "$f"
                val outputStream = context.openFileOutput("$f.mp3", Context.MODE_PRIVATE)
                val data = ByteArray(1024)
                var count = inputStream.read(data)
                var total = count
                while (count != -1) {
                    outputStream.write(data, 0, count)
                    count = inputStream.read(data)
                    publishProgress("" + ((total * 100) / lengthOfFile));
                    total += count
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()
                println("finished saving $filess to internal storage")


                return "Success"
            }else{

                return "Success failed"
            }

        }catch (e: Exception){

            return "Success failed"
        }

    }

    override fun onProgressUpdate(vararg values: String?) {
        super.onProgressUpdate(*values)
        p.progress = values[0]!!.toInt()
        progress = values[0]!!.toInt()
        if(values[0]!!.toInt()==100){
            im.setImageResource(R.drawable.ic_baseline_check_24)
            Toast.makeText(context, "$o: загрузка завершена", Toast.LENGTH_LONG).show()
        }



    }
    open fun onCreateDialog(id: Int): Dialog? {
        return when (id) {
            DIALOG_DOWNLOAD_PROGRESS -> {
                di = ProgressDialog(context)
                di?.setMessage("Downloading file..")
                di?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                di?.setCancelable(false)
                di?.show()
                di
            }
            else -> null
        }
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        progress = 100
        p.progress = 0
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
    companion object{
        const val DIALOG_DOWNLOAD_PROGRESS=0
    }
}