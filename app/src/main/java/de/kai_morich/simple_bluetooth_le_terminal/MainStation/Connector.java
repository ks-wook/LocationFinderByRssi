package de.kai_morich.simple_bluetooth_le_terminal.MainStation;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;

import de.kai_morich.simple_bluetooth_le_terminal.Defines.Define;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.SessionManager;

// 싱글턴 패턴 -> 커넥터는 only one
@RequiresApi(api = Build.VERSION_CODES.O)
public class Connector
{

    AsynchronousChannelGroup _channelGroup;
    AsynchronousSocketChannel _iocpSocket;

    String ServerIP;
    int port;

    // test packet
    byte[] sync = {(byte) Define.PacketSize.SyncSize.getValue(),
            (byte)Define.PacketId.Sync.getValue(),
            (byte) 10, (byte) 20, (byte) 30}; // dummy message


    byte[] disc = {(byte) Define.PacketSize.DiscSize.getValue(),
            (byte) Define.PacketId.Disc.getValue() };


    private static Connector _instance;

    static public Connector makeConnector(String ip, int port)
    {
        if(_instance == null)
            _instance = new Connector(ip, port);

        return _instance;
    }

    private Connector(String ip, int port)
    {
        this.ServerIP = ip;
        this.port = port;
    }

    // dummy value
    int x = 10;
    int y = 20;
    int z = 30;

    public void Init()
    {

        try
        {
            _channelGroup = AsynchronousChannelGroup.withFixedThreadPool(
                    Runtime.getRuntime().availableProcessors(),
                    Executors.defaultThreadFactory());
            _iocpSocket = AsynchronousSocketChannel.open(_channelGroup);


        }
        catch (IOException e)
        {
            e.printStackTrace();
        }



        // async connect
        RegisterConnect();



    }

    public void RegisterConnect()
    {
        Log.v("test", "RegisterConnect call");
        _iocpSocket.connect(new InetSocketAddress("192.168.0.15", 5001), null, ConnectCompletionHandler());
    }

    private CompletionHandler<Void, Void> ConnectCompletionHandler()
    {
        return new CompletionHandler<Void, Void>() {
            @Override
            public void completed(Void result, Void attachment) {
                // TODO Auto-generated method stub

                try
                {
                    ServerSession session = new ServerSession();
                    session.Init(_iocpSocket);
                    SessionManager.registerSession(session);

                    session.OnConnected();

                    // test
                    session.Send(sync);
                    session.Send(sync);

                    Thread.sleep(1000);
                    // session.Send(disc);

                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            @Override
            public void failed(Throwable exc, Void attachment)
            {
                exc.printStackTrace();
                System.out.println("connect failed");
            }
        };
    }
}