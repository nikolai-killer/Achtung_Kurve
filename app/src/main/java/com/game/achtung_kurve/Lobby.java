package com.game.achtung_kurve;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.hello_world.R;

import java.util.Arrays;
import java.util.LinkedList;

public class Lobby {
    public static final int maxPlayer = 4;
    public static final int[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};
    public static final int[] ids = {R.id.lobbySpotBlue, R.id.lobbySpotRed, R.id.lobbySpotGreen,R.id.lobbySpotYellow};
    public static final int[] idArrows = {R.drawable.right_tri_blue,R.drawable.right_tri_red,R.drawable.right_tri_green,R.drawable.right_tri_yellow};
    public static LinearLayout.LayoutParams lobbyLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

    private Activity activity;
    private LinkedList<String> player;

    public Lobby(Activity activity, String name){
        player = new LinkedList<>();
        this.activity = activity;
        addPlayer(name);
    }

    public int getPlayerNumber(){
        return this.player.size();
    }

    public void addPlayer(String name){
        this.player.add(name);
        this.actLobbyView();
    }

    public boolean contains(String name){
        return player.contains(name);
    }

    public void actLobbyList(String message){
        String[] playerArr = message.split("\n");
        LinkedList<String> playerList =  new LinkedList<>(Arrays.asList(playerArr).subList(1,playerArr.length));
        if(!playerList.equals(player)){
            player = playerList;
            actLobbyView();
        }
    }

    public void actLobbyView(){
        final LinearLayout[] layouts = new LinearLayout[Lobby.maxPlayer];
        final TextView[] textViews = new TextView[player.size()];

        for(int i = 0;i<Lobby.maxPlayer;i++){
            layouts[i] = activity.findViewById(ids[i]);
            if(i < player.size()) {
                textViews[i] = new TextView(activity);
                textViews[i].setText(player.get(i));
                textViews[i].setLayoutParams(Lobby.lobbyLayoutParams);
                textViews[i].setGravity(Gravity.CENTER_VERTICAL);
            }
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i<player.size(); i++){
                    layouts[i].removeAllViews();
                    layouts[i].addView(textViews[i]);
                }
                for(int rest = player.size();rest<Lobby.maxPlayer;rest++){
                    layouts[rest].removeAllViews();
                }
            }
        });
    }

    public int getOwnIndex() {
        for(String i:player){
            if(MainActivity.name.equals(i)){
                return player.indexOf(i);
            }
        }
        return 0;
    }
}
