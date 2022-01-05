package com.codechacha.alarmmanager;

import static com.codechacha.alarmmanager.AlarmReceiver.NOTIFICATION_ID;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class TestService extends Service  {
    private String TAG = this.getClass().getSimpleName();

    private Context context;
    private int requestCodeValue = 0;
    private String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        context = getApplicationContext();
        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        // 内部ストレージにログを保存
        InternalFileReadWrite fileReadWrite = new InternalFileReadWrite(context);
        fileReadWrite.writeFile();

        int requestCode = intent.getIntExtra("REQUEST_CODE",requestCodeValue);
        Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.putExtra("Alarm", true);
        //contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                //NOTIFICATION_ID,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        createNotificationChannel();
        String title = context.getString(R.string.app_name);
        Notification notification = new Notification.Builder(context, PRIMARY_CHANNEL_ID)
                .setContentTitle(title)
                // android標準アイコンから
                .setSmallIcon(android.R.drawable.btn_star)
                .setContentText("Alarm Counter TestService1")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
//                    .setContentIntent(contentPendingIntent)
                .setWhen(System.currentTimeMillis())
                .build();

        //notificationManager.notify(requestCode, notification);
//            notificationManager.notify(requestCodeValue, notification);
        //notificationManager.notify(NOTIFICATION_ID, notification);

        // startForeground
        startForeground(1, notification);

        // 毎回Alarmを設定する
        setNextAlarmService(context);

        return START_NOT_STICKY;
        //return START_STICKY;
        //return START_REDELIVER_INTENT;
    }

    // 次のアラームの設定
    private void setNextAlarmService(Context context){
        Log.d(TAG, "setNextAlarmService");

        long repeatPeriod = 1*60*1000;

        Intent intent = new Intent(context, TestService.class);

        long startMillis = System.currentTimeMillis() + repeatPeriod;

        PendingIntent pendingIntent
                = PendingIntent.getService(context, requestCodeValue, intent, 0);
        AlarmManager alarmManager
                = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(alarmManager != null){
            // Android Oreo 以上を想定
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    startMillis, pendingIntent);
        }
    }

    private void stopAlarmService(){
        Intent indent = new Intent(context, TestService.class);
        PendingIntent pendingIntent  = PendingIntent.getService(context, 0, indent, 0);

        // アラームを解除する
        AlarmManager alarmManager
                = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager != null){
            alarmManager.cancel(pendingIntent);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        stopAlarmService();
        // Service終了
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    PRIMARY_CHANNEL_ID,
                    "Stand up notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("AlarmManager Tests");
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

}