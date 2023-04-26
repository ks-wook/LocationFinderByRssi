package de.kai_morich.simple_bluetooth_le_terminal.MainStation;

import android.Manifest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


// 메인 스테이션의 주소를 알아내기 위한 클래스, broadcasting 방식으로 메인스테이션 탐색
public class BroadCastThread extends Thread{

    String serverIp;
    private static final int UDP_PORT = 8200;
    private static final int TCP_PORT = 8710;

    byte[] sendBuffer = new byte[1024];
    byte[] recvBuffer = new byte[1024];

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {

        try {
            DatagramSocket broadSocket = new DatagramSocket(UDP_PORT);
            broadSocket.setBroadcast(true);

            String message = "Hello, Server!";
            sendBuffer = message.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName("255.255.255.255"), UDP_PORT);
            // UDP packet 을 네트워크내에 모든 기기에 전송(broadcast)
            broadSocket.send(sendPacket);
            broadSocket.close();

            broadSocket = new DatagramSocket(UDP_PORT);
            DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
            broadSocket.receive(recvPacket);

            Log.v("Addr", new String(recvPacket.getData(), 0, recvPacket.getLength()));
            broadSocket.close();

            // MainStation IP 획득
            serverIp = new String(recvPacket.getData(), 0, recvPacket.getLength());
            Log.v("MS", "server IP : " + serverIp);

            // 획득한 IP로 연결
            MainStationConnector.makeConnector(serverIp, TCP_PORT);


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}