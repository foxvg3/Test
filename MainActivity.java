package com.example.flappybird;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements GameOverListener {

    private GameView gameView;
    private TextView scoreTextView;
    private Button restartButton;
    private Button leaderboardButton;
    private SoundPool soundPool;
    private int jumpSoundId, scoreSoundId, hitSoundId;

    private GoogleSignInClient signInClient;
    private LeaderboardsClient leaderboardsClient;
    private boolean signedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);
        scoreTextView = findViewById(R.id.scoreTextView);
        restartButton = findViewById(R.id.restartButton);
        leaderboardButton = findViewById(R.id.leaderboardButton);

        // Настройка звуков
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();
        jumpSoundId = soundPool.load(this, R.raw.jump, 1);
        scoreSoundId = soundPool.load(this, R.raw.score, 1);
        hitSoundId = soundPool.load(this, R.raw.hit, 1);

        gameView.setSoundPool(soundPool, jumpSoundId, scoreSoundId, hitSoundId);
        gameView.setGameOverListener(this);

        // Google Play Games авторизация
        signInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());
        signInSilently();

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.restartGame();
                restartButton.setVisibility(View.GONE);
                leaderboardButton.setVisibility(View.GONE);
                scoreTextView.setVisibility(View.VISIBLE);
            }
        });

        leaderboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLeaderboard();
            }
        });
    }

    private void signInSilently() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            leaderboardsClient = Games.getLeaderboardsClient(this, account);
            signedIn = true;
        } else {
            startSignInIntent();
        }
    }

    private void startSignInIntent() {
        startActivityForResult(signInClient.getSignInIntent(), 9001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9001) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();
                leaderboardsClient = Games.getLeaderboardsClient(this, account);
                signedIn = true;
            }
        }
    }

    private void showLeaderboard() {
        if (signedIn && leaderboardsClient != null) {
            leaderboardsClient.getLeaderboardIntent(getString(R.string.leaderboard_id))
                    .addOnCompleteListener(new OnCompleteListener<Intent>() {
                        @Override
                        public void onComplete(@NonNull Task<Intent> task) {
                            if (task.isSuccessful()) {
                                startActivityForResult(task.getResult(), 100);
                            }
                        }
                    });
        } else {
            startSignInIntent();
        }
    }

    @Override
    public void onGameOver(final int score) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Показать кнопки рестарта и лидерборда
                restartButton.setVisibility(View.VISIBLE);
                leaderboardButton.setVisibility(View.VISIBLE);
                scoreTextView.setVisibility(View.GONE);
                // Отправить результат в лидерборд, если авторизован
                if (signedIn && leaderboardsClient != null) {
                    leaderboardsClient.submitScore(getString(R.string.leaderboard_id), score);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }
                  }
