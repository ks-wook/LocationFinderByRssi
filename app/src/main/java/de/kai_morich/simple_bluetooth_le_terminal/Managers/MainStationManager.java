package de.kai_morich.simple_bluetooth_le_terminal.Managers;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import de.kai_morich.simple_bluetooth_le_terminal.Util.TextUtil;
import de.kai_morich.simple_bluetooth_le_terminal.Util.TextUtility;


public class MainStationManager extends AppCompatActivity{

    Socket _socket;
    ObjectOutputStream _oos;
    ObjectInputStream _ois;

    private MainStationManager() { }

    static MainStationManager _instance = new MainStationManager();

    public static MainStationManager getInstance() { return _instance; }

    private static final int ServerPort = 7777; // Mainstation port


    public boolean connect(String IP) {

        try {

            // 소켓 생성 밑 스트림 초기화
            _socket = new Socket(IP, ServerPort);
            _oos = new ObjectOutputStream(_socket.getOutputStream());
            _ois = new ObjectInputStream(_socket.getInputStream());

            Log.e("MainStation","connected");
            TextUtility.showToastMessage(getApplicationContext(), "메인스테이션에 연결되었습니다.");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return true;
    }

    // 패킷에 대한 내용을 파라미터로 받아서 구현 이하 TODO
    public void Send(final byte packetSize, byte packetId, byte[] packet)
    { // 반드시 정해진 프로토콜을 따라서 인자를 전달해야함

        if(_socket == null)
        {
            TextUtility.showToastMessage(getApplicationContext(), "메인 스테이션에 연결되지 않았습니다.");
            return;
        }

        try
        {

            byte[] data = new byte[packetSize]; // 직렬화를 위해 바이트배열만을 이용
            data[0] = packetSize;
            data[1] = packetId;

            if(packet != null) // 전달할 패킷 내용이 있는 경우
            {
                for(int i = 0; i < packet.length; i++)
                {
                    data[i + 2] = packet[i];
                }
            }

            // Thread.sleep(1000);
            _oos.writeObject(data); // read write 함수는 기본적으로 blocking 방식

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public boolean Receive()
    {
        try
        {
            byte[] recvData = (byte[]) _ois.readObject(); // blocking
            Log.v("test", "메세지 수신");


            if(recvData[1] == 0) // 위치 요청 패킷인경우
            {
                // dummy value 10, 20, 30 -> 추후에 연산된 값을 가져와서 직렬화
                byte[] sendData = new byte[] {(byte) 10, (byte) 20, (byte) 30 };
                Send((byte) 5, (byte) 0, sendData); // 위치 정보 전송
            }
            else if(recvData[1] == 2) // 연결 종료 패킷인 경우
            {
                // 종료 패킷은 무조건 클라이언트에서 먼저 보낸 후
                // 서버에서 응답하는 방식
                // 종료 패킷은 서비스 종료 파트에서 보내는 것이 우선 됨

                if(_ois != null)
                {
                    _ois.close();
                    _ois = null;
                }
                if(_oos != null)
                {
                    _oos.close();
                    _oos = null;
                }
                if(_socket != null)
                {
                    _socket.close();
                    _socket = null;
                }

                TextUtility.showToastMessage(getApplicationContext(), "메인스테이션과의 연결이 종료되었습니다.");
                return false;
            }


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return true;

    }
}
