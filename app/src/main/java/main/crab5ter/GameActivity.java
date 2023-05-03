package main.crab5ter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.WindowManager;
import android.widget.Toast;

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
    private float endX, endY; // goal position

    private int[][] maze, oldMaze;
    private long lastUpdate;

    public GameActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        this.oldMaze = new int[][]{
                {1,1,1,1,1,1,1,1,0,0,2},
                {1,0,0,0,0,0,0,1,0,0,0},
                {0,0,0,0,0,0,0,1,0,0,0},
                {0,0,0,0,0,0,0,1,0,0,1},
                {0,0,-1,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,1,1,1,1,1,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {1,1,1,1,1,0,0,0,0,1,1},
                {0,0,0,0,1,0,0,0,0,1,1},
                {0,0,0,0,1,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,-1,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,0,1,1},
                {0,0,0,0,0,0,0,0,1,1,1},
                {0,0,0,0,0,0,0,0,1,1,1},
                {1,1,1,1,0,3,0,1,1,1,1},
                {1,1,1,1,1,1,1,1,1,1,1}
        };
        this.maze = new int[][]{
                {2,0,0,1,1,1,1,1,1,1},
                {0,0,0,0,0,0,0,0,0,1},
                {1,0,0,1,1,1,1,0,0,1},
                {1,0,0,0,0,0,1,0,-1,0},
                {1,0,0,1,0,1,1,1,1,1},
                {1,0,0,0,0,0,1,0,0,3},
                {1,0,0,-1,0,0,1,0,0,1},
                {1,0,0,1,0,0,1,0,0,1},
                {1,0,0,1,0,0,1,0,0,1},
                {1,0,0,1,0,0,0,0,0,0},
                {1,1,1,1,1,1,1,0,-1,0}
        };
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        float mazeWidth = gameView.getWidth() / (float)maze[0].length;
        float mazeHeight = gameView.getHeight() / (float)maze.length;
        float minDimension = Math.min(mazeWidth, mazeHeight);

        for (int i = 0; i < maze.length; i++){
            for (int j = 0; j < maze[0].length; j++){
                float normalizedX = j / (float)maze[0].length;
                float normalizedY = i / (float)maze.length;
                float posX = normalizedX * gameView.getWidth();
                float posY = normalizedY * gameView.getHeight();
                float centerX = posX + mazeWidth / 2;
                float centerY = posY + mazeHeight / 2;

                if(maze[i][j] == 1) { //1 = vägg
                    walls.add(new Wall(posX, posY, mazeWidth, mazeHeight, new Paint(), Color.GREEN));
                } else if (maze[i][j] == -1) { //-1 = hål
                    holes.add(new Hole(centerX, centerY, minDimension / 2, new Paint(), Color.BLACK));
                } else if (maze[i][j] == 2) {// 2 = startPosition.
                    playerRadius = minDimension / 2.5f;
                    startX = centerX;
                    startY = centerY;
                } else if (maze[i][j] == 3) { // 3 = mål
                    endX = centerX;
                    endY = centerY;
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

            //beräkna den vinkeln som spelaren kommer in med mot väggen.
            double angle = Math.atan2(playerX- wall.clampX(playerX),playerY-wall.clampY(playerY));

            //Flytta ut spelaren ur väggen.
            playerX = (float)(wall.clampX(playerX) + (playerRadius*Math.sin(angle)));
            playerY = (float)(wall.clampY(playerY) + (playerRadius*Math.cos(angle)));

            //Ändra hastigheten så att spelaren studsar av väggen.
            if(Math.round(Math.cos(angle)) != 0)playerSpeedY = Math.abs(playerSpeedY) * Math.round(Math.cos(angle)) * 0.50f;
            if(Math.round(Math.sin(angle)) != 0)playerSpeedX = Math.abs(playerSpeedX) * Math.round(Math.sin(angle)) * 0.50f;

            if((playerRadius - distanceToWall) > 2)vibrator.vibrate(100);
        }
    }

    public void update() {
        handleScreenEdgeCollision();
        for(Wall wall : walls){
            handleWallCollision(wall);
        }
        for(Hole hole : holes){
            if(getDistance(hole.getX(),hole.getY(),playerX,playerY) < hole.getRadius()){
                onDeath();
                respawnPlayer();
            }
        }
        if (getDistance(endX, endY, playerX, playerY) < playerRadius){
            onWin();
            respawnPlayer();
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
        Paint endPaint = new Paint();
        endPaint.setColor(Color.YELLOW);
        canvas.drawCircle(startX,startY, playerRadius, startPaint);
        canvas.drawCircle(endX,endY, playerRadius, endPaint);
        for(Wall wall:walls){
            canvas.drawRect(wall.left(),wall.top(),wall.right(),wall.bottom(),wall.getPaint());
        }
        for(Hole hole:holes){
            canvas.drawCircle(hole.getX(),hole.getY(), hole.getRadius(), hole.getPaint());
        }
        canvas.drawCircle(playerX, playerY, playerRadius, playerPaint);
    }

    public void onDeath() {
        // https://pastebin.com/qpjpi80P
        onPause();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(GameActivity.this)
                        .setTitle("Tyvärr, du dog")
                        .setMessage("Vill du starta om spelet?")
                        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                respawnPlayer();
                                onResume();
                            }
                        })
                        .setNegativeButton("Nej", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                thread.setRunning(false);
                                finish();
                            }
                        })
                        .show();
            }
        });
    }

    public void onWin() {
        // https://pastebin.com/qpjpi80P
        onPause();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(GameActivity.this)
                        .setTitle("Grattis, du vann!")
                        .setMessage("Vill du spela igen?")
                        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                respawnPlayer();
                                onResume();
                            }
                        })
                        .setNegativeButton("Nej", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                thread.setRunning(false);
                                finish();
                            }
                        })
                        .show();
            }
        });
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
            updatePosition(ax, ay, deltaTime);
        }
        lastUpdate = now;
    }

    public void updatePosition(double ax, double ay, float deltaTime) {
        //Update speed
        playerSpeedX += - ax * deltaTime * 10;
        playerSpeedY += ay * deltaTime * 10;

        //Damping
        playerSpeedX *= 0.99f;
        playerSpeedY *= 0.99f;

        //Update position
        playerX += playerSpeedX * deltaTime;
        playerY += playerSpeedY * deltaTime;

        float speed = (float) Math.sqrt(Math.pow(playerSpeedX, 2) + Math.pow(playerSpeedY, 2));
        float maxSpeed = 20;

        if (speed > maxSpeed) {
            playerSpeedX = (playerSpeedX / speed) * maxSpeed;
            playerSpeedY = (playerSpeedY / speed) * maxSpeed;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
