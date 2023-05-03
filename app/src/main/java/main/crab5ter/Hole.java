package main.crab5ter;

import android.graphics.Paint;

public class Hole {
    private float x;
    private float y;
    private float radius;
    private Paint paint;

    public Hole(float x, float y, float radius, Paint paint, int color) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.paint = paint;
        this.paint.setColor(color);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRadius() {
        return radius;
    }

    public Paint getPaint() {
        return paint;
    }
}
