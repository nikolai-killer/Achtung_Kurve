package com.game.achtung_kurve;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.example.hello_world.R;

public class Splash extends Activity {
    private final int delay = 400;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.splash);
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally{
                    finish();
                    Intent weiterleitung = new Intent("com.example.hello_world.MAINACTIVITY");
                    startActivity(weiterleitung);
                }

            }
        };
        t.start();
    }
}
