package de.kai_morich.simple_bluetooth_le_terminal.MainStation;


import de.kai_morich.simple_bluetooth_le_terminal.Managers.MainStationManager;

public class SendThread extends Thread {

    MainStationManager msManager;
    public SendThread() {
        msManager = MainStationManager.getInstance();
    }

    @Override
    public void run(){
        try
        {
            byte[] data = new byte[] {10, 20, 30};
            msManager.Send((byte) 5, (byte) 0, data);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}

