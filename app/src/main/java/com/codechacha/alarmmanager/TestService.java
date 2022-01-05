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

        String channelId = "default";
        String title = context.getString(R.string.app_name);

        Intent contentIntent = new Intent(context, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        PendingIntent pendingIntent = PendingIntent.getActivity(
//                context, requestCode, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // ForegroundにするためNotificationが必要、Contextを設定
        NotificationManager notificationManager =
                (NotificationManager)context.
                        getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification　Channel 設定
        NotificationChannel channel = new NotificationChannel(
                channelId, title , NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Silent Notification");
        // 通知音を消さないと毎回通知音が出てしまう
        // この辺りの設定はcleanにしてから変更
        channel.setSound(null,null);
        // 通知ランプを消す
        channel.enableLights(false);
        channel.setLightColor(Color.BLUE);
        // 通知バイブレーション無し
        channel.enableVibration(false);

        if(notificationManager != null){
            notificationManager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(context, channelId)
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
            notificationManager.notify(requestCodeValue, notification);
            //notificationManager.notify(NOTIFICATION_ID, notification);

            // startForeground
            startForeground(1, notification);
        }

        // 毎回Alarmを設定する
        setNextAlarmService(context);

        return START_NOT_STICKY;
        //return START_STICKY;
        //return START_REDELIVER_INTENT;
    }

    // 次のアラームの設定
    private void setNextAlarmService(Context context){
        Log.d(TAG, "setNextAlarmService");

//        createNotificationChannel();
//        deliverNotification(context);
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

    private void deliverNotification(Context context) {
        Intent contentIntent = new Intent(context, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_alarm)
                        .setContentTitle("Alert")
                        .setContentText("This is repeating alarm")
                        .setContentIntent(contentPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
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