package com.example.ActivityRecognition

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.ActivityRecognition.history.FragmentHISTORY
import com.example.ActivityRecognition.settings.FragmentSETTINGS
import com.example.ActivityRecognition.start.FragmentSTART
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.*


class MainActivity : AppCompatActivity() {

    // create three fragments and one tab layout
    private lateinit var fragmentSTART: FragmentSTART
    private lateinit var fragmentHISTORY: FragmentHISTORY
    private lateinit var fragmentSETTINGS: FragmentSETTINGS
    private lateinit var fragments: ArrayList<Fragment>
    private lateinit var tabLayout: TabLayout
    private var tabTitles = arrayOf("START", "HISTORY", "SETTINGS")

    private lateinit var viewPager2: ViewPager2
    private lateinit var myFragmentStateAdapter: MyFragmentStateAdapter
    private lateinit var tabConfigurationStrategy: TabLayoutMediator.TabConfigurationStrategy
    private lateinit var tabLayoutMediator: TabLayoutMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "MyRuns5-ActivityRecognition"

        fragmentSTART = FragmentSTART()
        fragmentHISTORY = FragmentHISTORY()
        fragmentSETTINGS = FragmentSETTINGS()
        fragments = ArrayList()
        fragments.add(fragmentSTART)
        fragments.add(fragmentHISTORY)
        fragments.add(fragmentSETTINGS)

        tabLayout = findViewById(R.id.tab)
        viewPager2 = findViewById(R.id.viewpager2)
        myFragmentStateAdapter = MyFragmentStateAdapter(this, fragments)
        viewPager2.adapter = myFragmentStateAdapter

        tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy(){
            // assign name to tab layout
            tab: TabLayout.Tab,
            position: Int -> tab.text = tabTitles[position]
        }

        // this one is like a bridge, connector
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy)
        tabLayoutMediator.attach()
    }

    // when app closes
    override fun onDestroy() {
        super.onDestroy()
        tabLayoutMediator.detach()
    }
}