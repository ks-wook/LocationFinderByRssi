package de.kai_morich.simple_bluetooth_le_terminal.Managers;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;


// 텍스트, 메시지 관련한 일부 기능들을 사용하기위한 클래스
public class UtilityManager {

    private static final UtilityManager _instance = new UtilityManager();

    private UtilityManager() { };

    static public UtilityManager getInstance() { return _instance; }

    private Activity activity;

    public void Init(Activity activity) {
        this.activity = activity;
    }

    public static boolean isNumeric(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void showToastMessage(String msg) {
        Toast.makeText(activity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void showToastMessage(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}

