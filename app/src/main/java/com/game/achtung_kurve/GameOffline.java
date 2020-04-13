package com.game.achtung_kurve;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.example.hello_world.R;

public class GameOffline extends Game {
    final static int flags = View.SYSTEM_UI_FLAG_IMMERSIVE
            |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            |View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
    private ImageButton[] imageButtons = new ImageButton[4];
    public LinearLayout gamefieldLayout;
    public GamefieldView gamefieldView;
    private int currentApiVersion;
    private View decorView;
    public boolean gameStillActive;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameStillActive = false;
    }

    @Override
    protected void onCreate(Bundle savedInstancesState){
        super.onCreate(savedInstancesState);

        decorView = getWindow().getDecorView();
        currentApiVersion = Build.VERSION.SDK_INT;

        if(currentApiVersion >= Build.VERSION_CODES.KITKAT){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                    |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    |View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener(){
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                    {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });
        }

        setContentView(R.layout.gameoffline);

        imageButtons[0] = findViewById(R.id.button_rightP2);
        imageButtons[1] = findViewById(R.id.button_leftP2);
        imageButtons[3] = findViewById(R.id.button_rightP1);
        imageButtons[2] = findViewById(R.id.button_leftP1);

        gameStillActive = true;
        gamefieldLayout = findViewById(R.id.gamefieldoffline);
        gamefieldView = new GamefieldView(this, imageButtons,2);
        gamefieldLayout.addView(gamefieldView);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT){
            decorView.setSystemUiVisibility(flags);
        }
    }

    public void startGameHandler(GamefieldView gamefieldView){
        new Thread(new OfflineGameHandler(gamefieldView)).start();
    }

}
