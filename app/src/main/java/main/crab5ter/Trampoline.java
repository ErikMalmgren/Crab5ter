package main.crab5ter;

import android.graphics.Paint;
import android.graphics.Canvas;

import android.util.Log;

public class Trampoline {
    private float x;
    private float y;
    private float width;
    private float height;
    private Paint paint;

    public Trampoline(float x, float y, float width, float height, Paint paint, int color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.paint = paint;
        this.paint.setColor(color);
    }

    public float left(){
        return this.x;
    }

    public float right(){
        return this.x + this.width;
    }

    public float top(){
        return this.y;
    }

    public float bottom(){
        return this.y + height;
    }

    public float midX() { return this.left() + (this.width / 2); }
    public float midY() { return this.top() + (this.height / 2); }

    public Paint getPaint(){
        return this.paint;
    }

    public float clampX(float x){
        if(x < this.x){
            return this.x;
        }else if(x > this.x + this.width){
            return this.x + this.width;
        }else{
            return x;
        }
    }

    public float clampY(float y){
        if(y < this.y){
            return this.y;
        }else if(y > this.y + this.height){
            return this.y + this.height;
        }else{
            return y;
        }
    }
    public void createMaze(int [][] maze) {

    }
}
