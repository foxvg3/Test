package com.example.flappybird;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private boolean isPlaying;
    private boolean isGameOver = false;

    private Bird bird;
    private List<Pipe> pipes;
    private int score = 0;

    private Paint scorePaint;
    private Paint birdPaint;
    private Paint pipePaint;

    private int screenWidth, screenHeight;
    private Random random = new Random();

    private SoundPool soundPool;
    private int jumpSoundId, scoreSoundId, hitSoundId;

    private GameOverListener gameOverListener;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        birdPaint = new Paint();
        birdPaint.setColor(Color.YELLOW);

        pipePaint = new Paint();
        pipePaint.setColor(Color.GREEN);

        scorePaint = new Paint();
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextSize(80);
        scorePaint.setTextAlign(Paint.Align.CENTER);

        bird = new Bird();
        pipes = new ArrayList<>();

        getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                screenWidth = getWidth();
                screenHeight = getHeight();
                startGame();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                pause();
            }
        });
    }

    public void setSoundPool(SoundPool soundPool, int jump, int score, int hit) {
        this.soundPool = soundPool;
        this.jumpSoundId = jump;
        this.scoreSoundId = score;
        this.hitSoundId = hit;
    }

    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    public void startGame() {
        bird.reset(screenHeight / 2);
        pipes.clear();
        score = 0;
        isGameOver = false;
        isPlaying = true;
        resume();
    }

    public void restartGame() {
        startGame();
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (!isGameOver) {
                update();
            }
            draw();
            controlFPS();
        }
    }

    private void update() {
        bird.update();

        // Создание новых труб
        if (pipes.isEmpty() || pipes.get(pipes.size() - 1).getX() < screenWidth - 300) {
            int gap = 300 + random.nextInt(150);
            int pipeHeight = 200 + random.nextInt(400);
            pipes.add(new Pipe(screenWidth, 0, pipeHeight, gap));
        }

        // Обновление труб и проверка столкновений
        List<Pipe> pipesToRemove = new ArrayList<>();
        for (Pipe pipe : pipes) {
            pipe.update();

            // Проверка прохождения трубы (увеличение счета)
            if (!pipe.isPassed() && pipe.getX() + pipe.getWidth() < bird.getX()) {
                pipe.setPassed(true);
                score++;
                if (soundPool != null) {
                    soundPool.play(scoreSoundId, 1, 1, 1, 0, 1);
                }
            }

            // Столкновение с трубой
            if (RectF.intersects(bird.getRect(), pipe.getTopRect()) ||
                    RectF.intersects(bird.getRect(), pipe.getBottomRect())) {
                gameOver();
            }

            // Удаление труб за экраном
            if (pipe.getX() + pipe.getWidth() < 0) {
                pipesToRemove.add(pipe);
            }
        }
        pipes.removeAll(pipesToRemove);

        // Проверка выхода за границы
        if (bird.getY() < 0 || bird.getY() + bird.getHeight() > screenHeight) {
            gameOver();
        }
    }

    private void gameOver() {
        if (isGameOver) return;
        isGameOver = true;
        if (soundPool != null) {
            soundPool.play(hitSoundId, 1, 1, 1, 0, 1);
        }
        if (gameOverListener != null) {
            gameOverListener.onGameOver(score);
        }
    }

    private void draw() {
        SurfaceHolder holder = getHolder();
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();
            canvas.drawColor(Color.CYAN); // небо

            // Рисуем трубы
            for (Pipe pipe : pipes) {
                canvas.drawRect(pipe.getTopRect(), pipePaint);
                canvas.drawRect(pipe.getBottomRect(), pipePaint);
            }

            // Рисуем птицу
            canvas.drawRect(bird.getRect(), birdPaint);

            // Рисуем счёт
            canvas.drawText(String.valueOf(score), screenWidth / 2, 150, scorePaint);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void controlFPS() {
        try {
            Thread.sleep(16); // ~60 FPS
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isGameOver) {
                bird.jump();
                if (soundPool != null) {
                    soundPool.play(jumpSoundId, 1, 1, 1, 0, 1);
                }
            }
        }
        return true;
    }
              }
