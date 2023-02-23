package de.kai_morich.simple_bluetooth_le_terminal.MainStation;

import android.os.Build;
import androidx.annotation.RequiresApi;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.LinkedList;
import java.util.Queue;

import de.kai_morich.simple_bluetooth_le_terminal.Defines.Define;

@RequiresApi(api = Build.VERSION_CODES.O)
public abstract class Session {

    public AsynchronousSocketChannel _channel;

    private ByteBuffer _recvBuffer;
    private ByteBuffer _sendBuffer;

    public abstract void OnConnected();
    public abstract void OnRecvPacket(byte[] data);
    public abstract void OnDisconnected();

    private Queue<byte[]> _sendingQueue = new LinkedList<byte[]>();

    public void OnRecv(byte[] recvBuffer)
    {

        OnRecvPacket(recvBuffer);

    }

    public void OnSend(byte[] sendBuffer)
    {
        if(sendBuffer[1] == Define.PacketId.Disc.getValue()) // 연결종료 패킷 전송 시 채널 close
        {
            Disconnect();
        }
    }

    public void Init(AsynchronousSocketChannel channel)
    {
        this._channel = channel;
        this._recvBuffer = ByteBuffer.allocate(1024);

        RegisterRecv();
    }

    // Session 통신 영역
    // receive 버퍼는 추후 고려 TODO...
    void RegisterRecv()
    {
        _recvBuffer.clear();
        _channel.read(_recvBuffer, null, RecvHandler());
    }

    // async read completed
    private CompletionHandler<Integer, Void> RecvHandler() {
        return new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {

                if(result.intValue() > 0 && result.intValue() >= _recvBuffer.array()[0]) // 파싱 가능한지 확인
                {
                    try
                    {
                        System.out.println(
                                        "Log - " + "[" + Define.LogId.Recv.getValue() + "]" +
                                        " - PacketID : " + _recvBuffer.array()[1] + " from " + _channel.getRemoteAddress());
                        OnRecv( _recvBuffer.array());
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }

                    RegisterRecv(); // 비동기 호출 재등록
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment)
            {
            }
        };
    }

    public synchronized void Send(byte[] data)
    {
        if(_sendingQueue.isEmpty()) { // 보낼 패킷들이 pending 되지 않은 경우
            _sendingQueue.add(data);
            RegisterSend();
        }
        else	  					  // 이미 비동기 작업 실행중인 경우
            _sendingQueue.add(data);
    }

    void RegisterSend()
    {
        this._sendBuffer = ByteBuffer.wrap(_sendingQueue.peek());
        _channel.write(_sendBuffer, null, SendHandler());
    }

    private CompletionHandler<Integer, Void> SendHandler() {
            return new CompletionHandler<Integer, Void>() {
                @Override
                public void completed(Integer result, Void attachment)
                {
                    try
                    {
                        System.out.println(
                                        "Log - " + "[" + Define.LogId.Send.getValue() + "]" +
                                        " - PacketID : " + _sendBuffer.array()[1] + " to " + _channel.getRemoteAddress());

                        _sendingQueue.poll();
                        OnSend(_sendBuffer.array());
                        if(!_sendingQueue.isEmpty()) // 보낼 패킷이 밀린경우 재호출
                            RegisterSend();

                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(Throwable exc, Void attachment) { }
            };
    }

    public void Disconnect()
    {
        try
        {
            _channel.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        Connector.isReadyToConnect = false;
        OnDisconnected();
    }
}
