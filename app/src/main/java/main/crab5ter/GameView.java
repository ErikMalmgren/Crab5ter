package main.crab5ter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private float playerX, playerY; // current position of the player
    private float playerRadius; // size of the player
    private Paint playerPaint;
    private ArrayList<Wall> walls;
    private ArrayList<Hole> holes;
    private GameThread thread;
    private float playerSpeedX, playerSpeedY;
    private float startX,startY; //start position for player.
    private Vibrator vibrator;

    private int[][] maze;

    //private int cellSize = 100;
    
    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
        playerPaint = new Paint();
        playerPaint.setColor(Color.BLUE);
        holes = new ArrayList<Hole>();
        walls = new ArrayList<Wall>();
        this.maze = new int[][]{
                {1,1,1,1,0,2,0,1,1,1,1},
                {1,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,-1,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,-1,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,0,0},
                {1,1,1,1,1,1,1,1,1,1,1},
                {1,1,1,1,1,1,1,1,1,1,1}
        };
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        for(int i = 0; i < maze.length;i++){
            for(int j = 0; j < maze[0].length;j++){
                if(maze[i][j] == 1) {//1 = väg
                    float wallX = (j/ (float)maze[0].length) * getWidth();
                    float wallY = (i/ (float)maze.length) * getHeight();
                    float width = getWidth() / (float)maze[0].length;
                    float height = getHeight() / (float)maze.length;
                    walls.add(new Wall(wallX, wallY, width, height, new Paint(), Color.GREEN));
                }else if (maze[i][j] == -1){//-1 = hål
                    float radius = Math.min(getWidth() / (float)maze[0].length,getHeight() / (float)maze.length)/2;
                    float holeX = (j/ (float)maze[0].length) * getWidth() + (getWidth() / (float)maze[0].length)/2;
                    float holeY = (i/ (float)maze.length) * getHeight() + (getHeight() / (float)maze.length)/2;
                    holes.add(new Hole(holeX,holeY,radius,new Paint(), Color.BLACK));
                }else if (maze[i][j] == 2){// 2 = startPosition.
                    playerRadius = Math.min(getWidth() / (float)maze[0].length,getHeight() / (float)maze.length)/2.5f;
                    startX= (j/ (float)maze[0].length) * getWidth() + (getWidth() / (float)maze[0].length)/2;
                    startY = (i/ (float)maze.length) * getHeight() + (getHeight() / (float)maze.length)/2;
                }
            }
        }
        respawnPlayer();
        // start the game thread
        thread = new GameThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // stop the game thread
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                Log.d("Error", e.getMessage());
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void updatePosition(double ax, double ay) {
        // update player speed based on accelerometer sensor values

        playerSpeedX += -ax/10 * 0.95f;
        playerSpeedY += ay/10  * 0.95f;
    }

    public void setVibrator(Vibrator vibrator){
        this.vibrator =  vibrator;
    }

    private void handleScreenEdgeCollision(){
        if (playerX - playerRadius < 0 || playerX + playerRadius > getWidth()) {
            playerSpeedX = -playerSpeedX * 0.50f; // reverse speed and apply friction
            if (playerX - playerRadius < 0) {
                playerX = playerRadius;
            } else {
                playerX = getWidth() - playerRadius;
            }
        }
        if (playerY - playerRadius < 0 || playerY + playerRadius > getHeight()) {
            playerSpeedY = -playerSpeedY * 0.50f; // reverse speed and apply friction
            if (playerY - playerRadius < 0) {
                playerY = playerRadius;
            } else {
                playerY = getHeight() - playerRadius;
            }
        }
    }

    private void handleWallCollision(Wall wall){
        double distanceToWall = getDistance(wall.clampX(playerX),wall.clampY(playerY), playerX,playerY);
        if( distanceToWall < playerRadius){
            double angle = Math.atan2(playerX- wall.clampX(playerX),playerY-wall.clampY(playerY));
            playerX = (float)(wall.clampX(playerX) + (playerRadius*Math.sin(angle)));
            playerY = (float)(wall.clampY(playerY) + (playerRadius*Math.cos(angle)));
            if(Math.round(Math.cos(angle)) != 0)playerSpeedY = Math.abs(playerSpeedY) * Math.round(Math.cos(angle)) * 0.50f;
            if(Math.round(Math.sin(angle)) != 0)playerSpeedX = Math.abs(playerSpeedX) * Math.round(Math.sin(angle)) * 0.50f;
            vibrator.vibrate(100);
        }
    }

    public void update() {
        handleScreenEdgeCollision();
        for(Wall wall : walls){
            handleWallCollision(wall);
        }
        for(Hole hole : holes){
            if(getDistance(hole.getX(),hole.getY(),playerX,playerY) < hole.getRadius()){
                respawnPlayer();
            }
        }
        playerX += playerSpeedX;
        playerY += playerSpeedY;
    }

    private double getDistance(float x1,float y1, float x2, float y2){
        return Math.sqrt(Math.pow((x1-x2),2) + Math.pow((y1-y2),2));
    }

    private void respawnPlayer(){
        playerX = startX;
        playerY = startY;
        playerSpeedX = 0;
        playerSpeedY = 0;
    }


    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        Paint startPaint = new Paint();
        startPaint.setColor(Color.RED);
        canvas.drawCircle(startX,startY,playerRadius,startPaint);
        for(Wall wall:walls){
            canvas.drawRect(wall.left(),wall.top(),wall.right(),wall.bottom(),wall.getPaint());
        }
        for(Hole hole:holes){
            canvas.drawCircle(hole.getX(),hole.getY(), hole.getRadius(), hole.getPaint());
        }
        canvas.drawCircle(playerX, playerY, playerRadius, playerPaint);
    }

    public void pause() {
        // not used in this example
    }

    public void resume() {
        // not used in this example
    }
}
