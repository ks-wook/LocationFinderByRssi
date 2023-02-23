package de.kai_morich.simple_bluetooth_le_terminal.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import de.kai_morich.simple_bluetooth_le_terminal.MainActivity;
import de.kai_morich.simple_bluetooth_le_terminal.MainStation.Connector;
import de.kai_morich.simple_bluetooth_le_terminal.R;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.UtilityManager;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainStationService extends Service {

    Thread _serviceThread = new BackGroundTask();
    private static boolean OnService = false;

    public static boolean isOnService() { return OnService; }

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundService();
    }

    void startForegroundService() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent mPendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    0,
                    new Intent(getApplicationContext(), MainActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            Notification notification =
                    new Notification.Builder(getApplicationContext(), "1000")
                            .setContentTitle("LocationFinder Service")
                            .setContentText("위치 자동 갱신중...")
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

        if(!OnService) {
            if(Connector.isReadyToConnect)
            {
                _serviceThread.start();

                UtilityManager.getInstance().showToastMessage(getApplicationContext(), "위치 동기화 서비스를 실행합니다.");
            }
            else {
                UtilityManager.getInstance().showToastMessage(getApplicationContext(), "메인스테이션 세션 정보가 없습니다.");
                stopForeground(true);
                stopSelf();
            }

        }
        else // 이미 실행중이라면 메시지 출력
            UtilityManager.getInstance().showToastMessage(getApplicationContext(), "이미 서비스가 실행중입니다. 실행중인 서비스를 종료해주세요.");

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class BackGroundTask extends Thread {
        @Override
        public void run() {
            OnService = true;

            Connector connector = Connector.GetConnector();
            connector.Init(); // connect

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OnService = false;
        stopForeground(true);
        stopSelf();

        Log.v("Service", "service end");
    }

}