package de.kai_morich.simple_bluetooth_le_terminal.MainStation;


import de.kai_morich.simple_bluetooth_le_terminal.Managers.MainStationManager;

public class SendThread extends Thread {

    MainStationManager msManager;
    public SendThread() {
        msManager = MainStationManager.getInstance();
    }

    @Override
    public void run(){
        try {
            msManager.send();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

