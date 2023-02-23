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

    private static Connector _instance;

    public static boolean isReadyToConnect = false;

    static public void makeConnector(String ip, int port)
    {
        _instance = new Connector(ip, port);
        isReadyToConnect = true;
    }

    static public Connector GetConnector()
    {
        return _instance;
    }

    private Connector(String ip, int port)
    {
        this.ServerIP = ip;
        this.port = port;
    }

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
        _iocpSocket.connect(new InetSocketAddress(ServerIP, port), null, ConnectCompletionHandler());
    }

    private CompletionHandler<Void, Void> ConnectCompletionHandler()
    {
        return new CompletionHandler<Void, Void>() {
            @Override
            public void completed(Void result, Void attachment) {

                try
                {
                    ServerSession session = new ServerSession();
                    session.Init(_iocpSocket);
                    SessionManager.registerSession(session);

                    session.OnConnected();

                }
                catch (Exception e)
                {
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