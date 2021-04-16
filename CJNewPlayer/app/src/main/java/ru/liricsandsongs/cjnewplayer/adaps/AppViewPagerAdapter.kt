package ru.liricsandsongs.cjnewplayer.adaps

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.PagerAdapter.POSITION_NONE
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.liricsandsongs.cjnewplayer.FaveFragment
import ru.liricsandsongs.cjnewplayer.HomeFragment

class AppViewPagerAdapter(supportFragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(
    supportFragmentManager,
    lifecycle
) {

    var fragments:ArrayList<Fragment> = arrayListOf(
        FaveFragment(),
        HomeFragment()
    )

    override fun getItemCount(): Int {
return fragments.size
    }

    override fun createFragment(position: Int): Fragment {

return fragments[position]

    }
    fun getItemPosition(fragments:ArrayList<Fragment>): Int {
        val f = fragments

        return POSITION_NONE
    }


}
