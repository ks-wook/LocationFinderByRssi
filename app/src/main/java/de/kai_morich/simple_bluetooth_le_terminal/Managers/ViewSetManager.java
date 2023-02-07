package de.kai_morich.simple_bluetooth_le_terminal.Managers;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

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
