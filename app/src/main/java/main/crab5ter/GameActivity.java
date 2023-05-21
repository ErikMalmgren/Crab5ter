package main.crab5ter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
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
    private ArrayList<Trampoline> trampolines;
    private GameThread thread;
    private float playerSpeedX, playerSpeedY, playerSpeed;
    private float startX,startY; //start position for player.
    private float endX, endY; // goal position
    private boolean playerOnTrampoline;
    private Bitmap bitmap;
    private Matrix matrix;
    private Bitmap wallBitmap;
    private Bitmap trampolineBitmap;
    private long lastUpdate;
    private Sound sounds;
    MediaPlayer mediaPlayer;
    private int[][] maze;


    public GameActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        gameView = findViewById(R.id.gameView);
        gameView.setZOrderOnTop(true);
        surfaceHolder = gameView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        // Set up sensor manager and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sounds = new Sound(this);
        mediaPlayer = MediaPlayer.create(this, R.raw.beach_ambience);
        mediaPlayer.setVolume(0.1f, 0.1f);
        mediaPlayer.start();
        mediaPlayer.setLooping(true);
        init();

    }

    private void init() {
        playerPaint = new Paint();
        playerPaint.setColor(Color.parseColor("#e06b12"));
        holes = new ArrayList<Hole>();
        walls = new ArrayList<Wall>();
        trampolines = new ArrayList<Trampoline>();
        if(gameMode().equals("easy")) {
            this.maze = new Maze().getEasyMaze();
        } else if(gameMode().equals("medium")) {
            this.maze = new Maze().getMediumMaze();
        } else if(gameMode().equals("hard")) {
            this.maze = new Maze().getHardMaze();
        }
        playerOnTrampoline = false;

    }

    public String gameMode() {
        return getIntent().getStringExtra("GameMode");
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
                } else if(maze[i][j] == 4) { // 4 = trampolin
                    trampolines.add(new Trampoline(posX, posY, mazeWidth, mazeHeight, new Paint(),Color.TRANSPARENT));
                }
            }
        }

        // wall
        wallBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stone);

        // trampoline
        trampolineBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.trampoline);

        respawnPlayer();
        // start the game thread
        thread = new GameThread(surfaceHolder, this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // stop the game thread
        mediaPlayer.stop();
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

            float oldSpeedY = playerSpeedY;
            float oldSpeedX = playerSpeedX;

            //beräkna den vinkeln som spelaren kommer in med mot väggen.
            double angle = Math.atan2(playerX- wall.clampX(playerX),playerY-wall.clampY(playerY));

            //Flytta ut spelaren ur väggen.
            playerX = (float)(wall.clampX(playerX) + (playerRadius*Math.sin(angle)));
            playerY = (float)(wall.clampY(playerY) + (playerRadius*Math.cos(angle)));

            //Ändra hastigheten så att spelaren studsar av väggen.
            if(Math.round(Math.cos(angle)) != 0)playerSpeedY = Math.abs(playerSpeedY) * Math.round(Math.cos(angle)) * 0.50f;
            if(Math.round(Math.sin(angle)) != 0)playerSpeedX = Math.abs(playerSpeedX) * Math.round(Math.sin(angle)) * 0.50f;

            // tester med vibration och ljud
            float deltaX = oldSpeedX - playerSpeedX;
            float deltaY = oldSpeedY - playerSpeedY;
            float magnitude = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            if(magnitude > 10 ) {
                sounds.playCrashSound(magnitude/80);
                vibrator.vibrate((long) (magnitude * 5)); // bara vibrate vid hård träff
            }
        }
    }

    public void update() {
        handleScreenEdgeCollision();
        for(Wall wall : walls){
            handleWallCollision(wall);
        }
        for(Hole hole : holes){
            if(getDistance(hole.getX(),hole.getY(),playerX,playerY) < hole.getRadius()){
                sounds.playDeathSound();
                onFinish("You Lose!");
            }
        }

        for (Trampoline trampoline : trampolines) {
            float trampolineHeight = Math.abs(trampoline.top() - trampoline.bottom());
            if (getDistance(trampoline.midX(), trampoline.midY(), playerX, playerY) < trampolineHeight) {
                playerOnTrampoline = true;
                break;
            } else {
                playerOnTrampoline = false;
            }
        }
        // win
        if (getDistance(endX, endY, playerX, playerY) < playerRadius){
            sounds.playWinSound();
            onFinish("You Win!");

        }
        playerX += playerSpeedX;
        playerY += playerSpeedY;
    }

    public void onFinish(String string) {
        thread.setRunning(false);
        respawnPlayer();

        Intent i = new Intent(this, FinishActivity.class);
        i.putExtra("GameMode" , gameMode());
        i.putExtra("showMsg", string);
        startActivity(i);
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
        
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        Paint startPaint = new Paint();
        startPaint.setColor(Color.parseColor("#51B2D6"));
        Paint endPaint = new Paint();
        endPaint.setColor(Color.YELLOW);
        canvas.drawCircle(startX,startY, playerRadius, startPaint);
        canvas.drawCircle(endX,endY, playerRadius, endPaint);

        for(Wall wall:walls){
            Rect rect = new Rect((int) wall.left(), (int) wall.top(), (int) wall.right(), (int) wall.bottom());
            canvas.drawBitmap(wallBitmap, null, rect, null);
        }

        for(Hole hole:holes){
            canvas.drawCircle(hole.getX(),hole.getY(), hole.getRadius(), hole.getPaint());
        }

        for(Trampoline trampoline: trampolines) {
            Rect rect = new Rect((int) trampoline.left(), (int) trampoline.top(), (int) trampoline.right(), (int) trampoline.bottom());
            canvas.drawBitmap(trampolineBitmap, null, rect, null);
        }

        canvas.drawCircle(playerX, playerY, playerRadius, playerPaint);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister listener for accelerometer sensor
        sensorManager.unregisterListener(this);
        thread.setRunning(false);
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

        // Kolla om mobilen skakas, och att bollen är på en trampolin
        if((Math.abs(event.values[0]) > 20 || Math.abs(event.values[1]) > 20 || Math.abs(event.values[2]) > 20) && playerOnTrampoline) {
            jump();
        }


    }
    // hoppa med bollen, kanske att man bara kan hoppa mellan två statiska trampoliner?
    private void jump() {
        vibrator.vibrate(100);
        playerSpeedY = -50;
        float newPlayerX = playerX;
        float newPlayerY = playerY;
        double dist = Double.MAX_VALUE;

        // hitta den närmsta trampolinen som inte är den man är på
        for (Trampoline trampoline : trampolines) {
            float trampolineHeight = Math.abs(trampoline.top() - trampoline.bottom());
            double distTrampoline = getDistance(trampoline.midX(), trampoline.midY(), playerX, playerY);
            if (distTrampoline > (trampolineHeight) && distTrampoline < dist) {
                newPlayerX = trampoline.midX();
                newPlayerY = trampoline.midY();
                dist = distTrampoline;
            }
        }

        playerX = newPlayerX;
        playerY = newPlayerY;
        playerOnTrampoline = false;
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

        playerSpeed = (float) Math.sqrt(Math.pow(playerSpeedX, 2) + Math.pow(playerSpeedY, 2));
        float maxSpeed = 20;

        if (playerSpeed > maxSpeed) {
            playerSpeedX = (playerSpeedX / playerSpeed) * maxSpeed;
            playerSpeedY = (playerSpeedY / playerSpeed) * maxSpeed;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
