package com.game.achtung_kurve;

import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedWriter;
import java.io.IOException;

public class imageButtonsListener implements View.OnTouchListener {
    Circle circle;
    private boolean isRight;
    private boolean sendToServer;
    public BufferedWriter bufferedWriter;
    public imageButtonsListener(Circle circle, boolean isRight){
        this.circle = circle;
        this.isRight = isRight;
        this.sendToServer = false;
    }

    public imageButtonsListener(Circle circle, boolean isRight, BufferedWriter bufferedWriter){
        this.circle = circle;
        this.isRight = isRight;
        this.sendToServer = true;
        this.bufferedWriter = bufferedWriter;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(isRight){
                    circle.setRightIsPressed(true);
                }
                else{
                    circle.setLeftIsPressed(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(isRight){
                    circle.setRightIsPressed(false);
                }
                else{
                    circle.setLeftIsPressed(false);
                }
                break;
        }
        try{
            if(sendToServer){
                if(circle.getLeftIsPressed() && !circle.getRightIsPressed()){
                    bufferedWriter.write(OnlineGameHandler.LEFT);
                }
                else if(!circle.getLeftIsPressed() && circle.getRightIsPressed()){
                    bufferedWriter.write(OnlineGameHandler.RIGHT);
                }
                else{
                    bufferedWriter.write(OnlineGameHandler.STRAIGHT);
                }
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            bufferedWriter.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
}
