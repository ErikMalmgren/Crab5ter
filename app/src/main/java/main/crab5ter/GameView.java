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
public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private float playerX, playerY; // current position of the player
    private float playerRadius; // size of the player
    private Paint playerPaint;
    private Wall wall1,wall2, wall;
    private Paint hole;
    private GameThread thread;
    private float playerSpeedX, playerSpeedY;
    private Vibrator vibrator;

    private int[][] maze;

    private int cellSize = 100;
    
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
        playerRadius = 50;
        hole = new Paint();
        hole.setColor(Color.BLACK);
        this.maze = new int[][]{
                {1, 1, 1, 1, 1, 2, 1, 1, 1,1,1},
                {1, 0, 0, 0, 0, 0, 1, 0, 0},
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
                {0,0,0,0,0,0,0,0,0,1,1},
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
        respwanPlayer();
        //wall1 = new Wall(getWidth()/4f,getHeight()/4f -100,getWidth()/2f, 100,new Paint(), Color.GREEN);
        //wall2 = new Wall(getWidth()/4f -100,getHeight()/4f,100, getHeight()/2f,new Paint(), Color.YELLOW);

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

   /* private void handleWallCollision(Wall wall){
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
    */
   private void handleWallCollision(){

       int playerCellX = (int) Math.floor(playerX / cellSize);
       int playerCellY = (int) Math.floor(playerY / cellSize);

       if (maze[playerCellY][playerCellX] == 1) {
           double distanceToWall = getDistance(playerX, playerY, playerCellX * cellSize + cellSize / 2, playerCellY * cellSize + cellSize / 2);
           Log.d("Distance to wall = ",  ": " + distanceToWall);
           if (distanceToWall < playerRadius) {
               double angle = Math.atan2(playerY - (playerCellY * cellSize + cellSize / 2), playerX - (playerCellX * cellSize + cellSize / 2));
               playerX = (float) ((playerCellX * cellSize + cellSize / 2) + (playerRadius * Math.cos(angle)));
               playerY = (float) ((playerCellY * cellSize + cellSize / 2) + (playerRadius * Math.sin(angle)));
               if (Math.round(Math.cos(angle)) != 0) playerSpeedX = Math.abs(playerSpeedX) * Math.round(Math.cos(angle)) * 0.50f;
               if (Math.round(Math.sin(angle)) != 0) playerSpeedY = Math.abs(playerSpeedY) * Math.round(Math.sin(angle)) * 0.50f;
               vibrator.vibrate(100);
           }
       } else if(maze[playerCellY][playerCellX] == -1) {
           // this is wrong..
           double distanceToHole = getDistance(playerX, playerY, playerCellX * cellSize + cellSize / 2, playerCellY * cellSize + cellSize / 2);
            if(distanceToHole < playerRadius) {
                respwanPlayer();
            }
       }
   }






    public void update() {
        handleScreenEdgeCollision();
        handleWallCollision();
       // if(getDistance(getWidth()*3f/4f,getHeight()*3f/4f,playerX,playerY) < 100){
       //     respwanPlayer();
       // }
        playerX += playerSpeedX;
        playerY += playerSpeedY;

    }

    private double getDistance(float x1,float y1, float x2, float y2){
        return Math.sqrt(Math.pow((x1-x2),2) + Math.pow((y1-y2),2));
    }

    private void respwanPlayer(){
        playerX = getWidth()/2f;
        playerY = getHeight()/2f;
        playerSpeedX = 0;
        playerSpeedY = 0;
    }


    public void draw(Canvas canvas) {
        super.draw(canvas);
      /*  canvas.drawColor(Color.WHITE);
        canvas.drawRect(wall1.left(),wall1.top(),wall1.right(),wall1.bottom(),wall1.getPaint());
        canvas.drawRect(wall2.left(),wall2.top(),wall2.right(),wall2.bottom(),wall2.getPaint());
        canvas.drawCircle(getWidth()*3f/4f,getHeight()*3f/4f, 100, hole);
        canvas.drawCircle(playerX, playerY, playerRadius, playerPaint);
      */


            Paint wallPaint = new Paint();
            wallPaint.setColor(Color.GREEN);
            Paint holePaint = new Paint();
            holePaint.setColor(Color.RED);
            canvas.drawColor(Color.WHITE);
            canvas.drawCircle(playerX, playerY, playerRadius, playerPaint);


            for (int i = 0; i < maze.length; i++) {
                for (int j = 0; j < maze[i].length; j++) {
                    int cellValue = maze[i][j];
                    if (cellValue == 1) {
                        canvas.drawRect(j * cellSize, i * cellSize,
                                (j + 1) * cellSize, (i + 1) * cellSize, wallPaint);
                    }
                     else if (cellValue == -1) {
                        canvas.drawCircle(j*cellSize,i*cellSize,playerRadius+5,holePaint);
                    }
                }
            }









/*        Paint clampLine = new Paint();
        clampLine.setColor(Color.RED);
        double angle = Math.atan2(playerX- wall.clampX(playerX),playerY-wall.clampY(playerY));
        canvas.drawCircle((float) (wall.clampX(playerX) + (playerRadius*Math.sin(angle))), (float) (wall.clampY(playerY) + (playerRadius*Math.cos(angle))),10,clampLine);*/

/*        Paint textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(100);
        DecimalFormat df = new DecimalFormat("#.####");
        String testText = df.format(Math.sin(angle)) + " " + df.format(Math.cos(angle));
        canvas.drawText(testText, getWidth()/2f - textPaint.measureText(testText)/2, getHeight()/2f, textPaint);*/
    }

    public void pause() {
        // not used in this example
    }

    public void resume() {
        // not used in this example
    }
}
