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
import org.json.JSONException;
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
    byte[] beaconSpaceID1 = new byte[] {(byte)60, (byte)165, (byte)109, (byte)132, (byte)63, (byte)183};
    byte[] beaconSpaceID2 = new byte[] {(byte)39, (byte)8, (byte)1, (byte)15, (byte)91, (byte)33};

    String[] resulsStr = {"Search all beacon", "Search room1 beacon", "Search all primary beacon", "Search room1 primary beacon", "Target ID Search"};
    int index = 0;

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

            // DB 이벤트 등록
            socket.on("select_result", OnSelectResult);
            socket.on("insert_result", OnInsertResult);
            socket.on("update_result", OnUpdateResult);

            // 스캔 이벤트 등록
            socket.on("scan_req", OnScanRequest);

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



    // DB Control
    public void Select() {
        try {

            JSONObject allSearch = new JSONObject();
            allSearch.put("table", "Beacon");
            socket.emit("select", allSearch);


            // 방 1에 있는 모든 비콘 검색
            JSONObject room1Search = new JSONObject();
            room1Search.put("table", "Beacon");
            room1Search.put("ID", beaconSpaceID1);
            socket.emit("select", room1Search);

            // 등록된 모든 primary 비콘 검색
            JSONObject primarySearch = new JSONObject();
            primarySearch.put("table", "Beacon");
            primarySearch.put("isPrimary", true);
            socket.emit("select", primarySearch);


            // 방1의 primary 비콘 검색
            JSONObject room1PrimarySearch = new JSONObject();
            room1PrimarySearch.put("table", "Beacon");
            room1PrimarySearch.put("ID", beaconSpaceID1);
            room1PrimarySearch.put("isPrimary", true);
            socket.emit("select", room1PrimarySearch);

            JSONObject targetSearch = new JSONObject();
            targetSearch.put("table", "Space");
            targetSearch.put("ID", beaconSpaceID1);
            targetSearch.put("setTarget", 1);
            socket.emit("select", targetSearch);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Insert() {
        try {
            /*// Test : 비콘 테스트 데이터 삽입
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

            socket.emit("insert", room2beacon3);*/



            JSONObject room1 = new JSONObject();
            room1.put("Familiar_name", "방1");
            room1.put("table", "space");
            room1.put("Size_X", 2f);
            room1.put("Size_Y", 3f);

            socket.emit("insert", room1);

            JSONObject room2 = new JSONObject();
            room2.put("Familiar_name", "방2");
            room2.put("table", "space");
            room2.put("Size_X", 3f);
            room2.put("Size_Y", 4f);

            socket.emit("insert", room2);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Update() {
        try {

            /*JSONObject allSearch = new JSONObject();
            allSearch.put("table", "Beacon");
            allSearch.put("column", "State");
            allSearch.put("newValue", new byte[] {0, 0});
            socket.emit("update", allSearch);


            // 방 1에 있는 모든 비콘 검색
            JSONObject room1Search = new JSONObject();
            room1Search.put("table", "Beacon");
            room1Search.put("column", "State");
            room1Search.put("ID", beaconSpaceID1);
            room1Search.put("newValue", new byte[] {0, 0});
            socket.emit("update", room1Search);*/

            // 등록된 모든 primary 비콘 검색
            JSONObject primarySearch = new JSONObject();
            primarySearch.put("table", "Beacon");
            primarySearch.put("column", "State");
            primarySearch.put("isPrimary", true);
            primarySearch.put("ID", beaconSpaceID1);
            primarySearch.put("newValue", new byte[] {0, 1});
            socket.emit("update", primarySearch);

            /*// 방1의 primary 비콘 검색
            JSONObject room1PrimarySearch = new JSONObject();
            room1PrimarySearch.put("table", "Beacon");
            room1PrimarySearch.put("column", "State");
            room1PrimarySearch.put("ID", beaconSpaceID1);
            room1PrimarySearch.put("isPrimary", true);
            room1PrimarySearch.put("newValue", new byte[] {0, 1});
            socket.emit("update", room1PrimarySearch);*/

            /*// 타겟으로 하나의 ID를 정하여 검색
            JSONObject targetSearch = new JSONObject();
            targetSearch.put("table", "Space");
            targetSearch.put("column", "Familiar_name");
            targetSearch.put("ID", beaconSpaceID1);
            targetSearch.put("newValue", "내방");
            targetSearch.put("setTarget", 1);
            socket.emit("update", targetSearch);
*/

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

    private Emitter.Listener OnUpdateResult = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("OnUpdateResult call");

            try {
                JSONObject object = (JSONObject) args[0];

                int Ok = object.getInt("updateOk");
                if(Ok != 0)
                    System.out.println("update success");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    // Scan Event Handler
    private Emitter.Listener OnScanRequest = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            try {
                // TODO : 블루투스 스캔하여 스캔 값 획득


                // TEST : 테스트용 값 전송
                int[][] rssi_raw_data = new int[][] {{-53, -54, -54, -55, -55, -55, -56, -56, -80, -57},
                        {-53, -54, -54, -55, -55, -55, -56, -56, -80, -57},
                        {-53, -54, -54, -55, -55, -55, -56, -56, -80, -57}};

                JSONArray jsonArray = new JSONArray();
                for (int[] innerArray : rssi_raw_data) {
                    JSONArray innerJsonArray = new JSONArray();
                    for (int value : innerArray) {
                        innerJsonArray.put(value);
                    }
                    jsonArray.put(innerJsonArray);
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("rssi_raw_data", jsonArray);

                socket.emit("scan_res", jsonObject);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };









}