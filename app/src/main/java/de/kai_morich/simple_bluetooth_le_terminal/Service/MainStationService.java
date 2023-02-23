package de.kai_morich.simple_bluetooth_le_terminal.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import de.kai_morich.simple_bluetooth_le_terminal.Defines.Define;
import de.kai_morich.simple_bluetooth_le_terminal.MainActivity;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.SessionManager;
import de.kai_morich.simple_bluetooth_le_terminal.R;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.UtilityManager;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainStationService extends Service {

    // test packet
    byte[] sync = {(byte) Define.PacketSize.SyncSize.getValue(),
            (byte)Define.PacketId.Sync.getValue(),
            (byte) 10, (byte) 20, (byte) 30}; // dummy message


    byte[] disc = {(byte) Define.PacketSize.DiscSize.getValue(),
            (byte) Define.PacketId.Disc.getValue() };


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

        // 쓰레드 실행 시 실행 여부 저장
        if(!pref.getBoolean("isRunning", false))
            _serviceThread.start();
        else // 이미 실행중이라면 토스트 메시지 출력
            UtilityManager.getInstance().showToastMessage("이미 서비스가 실행중입니다. 실행중인 서비스를 종료해주세요.");

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class BackGroundTask extends Thread {
        @Override
        public void run() {

            editor.putBoolean("isRunning", true);
            editor.commit();

            while (flag) {
                try  // push 서버 형태
                {
                    // 패킷 수신 대기(blocking 상태)
                    // if(!_msManager.Receive())
                    SessionManager.CurrentSession().Send(sync);
                    Thread.sleep(1000);
                    // 추후에 더미 값이 아닌 측정값을 전달, 딜레이 시간 또한 제대로 된 알고리즘으로 수정 필요
                    // TODO
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        flag = false;
        stopForeground(true);
        stopSelf();

        Log.v("test", "service end");
    }

}