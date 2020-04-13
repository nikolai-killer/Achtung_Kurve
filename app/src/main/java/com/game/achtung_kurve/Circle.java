package com.game.achtung_kurve;

import android.graphics.Paint;
import android.graphics.Path;

public class Circle {
    private int x;
    private int y;
    private float circleSize;
    private GamefieldView context;
    private Paint circlePaint;
    private Paint pathPaint;
    private Path path;
    private double direction;
    private double dirChangeSpeed;
    private double circleSpeed;
    private boolean leftIsPressed;
    private boolean rightIsPressed;
    private boolean alive;

    public Circle(GamefieldView context, int color){
        this.context=context;
        this.circleSize =context.defaultCircleSize;
        this.dirChangeSpeed = GamefieldView.defaultDirChangeSpeed;
        this.circleSpeed = GamefieldView.defaultCircleSpeed;
        this.circlePaint = new Paint();
        this.circlePaint.setAntiAlias(false);
        this.circlePaint.setColor(color);
        this.path = new Path();
        this.pathPaint = new Paint();
        this.pathPaint.setColor(color);
        this.pathPaint.setStrokeWidth(context.defaultCirclePathWidth);
        this.pathPaint.setStyle(Paint.Style.STROKE);
        this.pathPaint.setAntiAlias(false);
        this.leftIsPressed = false;
        this.rightIsPressed = false;
        this.alive = true;
    }

    public void setPosition(int x, int y){
        this.x = x;
        this.y = y;
        this.path.moveTo(x,y);
        context.bitmapCanvas.drawCircle(getX(),getY(),getCircleSize(),getCirclePaint());
    }

    public void setDirection(double direction){
        this.direction = direction;
    }

    public boolean isAlive(){
        return alive;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public void setLeftIsPressed(boolean bool){
        this.leftIsPressed = bool;
    }
    public void setRightIsPressed(boolean bool){
        this.rightIsPressed = bool;
    }
    public boolean getLeftIsPressed(){
        return leftIsPressed;
    }
    public boolean getRightIsPressed(){
        return rightIsPressed;
    }


    public void setDirectionRight(){
        direction = direction - dirChangeSpeed;
    }
    public  void setDirectionLeft(){
        direction = direction + dirChangeSpeed;
    }

    public void setCircleSpeed(double speed){
        this.circleSpeed = speed;
    }

    public void setColor(int color){
        this.circlePaint.setColor(color);
        this.pathPaint.setColor(color);
    }

    public Paint getCirclePaint(){
        return circlePaint;
    }

    public Path getPath(){
        return path;
    }

    public Paint getPathPaint(){
        return pathPaint;
    }

    public float getCircleSize(){
        return circleSize;
    }

    public double getDirection() {
        return direction;
    }


    public void goToNextLocation(){
        if(circleSpeed == 0){
            return;
        }
        if(this.getRightIsPressed()){
            this.setDirectionRight();
        }
        if(this.getLeftIsPressed()){
            this.setDirectionLeft();
        }
        this.context.bitmapCanvas.drawCircle(getX(),getY(),getCircleSize()+1,GamefieldView.restoreCirclePaint);

        this.x = this.x + (int) Math.round(Math.cos(this.direction) * this.circleSpeed);
        this.y = this.y - (int) Math.round(Math.sin(this.direction) * this.circleSpeed);

        if(!circleIsInGF() || hitOtherPath()){
            endCircle();
        }

        this.context.bitmapCanvas.drawCircle(getX(),getY(),getCircleSize(),getCirclePaint());
        this.path.lineTo(x,y);
        this.context.bitmapCanvas.drawPath(getPath(),getPathPaint());
    }

    public void goToLocation(int x, int y){
        this.context.bitmapCanvas.drawCircle(getX(),getY(),getCircleSize()+1,GamefieldView.restoreCirclePaint);
        this.x  = x;
        this.y = y;
        this.context.bitmapCanvas.drawCircle(getX(),getY(),getCircleSize(),getCirclePaint());
        this.path.lineTo(x,y);
        this.context.bitmapCanvas.drawPath(getPath(),getPathPaint());
    }

    protected boolean circleIsInGF(){
        if(getX() < (getCircleSize()) || getX() > context.gamefieldWidth - getCircleSize()){
            return false;
        }
        return !(getY() < getCircleSize()) && !(getY() > context.gamefieldHeight - getCircleSize());
    }

    protected boolean hitOtherPath(){
        double anzPixel = 11;
        int pixel;
        double dir;
        for(int i = 0; i<anzPixel; i++){
            dir = getDirection() + ((i - (anzPixel/2)) / (anzPixel/2)) * 0.5 *  Math.PI;
            pixel = context.bitmap.getPixel(getX() + (int) (Math.cos(dir) * (getCircleSize())), getY() - (int) (Math.sin(dir) * (getCircleSize())));
            if(pixel != 0 && pixel != GamefieldView.restoreCirclePaint.getColor() && pixel != this.context.countdownPaint.getColor()){
                return true;
            }
        }
        return false;
    }

    private void endCircle(){
        this.alive = false;
        setCircleSpeed(0);
        context.playerAlive--;
    }
}
