package de.kai_morich.simple_bluetooth_le_terminal.Managers;

import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import de.kai_morich.simple_bluetooth_le_terminal.Defines.Define;
import de.kai_morich.simple_bluetooth_le_terminal.MainActivity;
import de.kai_morich.simple_bluetooth_le_terminal.Util.TextUtility;


public class MainStationManager extends AppCompatActivity{

    private MainStationManager() { }

    static MainStationManager _msManager = new MainStationManager();

    public static MainStationManager getInstance() {
        return _msManager;
    }

    Socket socket;
    ObjectOutputStream _oos;

    private int port = 7777; // Mainstation port


    public boolean connect(String IP) {
        try {

            socket = new Socket(IP, port);
            _oos = new ObjectOutputStream(socket.getOutputStream());

            Log.e("MT","connected");


        }
        catch (Exception e)
        {
            Log.e("error", e.getMessage());
        }


        return true;
    }

    // 패킷에 대한 내용을 파라미터로 받아서 구현 이하 TODO
    public void send() {
        if(socket == null)
            TextUtility.showToastMessage(getApplicationContext(), "메인 스테이션에 연결되지 않았습니다.");

        try {

            byte[] data = { (byte) 5, // PacketSize
                    (byte) 0, // Sync
                    (byte) 10, (byte) 20, (byte) 30}; // dummy message

            Thread.sleep(1000);

            _oos.writeObject(data);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ----------------------------------------------------
    // 통신 종료 패킷을 Send 에서 전송하게끔 구현 후 이하의 함수 폐기
    public void disConnect()  {
        try {
            byte[] disc = {(byte) 2, // packet size
                           (byte) 2 }; // disconnect

            _oos.writeObject(disc);

            Log.e("MT","Disconnect");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // 소켓 닫기 (연결 끊기)
        try
        {
            if(socket != null) { socket.close(); }
            if(_oos != null) { _oos.close(); }

            System.out.println("Disconnected from Server");
        }

        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }
    //----------------------------------------------------------

}
