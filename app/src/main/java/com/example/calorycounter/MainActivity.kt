package com.example.calorycounter

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.util.AttributeSet
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.calorycounter.helpers.FragmentPageAdapter
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.helpers.Keys
import com.example.calorycounter.helpers.appLanguageFile
import com.google.android.material.tabs.TabLayout


class MainActivity : AppCompatActivity() {
    private val dataHandler = DataHandler()
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: FragmentPageAdapter
    //    val CHANNEL_ID = "Channel_ID_1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Set language on creation
        val countryCode = dataHandler.loadData(this, appLanguageFile)[Keys.Language.toString()].toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(countryCode)
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(
                    countryCode
                )
            )
        }

//        TabLayoutStuff

        tabLayout = findViewById(R.id.tab_selector)
        viewPager2 = findViewById(R.id.viewPager)

        adapter = FragmentPageAdapter(supportFragmentManager, lifecycle)

        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.baseline_settings_24))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.baseline_home_24))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.baseline_insert_chart_outlined_24))

        viewPager2.adapter = adapter
        viewPager2.currentItem = 1
        tabLayout.selectTab(tabLayout.getTabAt(1))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewPager2.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }

        })

        //ToDo get data from db and send it to other class check this again later
//        val chartDataCalories = dataHandler.loadData(this, caloriesFile)
//        val serMap = HashMap(chartDataCalories)
////        val c = Chart::class
//        val chartActivityIntent = Intent(this, Chart::class.java)
//        chartActivityIntent.putExtra("caloriesData", serMap)
//        startActivity(chartActivityIntent)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {

        return super.onCreateView(name, context, attrs)
    }
}

//        Notification_stuff

//        var hasNotificationPermission = false
//
//        val channel = NotificationChannel(
//            CHANNEL_ID,
//            "High priority notifications",
//            NotificationManager.IMPORTANCE_HIGH
//        )
//
//        val requestPermission =  registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//                hasNotificationPermission = true
//            }
//
//
//        if (!hasNotificationPermission) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
//            }
//        }
//
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.createNotificationChannel(channel)
//
//        ReminderManager.startReminder(this)


//    private fun workerStuffForMaybeUse(){
//        //        val inputData: Data = Data.Builder().putInt("DBEventIDTag", 10).build()
//
////        val notificationWork: OneTimeWorkRequest = OneTimeWorkRequest.Builder(NotifyWorker::class.java)
////            .setInitialDelay(15, TimeUnit.SECONDS)
////            .setInputData(inputData)
////            .addTag(workTag)
////            .build()
//
////        WorkManager.getInstance(this).enqueue(notificationWork);
//
//        val work = PeriodicWorkRequestBuilder<NotifyWorker>(15, TimeUnit.SECONDS).build()
//
//        WorkManager.getInstance(this).enqueue(work)
//    }
