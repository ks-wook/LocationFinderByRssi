package de.kai_morich.simple_bluetooth_le_terminal.Beacon.BeaconUtil;

import java.util.HashMap;

public class BeaconRoomManager {

    static BeaconRoomManager BeaconRoom = new BeaconRoomManager();

    private BeaconRoomManager() { }

    public static BeaconRoomManager getInstance() { return BeaconRoom; }

    static HashMap<Integer, BeaconRoom> _beaconRooms = new HashMap<>();

    public void Add(BeaconRoom beaconRoom)
    {
        _beaconRooms.put((int)beaconRoom._roomNum, beaconRoom);

    }

    public void Remove(int roomId)
    {
        _beaconRooms.remove(roomId);
    }

    public BeaconRoom Find(int roomId)
    {
        return _beaconRooms.get(roomId);
    }


}
