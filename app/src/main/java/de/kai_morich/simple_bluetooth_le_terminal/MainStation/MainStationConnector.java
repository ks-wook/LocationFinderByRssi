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

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.spec.ECField;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

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
    // Test
    byte[] beaconSpaceID1 = new byte[] {(byte)60, (byte)26, (byte)47, (byte)192, (byte)223, (byte)163};
    byte[] beaconSpaceID2 = new byte[] {(byte)50, (byte)108, (byte)251, (byte)64, (byte)168, (byte)16};


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

            JSONObject allSearch = new JSONObject();
            allSearch.put("table", "beacon");
            socket.emit("select", allSearch);


            // 방 1에 있는 모든 비콘 검색
            JSONObject room1Search = new JSONObject();
            room1Search.put("table", "beacon");
            room1Search.put("ID", beaconSpaceID1);
            socket.emit("select", room1Search);

            // 등록된 모든 primary 비콘 검색
            JSONObject primarySearch = new JSONObject();
            primarySearch.put("table", "beacon");
            primarySearch.put("isPrimary", true);
            socket.emit("select", primarySearch);


            // 방1의 primary 비콘 검색
            JSONObject room1PrimarySearch = new JSONObject();
            room1PrimarySearch.put("table", "beacon");
            room1PrimarySearch.put("ID", beaconSpaceID1);
            room1PrimarySearch.put("isPrimary", true);
            socket.emit("select", room1PrimarySearch);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // DB Control
    public void Insert() {
        try {
            // Test : 비콘 테스트 데이터 삽입
            JSONObject room1beacon1 = new JSONObject();
            room1beacon1.put("table", "beacon");
            room1beacon1.put("SpaceID", beaconSpaceID1);
            room1beacon1.put("Pos_X", 1f);
            room1beacon1.put("Pos_Y", 2f);
            room1beacon1.put("Power", 100);
            room1beacon1.put("isPrimary", true);

            socket.emit("insert", room1beacon1);

            JSONObject room1beacon2 = new JSONObject();
            room1beacon2.put("table", "beacon");
            room1beacon2.put("SpaceID", beaconSpaceID1);
            room1beacon2.put("Pos_X", 3f);
            room1beacon2.put("Pos_Y", 4f);
            room1beacon2.put("Power", 100);
            room1beacon2.put("isPrimary", false);

            socket.emit("insert", room1beacon2);

            JSONObject room1beacon3 = new JSONObject();
            room1beacon3.put("table", "beacon");
            room1beacon3.put("SpaceID", beaconSpaceID1);
            room1beacon3.put("Pos_X", 1f);
            room1beacon3.put("Pos_Y", 2f);
            room1beacon3.put("Power", 100);
            room1beacon3.put("isPrimary", false);

            socket.emit("insert", room1beacon3);

            JSONObject room2beacon1 = new JSONObject();
            room2beacon1.put("table", "beacon");
            room2beacon1.put("SpaceID", beaconSpaceID2);
            room2beacon1.put("Pos_X", 1f);
            room2beacon1.put("Pos_Y", 2f);
            room2beacon1.put("Power", 100);
            room2beacon1.put("isPrimary", true);

            socket.emit("insert", room2beacon1);

            JSONObject room2beacon2 = new JSONObject();
            room2beacon2.put("table", "beacon");
            room2beacon2.put("SpaceID", beaconSpaceID2);
            room2beacon2.put("Pos_X", 3f);
            room2beacon2.put("Pos_Y", 4f);
            room2beacon2.put("Power", 100);
            room2beacon2.put("isPrimary", false);

            socket.emit("insert", room2beacon2);

            JSONObject room2beacon3 = new JSONObject();
            room2beacon3.put("table", "beacon");
            room2beacon3.put("SpaceID", beaconSpaceID2);
            room2beacon3.put("Pos_X", 1f);
            room2beacon3.put("Pos_Y", 2f);
            room2beacon3.put("Power", 100);
            room2beacon3.put("isPrimary", false);

            socket.emit("insert", room2beacon3);


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




    String[] resulsStr = {"Search all beacon", "Search room1 beacon", "Search all primary beacon", "Search room1 primary beacon"};
    int index = 0;
    // DB Event Handler
    private Emitter.Listener OnSelectResult = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("OnSelectResult call");


            try{

                JSONArray jsonArray = (JSONArray) args[0];
                // System.out.println(jsonArray.toString());

                System.out.println(resulsStr[index++]);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    System.out.println(jsonObject.toString());
                    System.out.println("");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


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