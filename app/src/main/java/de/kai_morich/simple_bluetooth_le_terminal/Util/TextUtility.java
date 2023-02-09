package de.kai_morich.simple_bluetooth_le_terminal.Util;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;


// 텍스트, 메시지 관련한 일부 기능들을 사용하기위한 클래스
public class TextUtility {

    public static boolean isNumeric(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void showToastMessage(Activity activity, String msg) {
        Toast.makeText(activity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToastMessage(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}

