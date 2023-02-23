package de.kai_morich.simple_bluetooth_le_terminal.MainStation;

import android.os.Build;
import androidx.annotation.RequiresApi;
import de.kai_morich.simple_bluetooth_le_terminal.Defines.Define;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.SessionManager;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ServerSession extends Session {

    @Override
    public void OnConnected() {

        try
        {
            System.out.println(
                    "Log - " + "[" + Define.PacketId.Conc.getValue() + "]" +
                            " - " + _channel.getRemoteAddress() + " connected");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void OnRecvPacket(byte[] data) {
        try
        {
            int packetId = data[1];

            if(packetId == Define.PacketId.Sync.getValue()) // 위치 동기와 요청 패킷
            {
                SessionManager.CurrentSession().Send(Packet.sync);
            }
            else if(packetId == Define.PacketId.Disc.getValue()) // 연결 종료 요청 패킷
            {
                SessionManager.CurrentSession().Disconnect();
            }

        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }

    }

    @Override
    public void OnDisconnected() {
        // TODO Auto-generated method stub

    }

}
