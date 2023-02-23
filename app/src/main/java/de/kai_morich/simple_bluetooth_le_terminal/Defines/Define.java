package de.kai_morich.simple_bluetooth_le_terminal.Defines;

public class Define {

     static public enum ScreenState {
         Home,
         Wifi,
         WifiTest,
         Bluetooth,
         Setting,

    }

    public static enum PacketId {

        Conc(1),
        Sync(2),
        Disc(3),;

        private final int value;
        PacketId(int value) {
            this.value = value;
        }
        public int getValue() { return value; }
    }

    public static enum LogId {

        Send(4),
        Recv(5),;

        private final int value;
        LogId (int value) {
            this.value = value;
        }
        public int getValue() { return value; }

    }


    public static enum PacketSize {

        // TODO
        SyncSize(5),
        DiscSize(2),;


        private final int value;
        PacketSize(int value) {
            this.value = value;
        }
        public int getValue() { return value; }
    }


}
