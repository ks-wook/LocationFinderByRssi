package de.kai_morich.simple_bluetooth_le_terminal.Managers;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

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


}
