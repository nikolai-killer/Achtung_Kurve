package com.game.achtung_kurve;

import android.annotation.SuppressLint;
import java.io.*;
import java.net.Socket;

public class OnlineGameHandler implements Runnable {
    final public static int LEFT = 1;
    final public static int STRAIGHT = 2;
    final public static int RIGHT = 3;
    final public static String placementString = "Placement: ";
    final public static String countDownString = "Countdown: ";
    final public static String[] circleStrings = {"BLUE;","RED;","GREEN;","YELLOW;"};
    private GameOnline gameOnline;
    public Socket socket;
    public BufferedReader reader;
    public BufferedWriter writer;
    private GamefieldView gamefieldView;
    private boolean listenToMessages;
    private int placement;
    private int colorIndex;


    public OnlineGameHandler(GamefieldView gamefieldView, int colorIndex){
        super();
        this.socket = SocketHandler.getSocket();
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        this.gamefieldView = gamefieldView;
        this.listenToMessages = true;
        this.gameOnline = (GameOnline) gamefieldView.game;
        this.placement = 0;
        this.colorIndex = colorIndex;

        for(int i = 0; i<gamefieldView.player;i++){
            gamefieldView.circles[i] = new Circle(gamefieldView,gamefieldView.colors[i]);
        }
        setListener();
    }

    @Override
    public void run() {
        try{
            //to prevernt any errors
            writer.write(GameLobby.ignoreHeader + "\n");
            writer.flush();
            writer.write("\n\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!initCircle()){
            gameOnline.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GameLobby.buildNotConnectedDialog(gameOnline).show();
                }
            });
            return;
        }
        String line;
        boolean found = false;
        while(listenToMessages) {
            try {
                if((line = reader.readLine()) != null){
                    for(int i = 0; i<gamefieldView.player;i++){
                        if(line.startsWith(OnlineGameHandler.circleStrings[i])){
                            String[] split = line.split(";");
                            gamefieldView.circles[i].goToLocation((int) (Double.parseDouble(split[1]) * gamefieldView.gamefieldWidth),(int)(Double.parseDouble(split[2]) * gamefieldView.gamefieldHeight));
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        if(line.startsWith(placementString)){
                            placement = Integer.parseInt(line.split(" ")[1]);
                            System.out.println("Placement: " + placement);
                            listenToMessages = false;
                        }
                    }
                    found = false;
                }
                else{
                    listenToMessages = false;
                }
                gamefieldView.invalidate();
            } catch (IOException e) {
                e.printStackTrace();
                gameOnline.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GameLobby.buildNotConnectedDialog(gameOnline).show();
                    }
                });
                listenToMessages = false;
                return;
            }
        }

        final String text = "You got " + placement + ". Place";
        final int color = Lobby.colors[colorIndex];


        gameOnline.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final EndDialog ed = new EndDialog(gameOnline,text,color);
                ed.show();
            }
        });
        try {
            writer.write(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void setListener(){
        gamefieldView.imageButtons[0].setOnTouchListener(new imageButtonsListener(gamefieldView.circles[0],true,writer));
        gamefieldView.imageButtons[1].setOnTouchListener(new imageButtonsListener(gamefieldView.circles[0],false,writer));
    }

    public boolean initCircle(){
        try {
            for(int countDownTimer = 0; countDownTimer<3;countDownTimer++){
                String countdown = reader.readLine();
                if(!countdown.startsWith(countDownString)){
                    return false;
                }
                gamefieldView.countdown = Integer.parseInt(countdown.split(" ")[1]);
                String[] initCircles = new String[gamefieldView.player];
                for(int i = 0; i<gamefieldView.player;i++){
                    initCircles[i] = reader.readLine();
                    if(!initCircles[i].startsWith(circleStrings[i])){
                        return false;
                    }
                    String[] split = initCircles[i].split(";");
                    gamefieldView.circles[i].setPosition((int) (Double.parseDouble(split[1]) * gamefieldView.gamefieldWidth), (int) (Double.parseDouble(split[2]) * gamefieldView.gamefieldHeight));
                    gamefieldView.circles[i].setDirection(Double.parseDouble(split[3]));
                }
                if(gamefieldView.countdown < 3){
                    gamefieldView.removeStartArrowPath();
                }
                else{
                    gamefieldView.drawStartArrowPath();
                }
                gamefieldView.invalidate();
            }
            gamefieldView.countdown = 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
