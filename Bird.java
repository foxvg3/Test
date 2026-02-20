package com.example.flappybird;

import android.graphics.RectF;

public class Bird {
    private static final float GRAVITY = 0.5f;
    private static final float JUMP_VELOCITY = -10f;
    private static final int BIRD_SIZE = 60; // размер птицы

    private float x, y;
    private float velocity;
    private RectF rect;

    public Bird() {
        rect = new RectF();
    }

    public void reset(float startY) {
        x = 200; // фиксированная позиция по X
        y = startY;
        velocity = 0;
    }

    public void update() {
        velocity += GRAVITY;
        y += velocity;
        rect.set(x, y, x + BIRD_SIZE, y + BIRD_SIZE);
    }

    public void jump() {
        velocity = JUMP_VELOCITY;
    }

    public RectF getRect() {
        return rect;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getHeight() {
        return BIRD_SIZE;
    }
}
