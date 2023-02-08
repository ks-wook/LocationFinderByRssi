package de.kai_morich.simple_bluetooth_le_terminal.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import de.kai_morich.simple_bluetooth_le_terminal.MainActivity;
import de.kai_morich.simple_bluetooth_le_terminal.R;
import de.kai_morich.simple_bluetooth_le_terminal.Util.TextUtility;

public class MainStationService extends Service {

    private static final String channel = "MainStationService";

    Thread _serviceThread = new BackGroundTask();

    // 쓰레드 실행 여부 확인
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    private boolean flag = true;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundService();
    }

    void startForegroundService() {

        pref = getSharedPreferences("isRunning", MODE_PRIVATE);
        editor = pref.edit();

        // createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent mPendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    0,
                    new Intent(getApplicationContext(), MainActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            Notification notification =
                    new Notification.Builder(getApplicationContext(), "1000")
                            .setContentTitle("foreground service test")
                            .setContentText("test msg")
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentIntent(mPendingIntent)
                            .setAutoCancel(true)
                            .setOngoing(false)
                            .build();

            startForeground(1000, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 쓰레드 실행 시 실행 여부 저장
        if(!pref.getBoolean("isRunning", false))
            _serviceThread.start();
        else // 이미 실행중이라면 토스트 메시지 출력
            TextUtility.showToastMessage(getApplicationContext(), "이미 서비스가 실행중입니다. 이미 실행중인 서비스를 종료해주세요.");


        return START_REDELIVER_INTENT ;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class BackGroundTask extends Thread
    {
        @Override
        public void run() {

            editor.putBoolean("isRunning", true);
            editor.commit();

            while(flag)
            {
                try
                {
                    Thread.sleep(2000);
                    Log.v("test", "task 실행중...");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }

            editor.putBoolean("isRunning", false);
            editor.commit();

            Log.v("test", "task 종료...");
        }
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getBaseContext().getSystemService(NotificationManager.class);

            NotificationChannel serviceChannel = new NotificationChannel(
                    "1000",
                    "service",
                    NotificationManager.IMPORTANCE_NONE
            );
            manager.createNotificationChannel(serviceChannel);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        flag = false;
        stopForeground(true);
        stopSelf();

        Log.v("test", "service end");
    }

}