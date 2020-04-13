package com.game.achtung_kurve;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import com.example.hello_world.R;

public class EndDialog extends Dialog implements android.view.View.OnClickListener {
    Game a;
    Button ok;
    Button revanche;
    String text;
    int color;

    public EndDialog(@NonNull Activity activity, String text, int color) {
        super(activity);
        a = (Game) activity;
        this.text = text;
        this.color = color;
        this.setCanceledOnTouchOutside(false);
        this.setCancelable(false);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()   ){
            case R.id.ok_button:
                a.goBackToMainActivity();
                break;
            case R.id.revanche_button:
                a.startAgain();
                break;
            default:
                a.goBackToMainActivity();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offlinefinished);
        TextView tv = findViewById(R.id.endtext);
        View view = findViewById(R.id.finished_sign);
        view.setBackgroundColor(color);
        tv.setText(text);
        ok = (Button) findViewById(R.id.ok_button);
        revanche = (Button) findViewById(R.id.revanche_button);
        revanche.setOnClickListener(this);
        ok.setOnClickListener(this);

    }
}
