package main.crab5ter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private float ballX, ballY; // current position of the ball
    private float ballSize; // size of the ball
    private Paint ballPaint;
    private Paint wallTest;
    //private boolean gameOver;

    private GameThread thread;

    private float ballSpeedX, ballSpeedY; // current speed of the ball

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
        ballPaint = new Paint();
        ballPaint.setColor(Color.BLUE);
        wallTest = new Paint();
        wallTest.setColor(Color.BLACK);
        ballSize = 20;
       // gameOver = false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // initialize game state
        ballX = getWidth() / 2f;
        ballY = getHeight() / 2f;
        ballSpeedX = 0;
        ballSpeedY = 0;

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
        // update ball speed based on accelerometer sensor values
        ballSpeedX += -ax/5 * 0.95f;
        ballSpeedY += ay/5  * 0.95f;
    }

    public void update() {
        // check for collisions with screen edges
        if (ballX - ballSize < 0 || ballX + ballSize > getWidth()) {
            ballSpeedX = -ballSpeedX * 0.3f; // reverse speed and apply friction
            if (ballX - ballSize < 0) {
                ballX = ballSize;
            } else {
                ballX = getWidth() - ballSize;
            }
        }
        if (ballY - ballSize < 0 || ballY + ballSize > getHeight()) {
            ballSpeedY = -ballSpeedY * 0.3f; // reverse speed and apply friction
            if (ballY - ballSize < 0) {
                ballY = ballSize;
            } else {
                ballY = getHeight() - ballSize;
            }
        }
        if(ballX-ballSize > getWidth()/4f && ballX-ballSize < (getWidth()/4f)+20){
            ballSpeedX = -ballSpeedX * 0.3f;
            if(ballX-ballSize < (getWidth()/4f)+20 && ballX-ballSize > (getWidth()/4f)+10){
                ballX = ballSize + (getWidth()/4f)+20;
            }else if(ballX+ballSize > getWidth()/4f && ballX+ballSize < (getWidth()/4f)+10){
                ballX = getWidth()/4f - ballSize;
            }
        }

        ballX += ballSpeedX;
        ballY += ballSpeedY;

        // check if ball is out of bounds
        //if (ballX < ballSize || ballX > getWidth() - ballSize || ballY < ballSize || ballY > getHeight() - ballSize) {
        //    gameOver = true;
        //}
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        canvas.drawCircle(ballX, ballY, ballSize, ballPaint);
        canvas.drawRect(getWidth()/4f,getHeight()/4f,getWidth()*3/4f,(getHeight()/4f)+20,wallTest);
/*        if (gameOver) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(100);
            String gameOverText = "GAME OVER";
            canvas.drawText(gameOverText, getWidth() / 2 - textPaint.measureText(gameOverText) / 2, getHeight() / 2, textPaint);
        }*/
    }

    public void pause() {
        // not used in this example
    }

    public void resume() {
        // not used in this example
    }
}
