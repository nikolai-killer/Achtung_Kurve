package com.game.achtung_kurve;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.widget.ImageButton;


@SuppressLint("ViewConstructor")
public class GamefieldView extends View {
    public int player;
    public Bitmap bitmap;
    public Canvas bitmapCanvas;
    public static Paint restoreCirclePaint;
    public static Paint restorePathPaint;
    public static Paint bitmapPaint;

    //Startscreen stuff
    public Game game;
    public static final int countdownlength = 3;
    public int countdown;
    public Paint countdownPaint;
    public final int startPadding = (int) pxFromDp(60);
    public static final int startArrowColor = Color.RED;
    public float startArrowPadding = pxFromDp(10);
    public float startArrowLength = pxFromDp(30);
    public float startArrowTipLength = pxFromDp(10);
    public static float startArrowTipAngle = (float) 0.78;
    public Path startArrowPath = new Path();
    public Paint startArrowPaint;

    //Default settings
    public static final int defaultCircleSpeed = 4;
    public static final double defaultDirChangeSpeed = 0.05;
    public final float defaultCirclePathWidth = pxFromDp(4);
    public final float defaultCircleSize = 13;
    public final int[] colors = {Color.BLUE,Color.RED,Color.GREEN,Color.YELLOW};


    public int gamefieldWidth;
    public int gamefieldHeight;
    public ImageButton[] imageButtons;
    public Circle[] circles;

    public int playerAlive;

    public GamefieldView(Context context, ImageButton[] buttons, int player){
        super(context);
        game = (Game) this.getContext();
        this.player = player;
        circles = new Circle[player];
        playerAlive = player;
        imageButtons = buttons;
    }

    private void setupPaints(){
        bitmapPaint = new Paint();

        countdownPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        countdownPaint.setColor(Color.WHITE);
        countdownPaint.setTextSize(pxFromDp(30));

        startArrowPaint = new Paint();
        startArrowPaint.setColor(startArrowColor);
        startArrowPaint.setStyle(Paint.Style.STROKE);
        startArrowPaint.setStrokeWidth(7);
        startArrowPaint.setStrokeCap(Paint.Cap.ROUND);

        GamefieldView.restoreCirclePaint = new Paint();
        GamefieldView.restoreCirclePaint.setColor(Color.BLACK);
        GamefieldView.restorePathPaint = new Paint();
        GamefieldView.restorePathPaint.setColor(Color.BLACK);
        GamefieldView.restorePathPaint.setStyle(Paint.Style.STROKE);
        GamefieldView.restorePathPaint.setStrokeWidth(7);
        GamefieldView.restorePathPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void drawStartArrowPath(){
        float fromx;
        float fromy;
        float tipx;
        float tipy;
        float deltaWinkel;
        float righttipy;
        float righttipx;
        float lefttipx;
        float lefttipy;
        for(int i = 0; i<this.player; i++){
            fromx = (float) this.circles[i].getX() + (float) Math.cos(this.circles[i].getDirection())*this.startArrowPadding;
            fromy = (float) this.circles[i].getY() - (float) Math.sin(this.circles[i].getDirection()) * this.startArrowPadding;
            tipx = fromx + (float) Math.cos(this.circles[i].getDirection())*(this.startArrowLength -5);
            tipy = fromy - (float) Math.sin(this.circles[i].getDirection())*(this.startArrowLength -5);
            this.startArrowPath.moveTo(fromx,fromy);
            this.startArrowPath.lineTo(tipx,tipy);
            tipx = fromx + (float) Math.cos(this.circles[i].getDirection())*(this.startArrowLength);
            tipy = fromy - (float) Math.sin(this.circles[i].getDirection())*(this.startArrowLength);
            deltaWinkel = (float) Math.atan2((fromy-tipy),(tipx-fromx));
            if(deltaWinkel<0){deltaWinkel = 2* (float) Math.PI + deltaWinkel;}
            righttipx = tipx + this.startArrowTipLength * (float) Math.cos(deltaWinkel - (float)Math.PI/2 - GamefieldView.startArrowTipAngle);
            righttipy = tipy  - this.startArrowTipLength* (float)Math.sin(deltaWinkel - (float) Math.PI/2 - GamefieldView.startArrowTipAngle);
            lefttipx = tipx + this.startArrowTipLength * (float) Math.cos(deltaWinkel + (float) Math.PI/2+ GamefieldView.startArrowTipAngle);
            lefttipy = tipy - this.startArrowTipLength * (float) Math.sin(deltaWinkel + (float) Math.PI/2+ GamefieldView.startArrowTipAngle);
            this.startArrowPath.moveTo(tipx,tipy);
            this.startArrowPath.lineTo(lefttipx,lefttipy);
            this.startArrowPath.moveTo(tipx,tipy);
            this.startArrowPath.lineTo(righttipx,righttipy);
        }
        this.bitmapCanvas.drawPath(this.startArrowPath,this.startArrowPaint);
    }
    public void removeStartArrowPath(){
        this.bitmapCanvas.drawPath(this.startArrowPath,GamefieldView.restorePathPaint);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas){
        if(this.countdown > 0){
            canvas.rotate(-90,pxFromDp(30),pxFromDp(80));
            canvas.drawText("" + this.countdown,pxFromDp(50),pxFromDp(80),countdownPaint);
            canvas.rotate(90,pxFromDp(30),pxFromDp(80));
        }
        canvas.drawBitmap(bitmap,0,0,bitmapPaint);
    }

    private float pxFromDp(final float dp){
        return dp * this.getContext().getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        this.gamefieldHeight = yNew;
        this.gamefieldWidth = xNew;
        this.bitmap = Bitmap.createBitmap(gamefieldWidth,gamefieldHeight,Bitmap.Config.ARGB_8888);
        this.bitmapCanvas = new Canvas(this.bitmap);
        setupPaints();
        game.startGameHandler(this);
    }
}
