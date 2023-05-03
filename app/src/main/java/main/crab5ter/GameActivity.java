package main.crab5ter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class GameActivity extends Activity implements SurfaceHolder.Callback, SensorEventListener {
    SurfaceView gameView;
    SurfaceHolder surfaceHolder;
    SensorManager sensorManager;
    Sensor accelerometer;
    Vibrator vibrator;
    private float playerX, playerY; // current position of the player
    private float playerRadius; // size of the player
    private Paint playerPaint;
    private ArrayList<Wall> walls;
    private ArrayList<Hole> holes;
    private GameThread thread;
    private float playerSpeedX, playerSpeedY;
    private float startX,startY; //start position for player.

    private int[][] maze;
    private long lastUpdate;

    //private int cellSize = 100;
    
    public GameActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameView = findViewById(R.id.gameView);
        surfaceHolder = gameView.getHolder();
        surfaceHolder.addCallback(this);

        // Set up sensor manager and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        init();
    }

    private void init() {
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
                if(maze[i][j] == 1) { //1 = vägg
                    float wallX = (j/ (float)maze[0].length) * gameView.getWidth();
                    float wallY = (i/ (float)maze.length) * gameView.getHeight();
                    float width = gameView.getWidth() / (float)maze[0].length;
                    float height = gameView.getHeight() / (float)maze.length;
                    walls.add(new Wall(wallX, wallY, width, height, new Paint(), Color.GREEN));
                } else if (maze[i][j] == -1) { //-1 = hål
                    float radius = Math.min(gameView.getWidth() / (float)maze[0].length,gameView.getHeight() / (float)maze.length)/2;
                    float holeX = (j/ (float)maze[0].length) * gameView.getWidth() + (gameView.getWidth() / (float)maze[0].length)/2;
                    float holeY = (i/ (float)maze.length) * gameView.getHeight() + (gameView.getHeight() / (float)maze.length)/2;
                    holes.add(new Hole(holeX,holeY,radius,new Paint(), Color.BLACK));
                }else if (maze[i][j] == 2){// 2 = startPosition.
                    playerRadius = Math.min(gameView.getWidth() / (float)maze[0].length,gameView.getHeight() / (float)maze.length)/2.5f;
                    startX= (j/ (float)maze[0].length) * gameView.getWidth() + (gameView.getWidth() / (float)maze[0].length)/2;
                    startY = (i/ (float)maze.length) * gameView.getHeight() + (gameView.getHeight() / (float)maze.length)/2;
                }
            }
        }
        respawnPlayer();
        // start the game thread
        thread = new GameThread(surfaceHolder, this);
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

    private void handleScreenEdgeCollision(){
        if (playerX - playerRadius < 0 || playerX + playerRadius > gameView.getWidth()) {
            playerSpeedX = -playerSpeedX * 0.50f; // reverse speed and apply friction
            if (playerX - playerRadius < 0) {
                playerX = playerRadius;
            } else {
                playerX = gameView.getWidth() - playerRadius;
            }
        }
        if (playerY - playerRadius < 0 || playerY + playerRadius > gameView.getHeight()) {
            playerSpeedY = -playerSpeedY * 0.50f; // reverse speed and apply friction
            if (playerY - playerRadius < 0) {
                playerY = playerRadius;
            } else {
                playerY = gameView.getHeight() - playerRadius;
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
        gameView.draw(canvas);
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

    @Override
    public void onPause() {
        super.onPause();
        // Unregister listener for accelerometer sensor
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register listener for accelerometer sensor
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // update player speed based on accelerometer sensor values
        long now = System.currentTimeMillis();
        if(lastUpdate != 0) {
            float deltaTime = (now - lastUpdate) / 1000.0f;
            double ax = Math.floor(event.values[0]);
            double ay = Math.floor(event.values[1]);
            updatePostition(ax, ay, deltaTime);
        }
        lastUpdate = now;
    }

    public void updatePostition(double ax, double ay, float deltaTime) {
        //Update speed
        playerSpeedX += - ax * deltaTime * 10;
        playerSpeedY += ay * deltaTime * 10;

        //Damping
        playerSpeedX *= 0.99f;
        playerSpeedY *= 0.99f;

        //Update position
        playerX += playerSpeedX * deltaTime;
        playerY += playerSpeedY * deltaTime;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
