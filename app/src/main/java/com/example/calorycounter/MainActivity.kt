package com.example.calorycounter

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout


class MainActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: FragmentPageAdapter
    private var caloriesFile = "calLog.txt"
//    val CHANNEL_ID = "Channel_ID_1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

//        TabLayoutStuff

        tabLayout = findViewById(R.id.tab_selector)
        viewPager2 = findViewById(R.id.viewPager)

        adapter = FragmentPageAdapter(supportFragmentManager, lifecycle)

        tabLayout.addTab(tabLayout.newTab().setText("Settings"))
        tabLayout.addTab(tabLayout.newTab().setText("Home"))
        tabLayout.addTab(tabLayout.newTab().setText("Chart"))

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
    fun setCurrentTab(position: Int){

        viewPager2.currentItem = position //your viewpager object

    }
}

//        Notificationstuff

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
