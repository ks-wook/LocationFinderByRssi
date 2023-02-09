package de.kai_morich.simple_bluetooth_le_terminal.MainStation;


import de.kai_morich.simple_bluetooth_le_terminal.Managers.MainStationManager;

// 임시용 클래스 SendThread 에 통합에정
public class DisConnectThread extends Thread {

    MainStationManager msManager;
    public DisConnectThread() {
        msManager = MainStationManager.getInstance();
    }

    @Override
    public void run(){
        try {
            msManager.Send((byte) 2, (byte) 2, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
