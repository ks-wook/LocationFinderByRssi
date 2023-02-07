package de.kai_morich.simple_bluetooth_le_terminal.Managers;

import java.util.ArrayList;
import java.util.Collections;

import de.kai_morich.simple_bluetooth_le_terminal.Wifi.Room;

public class WiFiTestResultManager {

    static public void sortRoomList(ArrayList<Room> roomList, int rssi1, int rssi2, int rssi3) {
        for(int i = 0; i < roomList.size(); i++) {

            Room room = roomList.get(i);
            int min1 = room.getAp1Min();
            int max1 = room.getAp1Max();
            int min2 = room.getAp2Min();
            int max2 = room.getAp2Max();
            int min3 = room.getAp3Min();
            int max3 = room.getAp3Max();

            // error1
            if(rssi1 >= min1 && rssi1 <= max1)
                room.error1 = 0;
            else if (rssi1 < min1)
                room.error1 = min1 - rssi1;
            else
                room.error1 = rssi1 - max1;

            // error2
            if(rssi2 >= min2 && rssi2 <= max2)
                room.error2 = 0;
            else if (rssi2 < min2)
                room.error2 = min2 - rssi2;
            else
                room.error2 = rssi2 - max2;

            // error3
            if(rssi3 >= min3 && rssi3 <= max3)
                room.error3 = 0;
            else if (rssi3 < min3)
                room.error3 = min3 - rssi3;
            else
                room.error3 = rssi3 - max3;

            room.settingErrorSum();
        }
        // sorting...
        Collections.sort(roomList, new Room.RoomComparator());
    }

    static public void setRank(ArrayList<Room> roomList) {
        for(int i = 0; i < roomList.size(); i++) {
            roomList.get(i).rank = String.valueOf(i);
        }
    }
}
