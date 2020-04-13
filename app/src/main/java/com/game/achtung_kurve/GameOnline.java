package com.game.achtung_kurve;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.example.hello_world.R;


public class GameOnline extends Game{
    final public static double neededScale = 1.185;
    final static int flags = View.SYSTEM_UI_FLAG_IMMERSIVE
            |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            |View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
    public static String arrowSourceKey = "ArrowSourceKeyExtra";
    public static String amountSourceKey = "AmountSourceKeyExtra";


    private ImageButton[] imageButtons = new ImageButton[2];
    public LinearLayout gamefieldLayout;
    public GamefieldView gamefieldView;
    private int currentApiVersion;
    private View decorView;
    public boolean gameStillActive;
    private int id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        decorView = getWindow().getDecorView();
        currentApiVersion = Build.VERSION.SDK_INT;

        if(currentApiVersion >= Build.VERSION_CODES.KITKAT){
            decorView.setSystemUiVisibility(flags);

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

        setContentView(R.layout.gameonline);

        imageButtons[0] = findViewById(R.id.button_left);
        imageButtons[1] = findViewById(R.id.button_right);
        Intent myIntent = getIntent();
        final int player = myIntent.getIntExtra(GameOnline.amountSourceKey,1);
        id = myIntent.getIntExtra(GameOnline.arrowSourceKey,0);
        int idArrow = Lobby.idArrows[id];
        imageButtons[0].setImageResource(idArrow);
        imageButtons[1].setImageResource(idArrow);
        gameStillActive = true;
        gamefieldLayout = findViewById(R.id.gamefieldonline);
        final GameOnline it = this;
        gamefieldLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                gamefieldLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                int height = gamefieldLayout.getHeight();
                int width = gamefieldLayout.getWidth();
                double scale = height / (double) width;
                if(scale < GameOnline.neededScale){
                    gamefieldLayout.setLayoutParams(new LinearLayout.LayoutParams((int) ((1/GameOnline.neededScale) * height),0,4f));

                }
                else if(scale > GameOnline.neededScale){
                    gamefieldLayout.setLayoutParams(new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)(GameOnline.neededScale * width))));
                }
                gamefieldView = new GamefieldView(it, imageButtons,player);
                gamefieldLayout.addView(gamefieldView);
                return false;
            }
        });
    }

    @Override
    public void startAgain() {
        finish();
        Intent newGameLobby = new Intent("com.example.hello_world.GAMELOBBY");
        startActivity(newGameLobby);
    }

    @Override
    public void startGameHandler(GamefieldView gamefieldView) {
        new Thread(new OnlineGameHandler(gamefieldView,id)).start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT){
            decorView.setSystemUiVisibility(flags);
        }
    }
}
