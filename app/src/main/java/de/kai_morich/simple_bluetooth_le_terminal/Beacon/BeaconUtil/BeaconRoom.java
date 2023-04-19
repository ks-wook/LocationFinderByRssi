package de.kai_morich.simple_bluetooth_le_terminal.Beacon.BeaconUtil;

import java.util.HashMap;

public class BeaconRoom {


    String _roomName;
    byte _roomNum;

    // 0 -> a, primary 1 -> b, 2 -> c
    public HashMap<String, BluetoothBeacon> _beacons = new HashMap<>();

    public BeaconRoom(int roomNum)
    {
        this._roomNum = (byte) roomNum;
    }

    public void AddBeacon(BluetoothBeacon beacon)
    {
        _beacons.put(beacon.macAdr, beacon);
        beacon.room = this;
    }

    public BluetoothBeacon FindBeacon(String adr)
    {
        return _beacons.get(adr);
    }

    public void printBeaconAdr()
    {
        for(int i = 0; i < _beacons.size(); i++)
        {

        }
    }
}
