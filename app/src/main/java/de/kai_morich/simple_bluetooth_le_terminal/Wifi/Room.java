package de.kai_morich.simple_bluetooth_le_terminal.Wifi;

import java.util.Comparator;

public class Room implements Cloneable {

    public String Name;

    private int ap1Min = -100;
    private int ap2Min = -100;
    private int ap3Min = -100;

    private int ap1Max = -100;
    private int ap2Max = -100;
    private int ap3Max = -100;

    public int error1 = 100;
    public int error2 = 100;
    public int error3 = 100;
    public int errorSum = 300;
    public String rank = "100";

    public Room(String Name, int ap1Min, int ap1Max, int ap2Min, int ap2Max, int ap3Min, int ap3Max) {
        this.Name = Name;

        this.ap1Min = ap1Min;
        this.ap1Max = ap1Max;

        this.ap2Min = ap2Min;
        this.ap2Max = ap2Max;

        this.ap3Min = ap3Min;
        this.ap3Max = ap3Max;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getAp1Min() {
        return ap1Min;
    }

    public void setAp1Min(int ap1Min) {
        this.ap1Min = ap1Min;
    }

    public int getAp2Min() {
        return ap2Min;
    }

    public void setAp2Min(int ap2Min) {
        this.ap2Min = ap2Min;
    }

    public int getAp3Min() {
        return ap3Min;
    }

    public void setAp3Min(int ap3Min) {
        this.ap3Min = ap3Min;
    }

    public int getAp1Max() {
        return ap1Max;
    }

    public void setAp1Max(int ap1Max) {
        this.ap1Max = ap1Max;
    }

    public int getAp2Max() {
        return ap2Max;
    }

    public void setAp2Max(int ap2Max) {
        this.ap2Max = ap2Max;
    }

    public int getAp3Max() {
        return ap3Max;
    }

    public void setAp3Max(int ap3Max) {
        this.ap3Max = ap3Max;
    }

    public void settingErrorSum() {
        this.errorSum = this.error1 + this.error2 + this.error3;
    }

    static public class RoomComparator implements Comparator<Room> {

        @Override
        public int compare(Room r1, Room r2) {
            if(r1.errorSum > r2.errorSum)
                return 1;
            else if(r1.errorSum < r2.errorSum)
                return -1;

            return 0;
        }
    }
}

