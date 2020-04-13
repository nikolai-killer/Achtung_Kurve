package com.game.achtung_kurve;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import com.example.hello_world.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class GameLobby extends Game{
//  public final static String serverName = "nikosserver.xyz";
//  public final static String serverName = "46.223.161.41";
    public final static String serverName = "192.168.0.39";

    public static InetAddress serverAdress;
    public static int serverPort = 6969;
    public static boolean inetAvailable;
    public static int heartFrequenzy = 2000;
    public static int timeout = 5000;
    public static float density;

    public final static String playerListHeader =  "AllPlayer\n";
    public final static String invHeader = "Invite\n";
    public final static String lobbyListHeader = "Lobby\n";
    public final static String acceptInvite = "AcceptInv\n";
    public final static String quitHeader = "QuitLobby\n";
    public final static String errorHeader = "Error\n";
    public final static String goGameHeader = "GoGame ";
    public final static String ignoreHeader = "Ignore\n";

    public final static int PLAYERLIST = 1;
    public final static int LOBBYLIST = 2;
    public final static int INVITE = 3;
    public final static int ERROR = 4;
    public final static int GOGAME = 5;

    public static LinkedList<String> playerOnlineList;
    public Lobby lobby;
    public Thread sendQuitLobby;
    public Thread sendGoGame;

    public static ImageButton[] inviteButtons;
    public static int onlineListLayoutHeight = 35;
    public  LinearLayout playerOnlineLinLayout;
    public RelativeLayout[] onlinePlayerEntryLayout;
    public RelativeLayout.LayoutParams onlinePlayerInvButtonParams;
    public RelativeLayout.LayoutParams onlinePlayerEntryParams;
    public RelativeLayout.LayoutParams onlinePlayerTextViewParams;

    public Socket clientSocket;
    protected BufferedWriter clientWriter;
    protected BufferedReader clientReader;
    final CountDownLatch latch = new CountDownLatch(2);
    private boolean sideIsActive;
    private boolean startGame = false;

    private static long sleepTimePlayerList = 2000;
    private String addPlayerString = "Heartbeat: ";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamelobby);
        this.sideIsActive = true;

        isNetworkConnected(this);
        isServerAvailable();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!GameLobby.inetAvailable){
            this.sideIsActive = false;
            buildNotConnectedDialog(this).show();
            return;
        }

        try {
            clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            goBackToMainActivity();
        }

        addPlayerString = addPlayerString + MainActivity.name + "\n\n";
        playerOnlineList = new LinkedList<>();
        lobby = new Lobby(this,MainActivity.name);
        density = getResources().getDisplayMetrics().density;
        playerOnlineLinLayout = findViewById(R.id.playerOnlineLayout);
        int actListLayoutHeight = pixelFromDp(onlineListLayoutHeight);
        onlinePlayerInvButtonParams = new RelativeLayout.LayoutParams(actListLayoutHeight,actListLayoutHeight);
        onlinePlayerInvButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        onlinePlayerEntryParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, actListLayoutHeight);
        onlinePlayerTextViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToMainActivity();
            }
        });
        findViewById(R.id.quitLobby).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendQuitLobby = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            clientWriter.write(GameLobby.quitHeader + "\n");
                            clientWriter.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                sendQuitLobby.start();
            }
        });
        findViewById(R.id.goGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGoGame = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            clientWriter.write(GameLobby.goGameHeader + "\n\n");
                            clientWriter.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                sendGoGame.start();
            }
        });

        sendHeartbeat();
        listenToIncomingMessages();
    }


    private void listenToIncomingMessages() {
        final Game game = this;
        Thread listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket.setSoTimeout(timeout);
                    String line;
                    while(sideIsActive){
                        StringBuilder message = new StringBuilder();
                        while(!(line = clientReader.readLine()).equals("")){
                            message.append(line).append("\n");
                        }
                        handleMessage(message.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(sideIsActive){
                                sideIsActive = false;
                                buildNotConnectedDialog(game).show();
                            }
                        }
                    });

                }
            }
        });
        listenThread.start();
    }

    private void handleMessage(String message) {
        switch(getMessageCode(message)){
            case GameLobby.PLAYERLIST:
                actOnlinePlayer(message);
                break;
            case GameLobby.LOBBYLIST:
                lobby.actLobbyList(message);
                break;
            case GameLobby.INVITE:
                showInvite(message);
                break;
            case GameLobby.ERROR:
                showErrorNotification(message);
                break;
            case GameLobby.GOGAME:
                sideIsActive = false;
                startGame(message);
                break;
            default:
                System.out.println("Not Found:" + message);
                break;
        }
    }

    private int getMessageCode(String message){
        if(message.startsWith(GameLobby.playerListHeader)){
            return GameLobby.PLAYERLIST;
        }
        else if(message.startsWith(GameLobby.lobbyListHeader)){
            return GameLobby.LOBBYLIST;
        }
        else if(message.startsWith(GameLobby.invHeader)){
            return GameLobby.INVITE;
        }
        else if(message.startsWith(GameLobby.errorHeader)){
            return GameLobby.ERROR;
        }
        else if(message.startsWith(GameLobby.goGameHeader)){
            return GameLobby.GOGAME;
        }
        return -1;
    }

    private void startGame(String message){
        startGame = true;
        int amount = Integer.parseInt(message.substring(GameLobby.goGameHeader.length(),message.length()-1));
        SocketHandler.setSocket(this.clientSocket);
        Intent newOnlineGame = new Intent("com.example.hello_world.NEWGAMEONLINE");
        newOnlineGame.putExtra(GameOnline.arrowSourceKey,this.lobby.getOwnIndex());
        newOnlineGame.putExtra(GameOnline.amountSourceKey,amount);
        startActivity(newOnlineGame);
        finish();
    }

    public void showErrorNotification(String message){
        final String errorString = "Error: "+ message.substring(GameLobby.errorHeader.length(),message.length()-1);
        final Snackbar snackError = Snackbar.make(findViewById(R.id.gameLobbyId), errorString,Snackbar.LENGTH_LONG);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                snackError.show();
            }
        });
    }

    public void showInvite(String message){
        final String player = message.substring(invHeader.length(),message.length()-1);
        final Snackbar snackInv = Snackbar.make(findViewById(R.id.gameLobbyId), "Invitation from: " + player, Snackbar.LENGTH_LONG).setAction("Accept", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    clientWriter.write(acceptInvite + player + "\n\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                snackInv.show();
            }
        });
    }

    private void actOnlinePlayer(String message) {
        boolean changed = false;
        String[] player = message.split("\n");
        if(GameLobby.playerOnlineList.size() == (player.length-1)){
            for(int i = 1; i<player.length;i++){
                if(!GameLobby.playerOnlineList.contains(player[i])){
                    changed = true;
                    break;
                }
            }
        }
        else{
            changed = true;
        }

        if(changed){
            GameLobby.playerOnlineList.clear();
            GameLobby.playerOnlineList.addAll(Arrays.asList(player).subList(1, player.length));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addPlayerListToView();
            }
        });
    }

    private void addPlayerListToView() {
        inviteButtons = new ImageButton[GameLobby.playerOnlineList.size()];
        playerOnlineLinLayout.removeAllViews();
        onlinePlayerEntryLayout = new RelativeLayout[GameLobby.playerOnlineList.size()];

        for(int i = 0; i<onlinePlayerEntryLayout.length;i++){
            if(!GameLobby.playerOnlineList.get(i).equals(MainActivity.name) && !lobby.contains(GameLobby.playerOnlineList.get(i))) {
                onlinePlayerEntryLayout[i] = new RelativeLayout(this);
                onlinePlayerEntryLayout[i].setLayoutParams(onlinePlayerEntryParams);
                onlinePlayerEntryLayout[i].setGravity(Gravity.CENTER_VERTICAL);

                TextView playerName = new TextView(this);
                playerName.setText(GameLobby.playerOnlineList.get(i));
                playerName.setLayoutParams(onlinePlayerTextViewParams);
                playerName.setGravity(Gravity.CENTER_VERTICAL);


                inviteButtons[i] = new ImageButton(this);
                inviteButtons[i].setLayoutParams(onlinePlayerInvButtonParams);
                inviteButtons[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
                inviteButtons[i].setImageResource(R.drawable.invite);
                inviteButtons[i].setBackgroundColor(Color.TRANSPARENT);
                inviteButtons[i].setTag(GameLobby.playerOnlineList.get(i));
                inviteButtons[i].setOnClickListener(new GameLobbyButtonHandler(this,GameLobby.playerOnlineList.get(i)));

                View sep = new View(this);
                sep.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,5));
                sep.setBackgroundColor(Color.GRAY);

                onlinePlayerEntryLayout[i].addView(playerName);
                onlinePlayerEntryLayout[i].addView(inviteButtons[i]);
                playerOnlineLinLayout.addView(onlinePlayerEntryLayout[i]);
                playerOnlineLinLayout.addView(sep);
            }
        }
    }

    public void sendHeartbeat(){
        Thread sendHeartbeatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(sideIsActive){
                        try {
                            clientWriter.write(addPlayerString);
                            clientWriter.flush();
                            Thread.sleep(GameLobby.sleepTimePlayerList);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    goBackToMainActivity();
                }
            }
        });
        sendHeartbeatThread.start();
    }

    public static AlertDialog buildNotConnectedDialog(final Game game){
        AlertDialog.Builder ad = new AlertDialog.Builder(game);
        ad.setTitle("Cant connect to the server");
        ad.setMessage("Maybe no internet connection or the server is offline");
        ad.setNeutralButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                game.goBackToMainActivity();
            }
        });
        ad.setCancelable(false);
        return ad.create();
    }

    private void isServerAvailable() {
        try {
            Thread newThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    InetAddress ipAddr;
                    try {
                        ipAddr = InetAddress.getByName(new URL("http://" + GameLobby.serverName).getHost());
                        if(!ipAddr.toString().equals("")){
                            GameLobby.serverAdress = ipAddr;
                            clientSocket = new Socket();
                            clientSocket.connect(new InetSocketAddress(GameLobby.serverAdress,GameLobby.serverPort),GameLobby.heartFrequenzy);

                            GameLobby.inetAvailable = true;
                        }
                        else{
                            GameLobby.inetAvailable = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        GameLobby.inetAvailable = false;
                    }
                    latch.countDown();
                }
            });
            newThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            GameLobby.inetAvailable = false;
            latch.countDown();
        }
    }

    public void isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm == null) {
            GameLobby.inetAvailable=false;
            return;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if(nc != null){
                if(nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                    GameLobby.inetAvailable =true;
                }
                if(nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                    GameLobby.inetAvailable =true;
                }
                if(nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                    GameLobby.inetAvailable =true;
                }
            }
        }
        else{
            GameLobby.inetAvailable =  cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
        }
        latch.countDown();
    }

    @Override
    protected void onDestroy() {
        try {
            if(!startGame){
                if(this.clientWriter != null){
                    this.clientWriter.close();
                }
                if(this.clientReader != null){
                    this.clientReader.close();
                }
                this.clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.sideIsActive = false;
        super.onDestroy();
    }

    public int pixelFromDp(int dp){
        return (int) (dp * GameLobby.density);
    }

    @Override
    public void startGameHandler(GamefieldView gamefieldView) {

    }
}
