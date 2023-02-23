package de.kai_morich.simple_bluetooth_le_terminal.MainStation;

import de.kai_morich.simple_bluetooth_le_terminal.Defines.Define;

public class Packet {
    // test packet
    public static byte[] sync = {(byte) Define.PacketSize.SyncSize.getValue(),
                                 (byte)Define.PacketId.Sync.getValue(),
                                 (byte) 10, (byte) 20, (byte) 30}; // dummy message


    public static byte[] disc = {(byte) Define.PacketSize.DiscSize.getValue(),
                                 (byte) Define.PacketId.Disc.getValue() };
}
