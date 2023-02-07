package de.kai_morich.simple_bluetooth_le_terminal.Wifi;

import java.util.Comparator;

public class WiFiAccessPoint implements Comparator<WiFiAccessPoint> {

    private String SSID;
    private String MacAddress;
    private int Rssi = -100;
    private int error = -100;

    public double midValue = 0;

    public WiFiAccessPoint(String SSID, String BSSID) {
        this.SSID = SSID;
        this.MacAddress = BSSID;
    }

    public String getSSID() {
        return SSID;
    }

    public String getMacAddress() {
        return MacAddress;
    }

    public int getRssi() {
        return Rssi;
    }

    public void setRssi(int rssi) {
        Rssi = rssi;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    @Override
    public int compare(WiFiAccessPoint ap1, WiFiAccessPoint ap2) {
        if(ap1.getError() > ap2.getError()) {
            // ap1의 오차 값이 더 큰 경우
            return 1;
        }
        else if(ap1.getError() < ap2.getError()) {
            // ap2의 오차 값이 더 큰 경우
            return -1;
        }
        else {
            // ap1과 ap2의 오차 값이 같은 경우

            // 중간 값과의 차이가 ap1이 더 큰 경우
            if(Math.abs(midValue - ap1.getRssi()) > Math.abs(midValue - ap2.getRssi())) {
                return 1;
            }
            else if(Math.abs(midValue - ap1.getRssi()) < Math.abs(midValue - ap2.getRssi())) {
                return -1;
            }
            else {
                // 차이마저 같은 경우
                return 0;
            }
        }
    }
}
