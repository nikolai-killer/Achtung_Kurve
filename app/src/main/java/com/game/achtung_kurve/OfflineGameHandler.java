package com.game.achtung_kurve;

import android.annotation.SuppressLint;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class OfflineGameHandler implements Runnable {
    public static final long sleepTime = 1000/40;
    private GamefieldView gamefieldView;
    final CountDownLatch latch = new CountDownLatch(1);

    public OfflineGameHandler(GamefieldView gamefieldView){
        super();
        this.gamefieldView = gamefieldView;
        setUpStartingPoints();
        setListener();
    }

    @Override
    public void run() {
        Thread t = new Thread(){
            @Override
            public void run() {
                gamefieldView.countdown = GamefieldView.countdownlength;
                while(gamefieldView.countdown > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        gamefieldView.countdown--;
                        gamefieldView.invalidate();
                        if(gamefieldView.countdown <= 2){
                            latch.countDown();
                        }
                    }
                }
            }
        };
        t.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startTracker();
    }

    private void setUpStartingPoints() {
        int player = gamefieldView.player;
        int gamefieldWidth=gamefieldView.gamefieldWidth;
        int gamefieldHeight=gamefieldView.gamefieldHeight;
        int startPadding=gamefieldView.startPadding;
        //        Random starting velocity and position
        Random r = new Random();
        double tempdir;
        int tempx;
        int tempy;
        for(int i = 0; i<player; i++){
            tempdir = r.nextDouble() * 2*Math.PI;
            tempx = (int) Math.round(r.nextDouble() * (gamefieldWidth- 2*startPadding) + startPadding);
            tempy = (int) Math.round(r.nextDouble() * (gamefieldHeight- 2*startPadding) + startPadding);
            gamefieldView.circles[i] = new Circle(this.gamefieldView, gamefieldView.colors[i]);
            gamefieldView.circles[i].setPosition(tempx,tempy);
            gamefieldView.circles[i].setDirection(tempdir);
        }

        this.gamefieldView.drawStartArrowPath();
    }

    public synchronized void startTracker(){
        GameOffline actAct = (GameOffline) gamefieldView.game;
        this.gamefieldView.removeStartArrowPath();
        while(gamefieldView.playerAlive>1 && actAct.gameStillActive){
            try {
                Thread.sleep(OfflineGameHandler.sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                for(int i = 0; i<gamefieldView.player;i++){
                    gamefieldView.circles[i].goToNextLocation();
                }
                gamefieldView.invalidate();

            }
        }

        if(actAct.gameStillActive) {
            gamefieldView.game.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String text;
                    int color;
                    if (gamefieldView.circles[0].isAlive()) {
                        text = "Blue Won!!";
                        color = gamefieldView.circles[0].getCirclePaint().getColor();
                    } else {
                        text = "Red Won!!";
                        color = gamefieldView.circles[1].getCirclePaint().getColor();
                    }
                    EndDialog ed = new EndDialog(gamefieldView.game, text, color);
                    ed.show();
                }
            });
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void setListener(){
        for(int i = 0; i<gamefieldView.player; i++){
            gamefieldView.imageButtons[i*2].setOnTouchListener(new imageButtonsListener(gamefieldView.circles[i],true));
            gamefieldView.imageButtons[i*2+1].setOnTouchListener(new imageButtonsListener(gamefieldView.circles[i],false));
        }
    }


}
