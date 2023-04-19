package de.kai_morich.simple_bluetooth_le_terminal.Managers;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

// 버튼에 리스너를 달게 하기 위해 만든 클래스
// 싱글턴 패턴에 유의, 필요시 메소드 추가
public class ButtonManager {

    private static final ButtonManager _instance = new ButtonManager();

    private ButtonManager () { }

    static public ButtonManager getInstance () { return _instance;}

    public void setClickListener(Button view, View.OnClickListener listener) {
        view.setOnClickListener(listener);
    }

    public void setImgClickListener(ImageButton view, View.OnClickListener listener) {
        view.setOnClickListener(listener);
    }

    public void setTxtClickListener(TextView view, View.OnClickListener listener) {
        view.setOnClickListener(listener);
    }

}
