package ru.liricsandsongs.cjnewplayer

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_control.*
import org.json.JSONArray
import ru.liricsandsongs.cjnewplayer.OtherApps
import ru.liricsandsongs.cjnewplayer.adaps.AppViewPagerAdapter
import ru.liricsandsongs.cjnewplayer.setters.Song

class MainActivity : AppCompatActivity() {
    lateinit var viewpager: ViewPager2
    var addresses = arrayOf("cheaterandcheese@gmail.com")
    var subject = "Обратная связь"
    var poin = -1
    lateinit var adapter: AppViewPagerAdapter
    lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_control)
        initViewPagerWithFragm()
        toolbar = findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.actions)
        val menu = toolbar.menu
        val itemm = menu.findItem(R.id.getsome)
        val anotheroneItem = menu.findItem(R.id.menuWithIconText)
        anotheroneItem.isVisible = false
        var intent = intent.getStringExtra("tracks")
        val songJArray = JSONArray(intent)
        var  songs = ArrayList<Song>()
        val i:Int = songJArray.length()
        for(q in 0 until i){
            val songObj = songJArray.getJSONObject(q)
            val title = songObj.getString("title")
            val id = songObj.getString("id")
            val artist = songObj.getString("artist_name")
            val cover = songObj.getString("cover")
            try{
                val url = songObj.getString("mp3_url")

                val liric = songObj.getString("text")
                val song = Song(
                        id = id,
                        title = title,
                        artist_name = artist,
                        cover = cover,
                        mp3_url = url,
                        internalPath = null,
                        favorit = null,
                        liric = liric
                )
                songs.add(song)
            }catch (e: java.lang.Exception){
                e.printStackTrace()

                val liric = songObj.getString("text")
                val song = Song(
                        id = id,
                        title = title,
                        artist_name = artist,
                        cover = cover,
                        mp3_url = "",
                        internalPath = null,
                        favorit = null,
                        liric = liric
                )
                songs.add(song)
            }
        }
        if(songs[0].mp3_url==""){

                itemm.isVisible = false

        }
        toolbar.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                val id = item?.itemId
                if (id == R.id.menuWithIconTex) {
                    val openOther = Intent(this@MainActivity, OtherApps::class.java)
                    startActivity(openOther)
                    return true
                } else if (id == R.id.menuWithIconTe) {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:")
                    intent.putExtra(Intent.EXTRA_EMAIL, addresses)
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    }
                }else if(id == R.id.getsome){
                    val b = AlertDialog.Builder(this@MainActivity)
                            .setTitle("Тексты")
                            .setMessage("Для просмотра текста вы можете сделать свайп влево по выбранному треку!")
                            .setPositiveButton("Oк")
                            { dialog, _ -> dialog?.dismiss() }

                    val d = b.create()
                    d.show()
                    return true
                }
                return false
            }
        })
    }
    private fun initViewPagerWithFragm() {
        val pref = getSharedPreferences("songs", MODE_PRIVATE)

        val viewPager2: ViewPager2 = viewPager

         adapter = AppViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager2.adapter = adapter
        if (pref.all!!.count() ==0){
            viewPager2.currentItem = 1
        }else{
            viewPager2.currentItem = 0
        }
        poin = viewPager2.currentItem

        adapter.getItemPosition(adapter.fragments)

        val tablayout: TabLayout = tabLayout
        val names:ArrayList<String> = arrayListOf("Моя музыка", "Вся музыка")
        TabLayoutMediator(tablayout, viewPager2){ tab, position ->
            tab.text = names[position]

        }.attach()
        try{

        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    @SuppressLint("ResourceType")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actions, menu)
        return true
    }
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
            adapter.notifyDataSetChanged()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menuWithIconTex) {
            val openOther = Intent(this, OtherApps::class.java)
            startActivity(openOther)
            return true
        }else if(id == R.id.menuWithIconTe){
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, addresses)
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}