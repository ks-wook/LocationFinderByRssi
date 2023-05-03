package de.kai_morich.simple_bluetooth_le_terminal.MainStation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.spec.ECField;
import java.util.Objects;

import de.kai_morich.simple_bluetooth_le_terminal.Connection.TestConnectFragment;
import de.kai_morich.simple_bluetooth_le_terminal.MainActivity;
import de.kai_morich.simple_bluetooth_le_terminal.Managers.FragManager;
import de.kai_morich.simple_bluetooth_le_terminal.R;
import de.kai_morich.simple_bluetooth_le_terminal.Setting.SettingFragment;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;


// 싱글턴 패턴 -> 커넥터는 only one
@RequiresApi(api = Build.VERSION_CODES.O)
public class MainStationConnector
{
    public static MainActivity _mainActivity;

    Socket socket;
    String ServerIP;
    int port;

    private static MainStationConnector _instance = null;
    public static boolean isReadyToConnect = false;

    static public void makeConnector(String ip, int port) {
        _instance = new MainStationConnector(ip, port);
        isReadyToConnect = true;
    }

    static public void InitConnector(MainActivity mainActivity)
    {
        _mainActivity = mainActivity;
    }

    static public MainStationConnector GetConnector()
    {
        return _instance;
    }

    @SuppressLint("SetTextI18n")
    private MainStationConnector(String ip, int port)
    {
        this.ServerIP = ip;
        this.port = port;

        ServerIP = "http://" + ServerIP + ":" + Integer.toString(port);
        Log.v("MS", "server IP : " + ServerIP);

        SettingFragment settingFragment = FragManager.getInstance().getFragment("SettingFragment");
        ((EditText) Objects.requireNonNull(settingFragment.getActivity()).findViewById(R.id.edit_ip_address)).setText(ip);
        ((EditText)settingFragment.getActivity().findViewById(R.id.edit_port_number)).setText(Integer.toString(port));

    }

    // Connect Control
    public void Connect() {
        try {
            IO.Options opts = new IO.Options();
            opts.transports = new String[]{"websocket"}; //or Polling.NAME
            opts.forceNew = true;

            socket = IO.socket(this.ServerIP);
            
            // 이벤트 리스너 등록
            socket.on(Socket.EVENT_CONNECT, OnConnect);
            socket.on(Socket.EVENT_DISCONNECT, OnDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, OnConnectError);

            socket.on("select_result", OnSelectResult);
            socket.on("insert_result", OnInsertResult);
            // socket.on("update_result", OnUpdateResult);
            

            socket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void Disconnect() {
        try {

            socket.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Select() {
        try {
            JSONObject object = new JSONObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Insert() {
        try {

            JSONObject insertData = new JSONObject();
            insertData.put("table", "user");
            insertData.put("User_name", "테스트유저");

            socket.emit("insert", insertData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Session Event Handler
    private Emitter.Listener OnConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("MS", "Connected to server");
            
            // TEST : 연결 확인 후 테스트 리퀘스트 전송
            // SendTestMsg();
        }
    };

    private Emitter.Listener OnDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("MS", "Disconnected from server");

            _mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TestConnectFragment testConnectFragment = FragManager.getInstance().getFragment("TestConnectFragment");
                    testConnectFragment.getActivity().findViewById(R.id.test_connect_disconnected_container).setVisibility(View.VISIBLE);
                }
            });



        }
    };

    private Emitter.Listener OnConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("MS", "Connection error: " + args[0].toString());
        }
    };





    // DB Event Handler
    private Emitter.Listener OnSelectResult = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("OnSelectResult call");

            JSONObject object = (JSONObject) args[0];

        }
    };

    private Emitter.Listener OnInsertResult = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("OnSelectResult call");

            JSONObject object = (JSONObject) args[0];

        }
    };










}