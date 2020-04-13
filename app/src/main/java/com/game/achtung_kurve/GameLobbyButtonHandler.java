package com.game.achtung_kurve;

import android.view.View;
import com.example.hello_world.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

public class GameLobbyButtonHandler extends Thread implements View.OnClickListener {

    String player;
    GameLobby gameLobby;

    public GameLobbyButtonHandler(GameLobby gameLobby, String player){
        this.player = player;
        this.gameLobby = gameLobby;
    }

    @Override
    public void onClick(View v) {
        if(this.getState() == State.NEW){
            this.start();
        }
    }

    @Override
    public void run() {
        super.run();
        if(GameLobby.playerOnlineList.contains(player)){
            sendInviteTo(player);
        }
    }

    private void sendInviteTo(String player){
        try {
            this.gameLobby.clientWriter.write(GameLobby.invHeader + player + "\n\n");
            this.gameLobby.clientWriter.flush();
            showInviteNotification(player);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showInviteNotification(String player){
        if(gameLobby.lobby.getPlayerNumber() < Lobby.maxPlayer){
            final Snackbar snackInv = Snackbar.make(gameLobby.findViewById(R.id.gameLobbyId), "Invitation send to: " + player, Snackbar.LENGTH_LONG);
            gameLobby.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    snackInv.show();
                }
            });
        }
    }
}
