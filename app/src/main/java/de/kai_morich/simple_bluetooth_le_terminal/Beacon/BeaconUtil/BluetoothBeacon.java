package de.kai_morich.simple_bluetooth_le_terminal.Beacon.BeaconUtil;

import android.util.Log;

public class BluetoothBeacon {

    boolean _isPrimary = false;
    String macAdr;
    public BeaconRoom room = null;

    byte[] rssiData = new byte[10];
    int index = 0;

    public BluetoothBeacon (String address)
    {
        this.macAdr = address;
    }

    public boolean saveRssi(int rssi)
    {
        rssiData[index++] = (byte) rssi;
        Log.v("rssi", "beacon adr : " + this.macAdr + "rssi : " + String.valueOf(rssi));

        if(index == 10) {
            index = 0;
            Log.v("rssi", "scan success");
            return false;
        }

        return true;
    }
}
