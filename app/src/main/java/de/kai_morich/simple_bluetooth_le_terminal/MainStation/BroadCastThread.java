package de.kai_morich.simple_bluetooth_le_terminal.MainStation;

import android.Manifest;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import de.kai_morich.simple_bluetooth_le_terminal.Managers.MainStationManager;
import de.kai_morich.simple_bluetooth_le_terminal.Util.TextUtility;


public class BroadCastThread extends Thread{

    String serverIp;
    private static final int PORT = 8200;

    private MainStationManager msManager;
    public BroadCastThread(MainStationManager msManager) {
        this.msManager = msManager;
    }

    @Override
    public void run() {

        try {
            DatagramSocket broadSocket = new DatagramSocket();
            byte[] ping = new byte[1];
            ping[0] = (byte) 1;

            DatagramPacket dp = new DatagramPacket(ping, 1, InetAddress.getByName("255.255.255.255"), PORT);
            // UDP packet 을 네트워크내에 모든 기기에 전송(broadcast)
            broadSocket.send(dp);
            broadSocket.close();

            broadSocket = new DatagramSocket(PORT);
            dp = new DatagramPacket(ping, ping.length);
            broadSocket.receive(dp);
            broadSocket.close();

            // MainStation IP 획득
            serverIp = dp.getAddress().getHostAddress();
            Log.v("MainStation", "server IP : " + serverIp);


            msManager.connect(serverIp);
            TextUtility.showToastMessage(msManager.getApplicationContext(), "메인스테이션에 연결되었습니다");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
