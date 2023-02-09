package de.kai_morich.simple_bluetooth_le_terminal.Managers;

import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;



// 텍스트뷰나 에딧뷰 등의 기본 뷰에 이벤트 리스터를 쉽게 사용하기 위한 Manager
// 필요시 메소드를 더 선언하여서 사용, 싱글턴 패턴 유의
public class ViewSetManager {

    private static final ViewSetManager _instance = new ViewSetManager();

    private ViewSetManager() { };

    public static ViewSetManager getInstance() {
        return _instance;
    }

    public void setText(TextView t, String str) {
        t.setText(str);
    }

    public void setText(EditText t, String str) {
        t.setText(str);
    }

    public String getText(TextView t) {
        return t.getText().toString();
    }

    public String getText(EditText t) {
        return t.getText().toString();
    }

    public void setListener(SeekBar s, SeekBar.OnSeekBarChangeListener listener) {
        s.setOnSeekBarChangeListener(listener);
    }

    public void setVisibility(View view, int visible) {
        view.setVisibility(visible);
    }

}
