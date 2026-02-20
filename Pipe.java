package com.example.flappybird;

import android.graphics.RectF;

public class Pipe {
    private static final int PIPE_WIDTH = 100;
    private static final int PIPE_SPEED = 10;

    private float x;
    private int topHeight;
    private int bottomY;
    private int gap;
    private boolean passed = false;

    private RectF topRect;
    private RectF bottomRect;

    public Pipe(float startX, int topHeight, int pipeHeight, int gap) {
        this.x = startX;
        this.topHeight = topHeight;
        this.gap = gap;
        this.bottomY = topHeight + gap;

        topRect = new RectF();
        bottomRect = new RectF();
    }

    public void update() {
        x -= PIPE_SPEED;
        topRect.set(x, 0, x + PIPE_WIDTH, topHeight);
        bottomRect.set(x, bottomY, x + PIPE_WIDTH, bottomY + 1000); // достаточно высокая нижняя труба
    }

    public float getX() {
        return x;
    }

    public int getWidth() {
        return PIPE_WIDTH;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public RectF getTopRect() {
        return topRect;
    }

    public RectF getBottomRect() {
        return bottomRect;
    }
}
