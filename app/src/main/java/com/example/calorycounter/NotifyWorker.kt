package com.example.calorycounter

//import android.app.NotificationManager
//import android.content.Context
//import androidx.core.app.NotificationCompat
//import androidx.core.content.ContextCompat.getSystemService
//import androidx.work.Worker
//import androidx.work.WorkerParameters
//
//private val main: MainActivity = MainActivity()
//
//class NotifyWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
//
//    val new_context = context
//    val CHANNEL_ID = "Channel_ID_1"
//
//    override fun doWork(): Result {
//        // Method to trigger an instant notification
//        println("I get here")
//        showNotification(new_context)
//
//        return Result.success()
//        // (Returning RETRY tells WorkManager to try this task again
//        // later; FAILURE says not to try again.)
//    }
//
//    fun showNotification(context: Context) {
//        val notificationManager = getSystemService(context, NotificationManager::class.java) as NotificationManager
//
//        val notificationBuilder =
//            NotificationCompat.Builder(context, CHANNEL_ID)
//                .setSmallIcon(R.drawable.no_drinks)
//                .setContentTitle("You wanted it!")
//                .setContentText("Track those calories.")
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
//
//        notificationManager.notify(666, notificationBuilder.build())
//    }
//
//}